package com.thermofisher.cdcam.services;

import com.gigya.socialize.GSKeyNotFoundException;
import com.thermofisher.cdcam.enums.aws.CdcamSecrets;
import com.thermofisher.cdcam.enums.cdc.FederationProviders;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccountV2;
import com.thermofisher.cdcam.model.cdc.CDCResponse;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CDCValidationError;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.Data;
import com.thermofisher.cdcam.model.cdc.OpenIdProvider;
import com.thermofisher.cdcam.model.cdc.OpenIdRelyingParty;
import com.thermofisher.cdcam.model.cdc.Registration;
import com.thermofisher.cdcam.model.dto.ConsentDTO;
import com.thermofisher.cdcam.model.notifications.AccountUpdatedNotification;
import com.thermofisher.cdcam.model.notifications.MergedAccountNotification;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCAccountsHandler;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class AccountsService {
    private final int FED_PASSWORD_LENGTH = 10;

    @Value("${general.cipdc}")
    private String cipdc;

    @Value("${is-new-marketing-enabled}")
    private boolean isNewMarketingConsentEnabled;

    @Value("${is-registration-notification-enabled}")
    private boolean isRegistrationNotificationEnabled;

    @Autowired
    GigyaService gigyaService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    SecretsService secretsService;

    @Async
    public void onAccountRegistered(@NotBlank String uid) {
        log.info(String.format("onAccountRegistered called by webhook for UID: %s", uid));
        Objects.requireNonNull(uid);

        try {
            AccountInfo account = gigyaService.getAccountInfo(uid);

            try {
                log.info("Saving post registration data.");
                setPostRegistrationData(account);
            } catch (Exception e) {
                log.error(String.format("Error on saving post registration data. %s", e.getMessage()));
            }

            if (hasFederationProvider(account)) {
                if (account.getPassword().isEmpty()) {
                    account.setPassword(Utils.getAlphaNumericString(FED_PASSWORD_LENGTH));
                }

                if (isRegistrationNotificationEnabled) {
                    log.info(String.format("Sending account registration notification for federated account. UID: %s", account.getUid()));
                    notificationService.sendAccountRegisteredNotification(account, cipdc);
                    log.info(String.format("Account registration notification sent successfully for federated account. UID: %s", account.getUid()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(String.format("Error: %s. UID: %s.", e.getMessage(), uid));
        }
    }

    private void setPostRegistrationData(AccountInfo account) throws JSONException, GSKeyNotFoundException, CustomGigyaErrorException {
        String awsQuickSightRole = secretsService.get(CdcamSecrets.QUICKSIGHT_ROLE.getKey());

        Registration registration = Registration.builder().build();
        if (StringUtils.isNotBlank(account.getOpenIdProviderId())) {
            OpenIdRelyingParty openIdRelyingParty = gigyaService.getRP(account.getOpenIdProviderId());
            OpenIdProvider openIdProvider = OpenIdProvider.builder()
                    .providerName(openIdRelyingParty.getDescription())
                    .build();

            registration.setOpenIdProvider(openIdProvider);
        }

        Data data = Data.builder()
                .awsQuickSightRole(awsQuickSightRole)
                .registration(registration)
                .build();

        CDCAccount cdcAccount = CDCAccount.builder()
                .UID(account.getUid())
                .data(data)
                .build();

        gigyaService.setAccountInfo(cdcAccount);
    }

    public CDCResponseData createAccount(AccountInfo accountInfo) throws JSONException, IOException, CustomGigyaErrorException {
        Objects.requireNonNull(accountInfo);
        CDCResponseData accountCreationResponse = null;

        if (isNewMarketingConsentEnabled) {
            CDCNewAccountV2 newAccountV2 = CDCAccountsHandler.buildCDCNewAccountV2(accountInfo);
            accountCreationResponse = gigyaService.register(newAccountV2);
        } else {
            CDCNewAccount newAccount = CDCAccountsHandler.buildCDCNewAccount(accountInfo);
            accountCreationResponse = gigyaService.register(newAccount);
        }

        if (!HttpStatus.valueOf(accountCreationResponse.getStatusCode()).is2xxSuccessful()) {
            if (Objects.nonNull(accountCreationResponse.getValidationErrors())) {
                for (CDCValidationError validationError : accountCreationResponse.getValidationErrors()) {
                    log.error(String.format("Error %d on %s: %s.", validationError.getErrorCode(), validationError.getFieldName(), validationError.getMessage()));
                }
            }

            String error = String.format("Error on account registration request. Username: %s. Error: %d %s", accountInfo.getUsername(), accountCreationResponse.getErrorCode(), accountCreationResponse.getErrorDetails());
            throw new CustomGigyaErrorException(error);
        }

        return accountCreationResponse;
    }

    private boolean hasFederationProvider(AccountInfo account) {
        return account.getLoginProvider().toLowerCase().contains(FederationProviders.OIDC.getValue()) || account.getLoginProvider().toLowerCase().contains(FederationProviders.SAML.getValue());
    }

    @Async
    public void onAccountMerged(@NotBlank String uid) {
        Objects.requireNonNull(uid);

        log.info(String.format("Account linking merge process started for UID: %s", uid));
        try {
            AccountInfo accountInfo = gigyaService.getAccountInfo(uid);
            if (!accountInfo.isFederatedAccount()) {
                log.info(String.format("Merge update process stopped. Account %s with UID %s is not federated. Merge update is only supported for federated accounts.", accountInfo.getUsername(), uid));
                return;
            }

            log.info("Setting random password for merged account notification.");
            accountInfo.setPassword(Utils.getAlphaNumericString(FED_PASSWORD_LENGTH));
            log.info("Building MergedAccountNotification object.");
            MergedAccountNotification mergedAccountNotification = MergedAccountNotification.build(accountInfo);
            log.info("Sending accountMerged notification.");
            notificationService.sendAccountMergedNotification(mergedAccountNotification);
            log.info("accountMerged notification sent.");
        } catch (Exception e) {
            log.error(String.format("onAccountMerged - Something went wrong. %s.", e.getMessage()));
        }
    }

    @Async
    public void onAccountUpdated(@NotBlank String uid) {
        log.info(String.format("Account linking update process started for UID: %s", uid));
        try {
            AccountInfo accountInfo = gigyaService.getAccountInfo(uid);
            if (!accountInfo.isFederatedAccount()) {
                log.info(String.format("Update process stopped. Account %s with UID %s is not federated. Update is only supported for federated accounts.", accountInfo.getUsername(), uid));
                return;
            }

            log.info("Building AccountUpdatedNotification object.");
            AccountUpdatedNotification accountUpdatedNotification = AccountUpdatedNotification.build(accountInfo);
            log.info("Sending accountUpdated notification.");
            notificationService.sendPrivateAccountUpdatedNotification(accountUpdatedNotification);
            log.info("accountUpdated notification sent.");
        } catch (Exception e) {
            log.error(String.format("onAccountUpdated - Something went wrong. %s.", e.getMessage()));
        }
    }

    public CDCResponse verify(AccountInfo account, String regToken) throws CustomGigyaErrorException, JSONException {
        Map<String, String> params = new HashMap<>();
        params.put("UID", account.getUid());
        params.put("isVerified", "true");
        JSONObject dataJson = new JSONObject();
        dataJson.put("verifiedEmailDate", LocalDate.now());
        params.put("data", dataJson.toString());
        params.put("finalizeRegistration", "true");
        gigyaService.setAccountInfo(params);
        log.info(String.format("Finalizing registration for UID: %s", account.getUid()));
        return gigyaService.finalizeRegistration(regToken);
    }

    public void updateConsent(ConsentDTO consentDTO) throws CustomGigyaErrorException, JSONException {
        log.info("Initiated consent update for user with UID: {}", consentDTO.getUid());

        Map<String, String> params = new HashMap<>();
        params.put("UID", consentDTO.getUid());

        JSONObject consent = new JSONObject();
        consent.put("isConsentGranted", consentDTO.getMarketingConsent());

        JSONObject marketing = new JSONObject();
        marketing.put("consent", consent);

        JSONObject preferences = new JSONObject();
        preferences.put("marketing", marketing);

        params.put("preferences", preferences.toString());

        if (consentDTO.getMarketingConsent()) {
            log.info("Updating additional city and company fields.");
            JSONObject work = new JSONObject();
            work.put("company", consentDTO.getCompany());

            JSONObject profile = new JSONObject();
            profile.put("city", consentDTO.getCity());
            profile.put("work", work);

            params.put("profile", profile.toString());
        }

        gigyaService.setAccountInfo(params);
        log.info("Marketing consent update completed.");
    }

    public void notifyUpdatedConsent(String uid) throws CustomGigyaErrorException {
        log.info("Initiated updated consent notification for user with UID: {}", uid);
        AccountInfo updatedAccountInfo = gigyaService.getAccountInfo(uid);
        AccountUpdatedNotification accountUpdatedNotification = AccountUpdatedNotification.build(updatedAccountInfo);
        notificationService.sendPublicAccountUpdatedNotification(accountUpdatedNotification);
        log.info("Completed updated consent notification.");
    }
}
