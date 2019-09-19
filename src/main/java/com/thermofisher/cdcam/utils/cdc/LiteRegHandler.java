package com.thermofisher.cdcam.utils.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.model.*;
import com.thermofisher.cdcam.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class LiteRegHandler {

    @Autowired
    CDCAccounts cdcAccounts;

    public List<EECUser> process(EmailList emailList) throws IOException {
        List<EECUser> users = new ArrayList<>();
        List<String> emails = emailList.getEmails();

        if (emails.size() == 0) return users;

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
                        EECUser user = EECUser.builder()
                                .uid(result.getUID())
                                .username(result.getProfile().getUsername())
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

        return users;
    }

    private EECUser liteRegisterUser(String email) throws IOException {
        GSResponse response = cdcAccounts.setLiteReg(email);

        if (response == null) return EECUser.builder()
                .uid(null)
                .email(email)
                .cdcResponseCode(500)
                .cdcResponseMessage("An error occurred during CDC Lite Registration...")
                .build();

        CDCLiteRegResponse cdcLiteRegResponse = new ObjectMapper().readValue(response.getResponseText(), CDCLiteRegResponse.class);

        if(cdcLiteRegResponse.getErrorCode() == 0) {
            return EECUser.builder()
                    .uid(cdcLiteRegResponse.getData().getUID())
                    .username(null)
                    .email(email)
                    .registered(false)
                    .cdcResponseCode(cdcLiteRegResponse.getData().getStatusCode())
                    .cdcResponseMessage(cdcLiteRegResponse.getData().getStatusReason())
                    .build();
        } else {
            String errorList = Utils.convertJavaToJsonString(cdcLiteRegResponse.getData().getValidationErrors());
            String errorDetails = String.format("%s: %s -> %s",
                    cdcLiteRegResponse.getErrorMessage(), cdcLiteRegResponse.getErrorDetails(), errorList);

            return EECUser.builder()
                    .uid(null)
                    .email(email)
                    .cdcResponseCode(cdcLiteRegResponse.getErrorCode())
                    .cdcResponseMessage(errorDetails)
                    .build();
        }
    }
}
