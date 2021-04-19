package com.thermofisher.cdcam.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.utils.AccountInfoHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AccountInfoNotificationService {

    @Autowired
    AccountInfoHandler accountHandler;

    @Autowired
    SNSHandler snsHandler;

    @Value("${aws.sns.aspire.reg.topic}")
    private String snsAspireRegistration;

    public void sendAspireRegistrationSNS(AccountInfo accountInfo) throws JsonProcessingException {
        String accountForAspire = accountHandler.prepareForAspireNotification(accountInfo);
        snsHandler.sendNotification(accountForAspire, snsAspireRegistration);
    }
}
