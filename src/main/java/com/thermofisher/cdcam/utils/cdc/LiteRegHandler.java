package com.thermofisher.cdcam.utils.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.enums.cdc.AccountTypes;
import com.thermofisher.cdcam.model.*;
import com.thermofisher.cdcam.services.CDCAccountsService;
import com.thermofisher.cdcam.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class LiteRegHandler {

    private Logger logger = LogManager.getLogger(this.getClass());

    @Value("${eec.request.limit}")
    public int requestLimit;

    @Autowired
    CDCAccountsService cdcAccountsService;

    public List<EECUser> process(EmailList emailList) throws IOException {
        logger.info("EEC account processing initiated.");
        List<EECUser> users = new ArrayList<>();
        List<String> emails = emailList.getEmails();

        if (emails.size() == 0) {
            logger.warn("No accounts were sent in the request.");
            return users;
        }

        logger.info(String.format("%d EEC users requested.", emails.size()));

        for (String email: emails) {
            if (email == null) {
                EECUser failedSearchUser = EECUser.builder()
                        .uid(null)
                        .username(null)
                        .email(null)
                        .responseCode(500)
                        .responseMessage("User requested has null email value...")
                        .build();

                users.add(failedSearchUser);
                continue;
            }

            String query = String.format("SELECT * FROM accounts WHERE profile.username CONTAINS '%1$s' OR profile.email CONTAINS '%1$s'", email);
            GSResponse response = cdcAccountsService.search(query,AccountTypes.FULL_LITE.getValue());

            if (response == null) {
                logger.error(String.format("No response from CDC when searching '%s'.", email));

                EECUser failedSearchUser = EECUser.builder()
                        .uid(null)
                        .username(null)
                        .email(email)
                        .responseCode(500)
                        .responseMessage("An error occurred when retrieving user's info")
                        .build();

                users.add(failedSearchUser);
                continue;
            }

            CDCSearchResponse cdcSearchResponse = new ObjectMapper().readValue(response.getResponseText(), CDCSearchResponse.class);

            if (cdcSearchResponse.getErrorCode() == 0) {
                if (cdcSearchResponse.getTotalCount() > 0) {
                    for (CDCAccount result: cdcSearchResponse.getResults()) {
                        Profile profile = result.getProfile();
                        Object isReg = result.getIsRegistered();
                        EECUser user = EECUser.builder()
                                .uid(result.getUID())
                                .username((profile != null) ? profile.getUsername() : null)
                                .email(email)
                                .registered(isReg == null?false:(boolean)isReg)
                                .responseCode(cdcSearchResponse.getStatusCode())
                                .responseMessage(cdcSearchResponse.getStatusReason())
                                .build();

                        users.add(user);
                    }
                } else {
                    users.add(liteRegisterUser(email));
                }
            } else {
                int errorCode = cdcSearchResponse.getErrorCode();
                String errorMessage = cdcSearchResponse.getStatusReason();

                EECUser failedSearchUser = EECUser.builder()
                        .uid(null)
                        .username(null)
                        .email(email)
                        .responseCode(errorCode)
                        .responseMessage(errorMessage)
                        .build();

                users.add(failedSearchUser);

                logger.error(String.format("Failed searching account with email: %s. CDC Error code: %d. CDC Error message: %s", email, errorCode, errorMessage));
            }
        }

        logger.info(String.format("%d EEC users processed.", users.size()));
        return users;
    }

    private EECUser liteRegisterUser(String email) throws IOException {
        GSResponse response = cdcAccountsService.setLiteReg(email);

        if (response == null) {
            logger.error(String.format("An error occurred during CDC email only registration for '%s'", email));
            return EECUser.builder()
                    .uid(null)
                    .email(email)
                    .responseCode(500)
                    .responseMessage("An error occurred during CDC email only registration...")
                    .build();
        }

        CDCResponseData cdcData = new ObjectMapper().readValue(response.getResponseText(), CDCResponseData.class);

        if(response.getErrorCode() == 0) {
            logger.info(String.format("Successful email only registration for '%s'", email));
            return EECUser.builder()
                    .uid(cdcData.getUID())
                    .username(null)
                    .email(email)
                    .registered(false)
                    .responseCode(cdcData.getStatusCode())
                    .responseMessage(cdcData.getStatusReason())
                    .build();
        } else {
            String errorList = Utils.convertJavaToJsonString(cdcData.getValidationErrors());
            String errorDetails = String.format("%s: %s -> %s",
                    response.getErrorMessage(), response.getErrorDetails(), errorList);

            logger.error(String.format("Email only registration failed. Email: %s. Error details: %s", email, errorDetails));

            return EECUser.builder()
                    .uid(null)
                    .email(email)
                    .responseCode(response.getErrorCode())
                    .responseMessage(errorDetails)
                    .build();
        }
    }
}
