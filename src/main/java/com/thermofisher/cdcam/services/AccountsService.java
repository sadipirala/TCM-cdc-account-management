package com.thermofisher.cdcam.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotBlank;

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
import com.thermofisher.cdcam.model.dto.SelfServeConsentDTO;
import com.thermofisher.cdcam.model.notifications.AccountUpdatedNotification;
import com.thermofisher.cdcam.model.notifications.MergedAccountNotification;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCAccountsHandler;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {
    private Logger logger = LogManager.getLogger(this.getClass());
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
        logger.info(String.format("onAccountRegistered called by webhook for UID: %s", uid));
        Objects.requireNonNull(uid);

        try {
            AccountInfo account = gigyaService.getAccountInfo(uid);
            
            try {
                logger.info("Saving post registration data.");
                setPostRegistrationData(account);
            } catch (Exception e) {
                logger.error(String.format("Error on saving post registration data. %s", e.getMessage()));
            }

            if (hasFederationProvider(account)) {
                if (account.getPassword().isEmpty()) {
                    account.setPassword(Utils.getAlphaNumericString(FED_PASSWORD_LENGTH));
                }

                if (isRegistrationNotificationEnabled) {
                    logger.info(String.format("Sending account registration notification for federated account. UID: %s", account.getUid()));
                    notificationService.sendAccountRegisteredNotification(account, cipdc);
                    logger.info(String.format("Account registration notification sent successfully for federated account. UID: %s", account.getUid()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(String.format("Error: %s. UID: %s.", e.getMessage(), uid));
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
                    logger.error(String.format("Error %d on %s: %s.", validationError.getErrorCode(), validationError.getFieldName(), validationError.getMessage()));
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

        logger.info(String.format("Account linking merge process started for UID: %s", uid));
        try {
            AccountInfo accountInfo = gigyaService.getAccountInfo(uid);
            if (!accountInfo.isFederatedAccount()) {
                logger.info(String.format("Merge update process stopped. Account %s with UID %s is not federated. Merge update is only supported for federated accounts.", accountInfo.getUsername(), uid));
                return;
            }

            logger.info("Setting random password for merged account notification.");
            accountInfo.setPassword(Utils.getAlphaNumericString(FED_PASSWORD_LENGTH));
            logger.info("Building MergedAccountNotification object.");
            MergedAccountNotification mergedAccountNotification = MergedAccountNotification.build(accountInfo);
            logger.info("Sending accountMerged notification.");
            notificationService.sendAccountMergedNotification(mergedAccountNotification);
            logger.info("accountMerged notification sent.");
        } catch (Exception e) {
            logger.error(String.format("onAccountMerged - Something went wrong. %s.", e.getMessage()));
        }
    }

    @Async
    public void onAccountUpdated(@NotBlank String uid) {
        logger.info(String.format("Account linking update process started for UID: %s", uid));
        try {
            AccountInfo accountInfo = gigyaService.getAccountInfo(uid);
            if (!accountInfo.isFederatedAccount()) {
                logger.info(String.format("Update process stopped. Account %s with UID %s is not federated. Update is only supported for federated accounts.", accountInfo.getUsername(), uid));
                return;
            }

            logger.info("Building AccountUpdatedNotification object.");
            AccountUpdatedNotification accountUpdatedNotification = AccountUpdatedNotification.build(accountInfo);
            logger.info("Sending accountUpdated notification.");
            notificationService.sendPrivateAccountUpdatedNotification(accountUpdatedNotification);
            logger.info("accountUpdated notification sent.");
        } catch (Exception e) {
            logger.error(String.format("onAccountUpdated - Something went wrong. %s.", e.getMessage()));
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
        logger.info(String.format("Finalizing registration for UID: %s", account.getUid()));
        return gigyaService.finalizeRegistration(regToken);
    }

    public void updateMarketingConsent(SelfServeConsentDTO selfServeConsentDTO) throws CustomGigyaErrorException, JSONException {
        logger.info("Initiated marketing consent update for user with UID: {}", selfServeConsentDTO.getUid());

        Map<String, String> params = new HashMap<>();
        params.put("UID", selfServeConsentDTO.getUid());

        JSONObject consent = new JSONObject();
        consent.put("isConsentGranted", selfServeConsentDTO.getMarketingConsent());

        JSONObject marketing = new JSONObject();
        marketing.put("consent", consent);

        JSONObject preferences = new JSONObject();
        preferences.put("marketing", marketing);

        params.put("preferences", preferences.toString());

        if (selfServeConsentDTO.getMarketingConsent()) {
            logger.info("Updating additional city and company fields.");
            JSONObject profile = new JSONObject();
            profile.put("city", selfServeConsentDTO.getCity());

            JSONObject work = new JSONObject();
            work.put("company", selfServeConsentDTO.getCompany());
            profile.put("work", work.toString());

            params.put("profile", profile.toString());
        }

        gigyaService.setAccountInfo(params);
        logger.info("Marketing consent update completed.");
    }

    public void notifyUpdatedMarketingConsent(String uid) throws CustomGigyaErrorException {
        logger.info("Initiated updated marketing consent notification for user with UID: {}", uid);
        AccountInfo updatedAccountInfo = gigyaService.getAccountInfo(uid);
        AccountUpdatedNotification accountUpdatedNotification = AccountUpdatedNotification.build(updatedAccountInfo);
        notificationService.sendPublicAccountUpdatedNotification(accountUpdatedNotification);
        logger.info("Marketing consent notification update completed.");
    }
}
