package com.thermofisher.cdcam.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

public class UpdateAccountService implements Runnable {

    static final Logger logger = LogManager.getLogger("CdcamApp");
    private static final int SUCCESS_CODE = 0;
    private String emailAddress;
    private String uid;

    public UpdateAccountService(String emailAddress, String uid) {
        this.emailAddress = emailAddress;
        this.uid = uid;
    }

    @Override
    public void run() {
        try {
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

            GSResponse response = cdcAccounts.setUserInfo(uid,dataJsonString,profileJsonString);
            if (response.getErrorCode() == SUCCESS_CODE) {
                logger.info("uid: " + uid + " updated.");
            } else {
                logger.error("uid: " + uid + " failed. error Code: "+response.getErrorCode() );
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }
}
