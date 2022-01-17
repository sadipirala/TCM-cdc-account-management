package com.thermofisher.cdcam.services;

import java.io.IOException;
import java.util.Objects;

import javax.validation.constraints.NotBlank;

import com.gigya.socialize.GSKeyNotFoundException;
import com.thermofisher.cdcam.enums.aws.CdcamSecrets;
import com.thermofisher.cdcam.enums.cdc.FederationProviders;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CDCValidationError;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.Data;
import com.thermofisher.cdcam.model.cdc.OpenIdProvider;
import com.thermofisher.cdcam.model.cdc.OpenIdRelyingParty;
import com.thermofisher.cdcam.model.cdc.Registration;
import com.thermofisher.cdcam.model.notifications.AccountUpdatedNotification;
import com.thermofisher.cdcam.model.notifications.MergedAccountNotification;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCAccountsHandler;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AccountRequestService {
    private Logger logger = LogManager.getLogger(this.getClass());
    private final int FED_PASSWORD_LENGTH = 10;

    @Value("${general.cipdc}")
    private String cipdc;

    @Autowired
    CDCAccountsService cdcAccountsService;

    @Autowired
    CDCResponseHandler cdcResponseHandler;

    @Autowired
    NotificationService notificationService;

    @Autowired
    SecretsService secretsService;

    @Async
    public void onAccountRegistered(@NotBlank String uid) {
        logger.info(String.format("onAccountRegistered called by webhook for UID: %s", uid));
        Objects.requireNonNull(uid);

        try {
            AccountInfo account = cdcResponseHandler.getAccountInfo(uid);
            
            try {
                logger.info("Saving post registration data.");
                setPostRegistrationData(account);
            } catch (Exception e) {
                logger.error(String.format("Error on saving post registration data. %s", e.getMessage()));
            }
            
            try {
                logger.info(String.format("Sending account info notification for UID: %s", uid));
                notificationService.sendNotifyAccountInfoNotification(account, cipdc);
                logger.info(String.format("Account info notification sent for UID: %s", uid));
            } catch (Exception e) {
                logger.error(String.format("Account info notification error: %s", e.getMessage()));
            }

            if (hasFederationProvider(account)) {
                if (account.getPassword().isEmpty()) {
                    account.setPassword(Utils.getAlphaNumericString(FED_PASSWORD_LENGTH));
                }

                logger.info(String.format("Sending account registration notification for federated account. UID: %s", account.getUid()));
                notificationService.sendAccountRegisteredNotification(account, cipdc);
                logger.info(String.format("Account registration notification sent successfully for federated account. UID: %s", account.getUid()));
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
            OpenIdRelyingParty openIdRelyingParty = cdcResponseHandler.getRP(account.getOpenIdProviderId());
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

        cdcResponseHandler.setAccountInfo(cdcAccount);
    }

    public CDCResponseData createAccount(AccountInfo accountInfo) throws JSONException, IOException, CustomGigyaErrorException {
        Objects.requireNonNull(accountInfo);
        CDCNewAccount newAccount = CDCAccountsHandler.buildCDCNewAccount(accountInfo);
        CDCResponseData accountCreationResponse = cdcResponseHandler.register(newAccount);

        if (!HttpStatus.valueOf(accountCreationResponse.getStatusCode()).is2xxSuccessful()) {
            if (Objects.nonNull(accountCreationResponse.getValidationErrors())) {
                for (CDCValidationError validationError : accountCreationResponse.getValidationErrors()) {
                    logger.error(String.format("Error %d on %s: %s.", validationError.getErrorCode(), validationError.getFieldName(), validationError.getMessage()));
                }
            }

            String error = String.format("Error on account registration request. Username: %s. Error: %d %s", accountInfo.getUsername(), accountCreationResponse.getStatusCode(), accountCreationResponse.getStatusReason());
            throw new CustomGigyaErrorException(error);
        }

        return accountCreationResponse;
    }

    @Async
    public void sendVerificationEmail(String uid) {
        triggerVerificationEmailProcess(uid);
    }

    public CDCResponseData sendVerificationEmailSync(String uid) {
        return triggerVerificationEmailProcess(uid);
    }

    private CDCResponseData triggerVerificationEmailProcess(String uid) {
        CDCResponseData response = new CDCResponseData();

        try {
            response = cdcResponseHandler.sendVerificationEmail(uid);
            HttpStatus status = HttpStatus.valueOf(response.getStatusCode());

            if (status.is2xxSuccessful()) {
                logger.info(String.format("Verification email sent successfully. UID: %s", uid));
            } else {
                logger.info(String.format("Something went wrong while sending the verification email. UID: %s. Status: %d. Error: %s", uid, status.value(), response.getErrorDetails()));
            }
        } catch (Exception e) {
            logger.error(String.format("An exception occurred while sending the verification email to the user. UID: %s. Exception: %s", uid, Utils.stackTraceToString(e)));
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }

    private boolean hasFederationProvider(AccountInfo account) {
        return account.getLoginProvider().toLowerCase().contains(FederationProviders.OIDC.getValue()) || account.getLoginProvider().toLowerCase().contains(FederationProviders.SAML.getValue());
    }

    @Async
    public void onAccountMerged(@NotBlank String uid) {
        Objects.requireNonNull(uid);

        logger.info(String.format("Account linking merge process started for UID: %s", uid));
        try {
            AccountInfo accountInfo = cdcResponseHandler.getAccountInfo(uid);
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
            AccountInfo accountInfo = cdcResponseHandler.getAccountInfo(uid);
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
}
