package com.thermofisher.cdcam.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AccountInfoNotificationService {
    private Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    AccountInfoHandler accountHandler;

    @Autowired
    SNSHandler snsHandler;

    @Value("${aws.sns.aspire.reg.topic}")
    private String snsAspireRegistration;

    public void sendAspireRegistrationSNS(AccountInfo accountInfo) throws JsonProcessingException {
        String accountForAspire = accountHandler.prepareForAspireNotification(accountInfo);
        boolean AspireSNSSentCorrectly = snsHandler.sendSNSNotification(accountForAspire, snsAspireRegistration);

        if (AspireSNSSentCorrectly) {
            logger.info(String.format("Aspire Registration Notification sent successfully. UID: %s", accountInfo.getUid()));
        } else {
            logger.error(String.format("Posting SNS Topic (%s) failed for UID: %s.", snsAspireRegistration, accountInfo.getUid()));
        }
    }
}
