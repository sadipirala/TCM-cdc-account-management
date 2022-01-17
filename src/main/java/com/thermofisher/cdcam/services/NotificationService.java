package com.thermofisher.cdcam.services;

import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.GsonBuilder;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.EmailNotification;
import com.thermofisher.cdcam.model.EmailUpdatedNotification;
import com.thermofisher.cdcam.model.MarketingConsentUpdatedNotification;
import com.thermofisher.cdcam.model.dto.RequestResetPasswordDTO;
import com.thermofisher.cdcam.model.dto.UsernameRecoveryDTO;
import com.thermofisher.cdcam.model.notifications.AccountUpdatedNotification;
import com.thermofisher.cdcam.model.notifications.MergedAccountNotification;
import com.thermofisher.cdcam.model.notifications.PasswordUpdateNotification;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import com.thermofisher.cdcam.utils.EmailLocaleUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Value("${tf.home}")
    private String redirectUrl;

    @Value("${reset-password.url}")
    private String resetPasswordUrl;

    @Value("${aws.sns.accnt.info.topic}")
    private String accountInfoSNSTopic;

    @Value("${aws.sns.aspire.reg.topic}")
    private String aspireRegistrationSNSTopic;

    @Value("${aws.sns.password.update}")
    private String passwordUpdateSNSTopic;

    @Value("${aws.sns.reg.topic}")
    private String registrationSNSTopic;

    @Value("${aws.sns.account.updated}")
    private String accountUpdatedSNSTopic;
    
    @Value("${aws.sns.email.service}")
    private String emailServiceSNSTopic;

    @Autowired
    SNSHandler snsHandler;

    public void sendAccountRegisteredNotification(@NotNull AccountInfo account, String cipdc) throws JsonProcessingException {
        Objects.requireNonNull(account);
        String accountRegisteredNotificationMessage = AccountInfoHandler.buildRegistrationNotificationPayload(account, cipdc);
        snsHandler.sendNotification(accountRegisteredNotificationMessage, registrationSNSTopic);
    }

    public void sendNotifyAccountInfoNotification(@NotNull AccountInfo account, String cipdc) throws JsonProcessingException {
        Objects.requireNonNull(account);
        String accountToNotify = AccountInfoHandler.prepareForProfileInfoNotification(account, cipdc);
        Map<String, MessageAttributeValue> messageAttributes = AccountInfoHandler.buildMessageAttributesForAccountInfoSNS(account);
        snsHandler.sendNotification(accountToNotify, accountInfoSNSTopic, messageAttributes);
    }
    
    public void sendAccountMergedNotification(@NotNull MergedAccountNotification mergedAccountNotification) {
        Objects.requireNonNull(mergedAccountNotification);
        String notificationMessage = new GsonBuilder().create().toJson(mergedAccountNotification);
        snsHandler.sendNotification(notificationMessage, registrationSNSTopic);
    }

    public void sendPrivateAccountUpdatedNotification(@NotNull AccountUpdatedNotification accountUpdatedNotification) {
        Objects.requireNonNull(accountUpdatedNotification);
        String notificationMessage = new GsonBuilder().create().toJson(accountUpdatedNotification);
        snsHandler.sendNotification(notificationMessage, registrationSNSTopic);
    }

    public void sendPublicAccountUpdatedNotification(@NotNull AccountUpdatedNotification accountUpdatedNotification) {
        Objects.requireNonNull(accountUpdatedNotification);
        String notificationMessage = new GsonBuilder().create().toJson(accountUpdatedNotification);
        snsHandler.sendNotification(notificationMessage, accountUpdatedSNSTopic);
    }

    public void sendPublicEmailUpdatedNotification(@NotNull EmailUpdatedNotification emailUpdatedNotification) {
        Objects.requireNonNull(emailUpdatedNotification);
        String notificationMessage = new GsonBuilder().create().toJson(emailUpdatedNotification);
        snsHandler.sendNotification(notificationMessage, accountUpdatedSNSTopic);
    }

    public void sendPrivateEmailUpdatedNotification(@NotNull EmailUpdatedNotification emailUpdatedNotification) {
        Objects.requireNonNull(emailUpdatedNotification);
        String notificationMessage = new GsonBuilder().create().toJson(emailUpdatedNotification);
        snsHandler.sendNotification(notificationMessage, registrationSNSTopic);
    }

    public void sendAspireRegistrationNotification(@NotNull AccountInfo accountInfo) throws JsonProcessingException {
        Objects.requireNonNull(accountInfo);
        String accountForAspire = AccountInfoHandler.prepareForAspireNotification(accountInfo);
        snsHandler.sendNotification(accountForAspire, aspireRegistrationSNSTopic);
    }

    public void sendPasswordUpdateNotification(@NotNull PasswordUpdateNotification passwordUpdateNotification) {
        Objects.requireNonNull(passwordUpdateNotification);
        String notificationMessage = new GsonBuilder().create().toJson(passwordUpdateNotification);
        snsHandler.sendNotification(notificationMessage, passwordUpdateSNSTopic);
    }

    public void sendResetPasswordConfirmationEmailNotification(AccountInfo accountInfo) {
        processAccountLocale(accountInfo);
        EmailNotification emailNotification = EmailNotification.buildResetPasswordNotification(accountInfo, redirectUrl);
        sendEmailNotification(emailNotification);
    }

    public void sendConfirmationEmailNotification(AccountInfo account) {
        EmailNotification emailNotification = EmailNotification.buildConfirmationEmail(account);
        sendEmailNotification(emailNotification);
    }

    public void sendRecoveryUsernameEmailNotification(UsernameRecoveryDTO usernameRecoveryDTO, AccountInfo account) {
        EmailNotification emailNotification = EmailNotification.buildRetrieveUsernameNotification(usernameRecoveryDTO, account);
        sendEmailNotification(emailNotification);
    }

    public void sendRequestResetPasswordEmailNotification(AccountInfo accountInfo, RequestResetPasswordDTO requestResetPasswordDTO) {
        processAccountLocale(accountInfo);
        EmailNotification emailNotification = EmailNotification.buildRequestResetPasswordNotification(accountInfo, requestResetPasswordDTO, resetPasswordUrl);
        sendEmailNotification(emailNotification);
    }

    private void processAccountLocale(AccountInfo account) {
        account.setLocaleName(EmailLocaleUtils.processLocaleForNotification(account.getLocaleName(), account.getCountry()));
    }

    private void sendEmailNotification(EmailNotification snsNotificationMessage) {
        Objects.requireNonNull(snsNotificationMessage);
        String emailNotificationBody = new GsonBuilder().create().toJson(snsNotificationMessage);
        snsHandler.sendNotification(emailNotificationBody, emailServiceSNSTopic);
    }

    public void sendPublicMarketingConsentUpdatedNotification(MarketingConsentUpdatedNotification marketingConsentUpdatedNotification) {
        Objects.requireNonNull(marketingConsentUpdatedNotification);
        String notificationMessage = new GsonBuilder().create().toJson(marketingConsentUpdatedNotification);
        snsHandler.sendNotification(notificationMessage, accountUpdatedSNSTopic);
    }

    public void sendPrivateMarketingConsentUpdatedNotification(MarketingConsentUpdatedNotification marketingConsentUpdatedNotification) {
        Objects.requireNonNull(marketingConsentUpdatedNotification);
        String notificationMessage = new GsonBuilder().create().toJson(marketingConsentUpdatedNotification);
        snsHandler.sendNotification(notificationMessage, registrationSNSTopic);
    }
}
