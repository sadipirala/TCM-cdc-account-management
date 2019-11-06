package com.thermofisher.cdcam.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpdateAccountService implements Runnable {
    static final Logger logger = LogManager.getLogger("CdcamApp");
    private static final int SUCCESS_CODE = 0;
    private String emailAddress;
    private String uid;

    public UpdateAccountService(String uid, String emailAddress) {
        this.uid = uid;
        this.emailAddress = emailAddress;
    }

    @Override
    public void run() {
        try {
            logger.fatal("thread.run");
            CDCAccounts cdcAccounts = new CDCAccounts();
            Thermofisher thermofisher = Thermofisher.builder()
                    .legacyEmail(emailAddress)
                    .legacyUsername(emailAddress)
                    .build();
            Data data = Data.builder()
                    .thermofisher(thermofisher)
                    .build();
            Profile profile = Profile.builder()
                    .username(emailAddress)
                    .build();

            ObjectMapper mapper = new ObjectMapper();

            String dataJsonString = mapper.writeValueAsString(data);
            String profileJsonString = mapper.writeValueAsString(profile);

            logger.fatal("cdcAccounts.setUserInfo");
            GSResponse response = cdcAccounts.setUserInfo(uid, dataJsonString, profileJsonString);
            logger.fatal("gigya response code: " + response.getErrorCode());
            if (response.getErrorCode() == SUCCESS_CODE) {
                logger.fatal("uid: " + uid + " updated.");
            } else {
                logger.fatal("uid: " + uid + " failed. error Code: " + response.getLog());
            }
        } catch (Exception e) {
            logger.fatal("error message: " + e.getMessage());
        }
    }
}
