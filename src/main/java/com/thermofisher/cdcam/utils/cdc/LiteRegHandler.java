package com.thermofisher.cdcam.utils.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.model.*;
import com.thermofisher.cdcam.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class LiteRegHandler {

    static final Logger logger = LogManager.getLogger("CdcamApp");

    @Autowired
    CDCAccounts cdcAccounts;

    public List<EECUser> process(EmailList emailList) throws IOException {
        List<EECUser> users = new ArrayList<>();
        List<String> emails = emailList.getEmails();

        if (emails.size() == 0) return users;

        logger.info(String.format("%d EEC users requested...", emails.size()));

        for (String email: emails) {
            GSResponse response = cdcAccounts.searchByEmail(email);

            if (response == null) {
                EECUser failedSearchUser = EECUser.builder()
                        .uid(null)
                        .username(null)
                        .email(email)
                        .cdcResponseCode(500)
                        .cdcResponseMessage("An error occurred during CDC User Search...")
                        .build();

                users.add(failedSearchUser);
                continue;
            }

            CDCSearchResponse cdcSearchResponse = new ObjectMapper().readValue(response.getResponseText(), CDCSearchResponse.class);

            if (cdcSearchResponse.getErrorCode() == 0) {
                if (cdcSearchResponse.getTotalCount() > 0) {
                    for (CDCResult result: cdcSearchResponse.getResults()) {

                        CDCProfile profile = result.getProfile();

                        EECUser user = EECUser.builder()
                                .uid(result.getUID())
                                .username((profile != null) ? profile.getUsername() : null)
                                .email(email)
                                .registered(result.isRegistered())
                                .cdcResponseCode(cdcSearchResponse.getStatusCode())
                                .cdcResponseMessage(cdcSearchResponse.getStatusReason())
                                .build();

                        users.add(user);
                    }
                } else {
                    users.add(liteRegisterUser(email));
                }
            } else {
                EECUser failedSearchUser = EECUser.builder()
                        .uid(null)
                        .username(null)
                        .email(email)
                        .cdcResponseCode(cdcSearchResponse.getErrorCode())
                        .cdcResponseMessage(cdcSearchResponse.getStatusReason())
                        .build();

                users.add(failedSearchUser);
            }
        }

        logger.info(String.format("%d EEC users returned...", users.size()));
        return users;
    }

    private EECUser liteRegisterUser(String email) throws IOException {
        GSResponse response = cdcAccounts.setLiteReg(email);

        if (response == null) {
            logger.error(String.format("An error occurred during CDC Lite Registration for '%s'", email));
            return EECUser.builder()
                    .uid(null)
                    .email(email)
                    .cdcResponseCode(500)
                    .cdcResponseMessage("An error occurred during CDC Lite Registration...")
                    .build();
        }

        CDCData cdcData = new ObjectMapper().readValue(response.getResponseText(), CDCData.class);

        if(response.getErrorCode() == 0) {
            logger.info(String.format("New email only registration for '%s'", email));
            return EECUser.builder()
                    .uid(cdcData.getUID())
                    .username(null)
                    .email(email)
                    .registered(false)
                    .cdcResponseCode(cdcData.getStatusCode())
                    .cdcResponseMessage(cdcData.getStatusReason())
                    .build();
        } else {
            logger.error(String.format("Email only registration failed for '%s'", email));
            String errorList = Utils.convertJavaToJsonString(cdcData.getValidationErrors());
            String errorDetails = String.format("%s: %s -> %s",
                    response.getErrorMessage(), response.getErrorDetails(), errorList);

            return EECUser.builder()
                    .uid(null)
                    .email(email)
                    .cdcResponseCode(response.getErrorCode())
                    .cdcResponseMessage(errorDetails)
                    .build();
        }
    }
}
