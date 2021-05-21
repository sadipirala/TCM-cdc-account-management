package com.thermofisher.cdcam.services;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.GsonBuilder;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.notifications.AccountUpdatedNotification;
import com.thermofisher.cdcam.model.notifications.MergedAccountNotification;
import com.thermofisher.cdcam.utils.AccountInfoHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Value("${aws.sns.aspire.reg.topic}")
    private String aspireRegistrationSNSTopic;

    @Value("${aws.sns.reg.topic}")
    private String registrationSNSTopic;

    @Autowired
    AccountInfoHandler accountHandler;

    @Autowired
    SNSHandler snsHandler;
    
    public void sendAccountMergedNotification(@NotNull MergedAccountNotification mergedAccountNotification) {
        Objects.requireNonNull(mergedAccountNotification);
        String notificationMessage = new GsonBuilder().create().toJson(mergedAccountNotification);
        snsHandler.sendNotification(notificationMessage, registrationSNSTopic);
    }

    public void sendAccountUpdatedNotification(@NotNull AccountUpdatedNotification accountUpdatedNotification) {
        Objects.requireNonNull(accountUpdatedNotification);
        String notificationMessage = new GsonBuilder().create().toJson(accountUpdatedNotification);
        snsHandler.sendNotification(notificationMessage, registrationSNSTopic);
    }

    public void sendAspireRegistrationNotification(@NotNull AccountInfo accountInfo) throws JsonProcessingException {
        Objects.requireNonNull(accountInfo);
        String accountForAspire = accountHandler.prepareForAspireNotification(accountInfo);
        snsHandler.sendNotification(accountForAspire, aspireRegistrationSNSTopic);
    }
}
