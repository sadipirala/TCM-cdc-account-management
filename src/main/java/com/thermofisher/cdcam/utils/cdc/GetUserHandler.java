package com.thermofisher.cdcam.utils.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.enums.cdc.AccountTypes;
import com.thermofisher.cdcam.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class GetUserHandler {

    static final Logger logger = LogManager.getLogger("CdcamApp");

    @Autowired
    CDCAccounts cdcAccounts;

    public UserDetails getUser(String uid) throws IOException {
        List<UserDetails> userDetails = new ArrayList<>();
        logger.info(String.format("%s user requested...", uid));
        String query = String.format("SELECT UID, profile.email, profile.firstName,profile.lastName FROM accounts WHERE UID = '%s' ", uid);
        GSResponse response = cdcAccounts.search(query, AccountTypes.FULL_LITE.getValue());

        CDCSearchResponse cdcSearchResponse = new ObjectMapper().readValue(response.getResponseText(), CDCSearchResponse.class);
        if (cdcSearchResponse.getErrorCode() == 0) {
            if (cdcSearchResponse.getTotalCount() > 0) {
                for (CDCResult result : cdcSearchResponse.getResults()) {

                    CDCProfile profile = result.getProfile();

                    UserDetails user = UserDetails.builder()
                            .uid(result.getUID())
                            .email(profile.getEmail())
                            .firstName(profile.getFirstName())
                            .lastName(profile.getLastName())
                            .build();

                    UserDetails existingUser = userDetails.stream().filter(usr -> result.getUID().equals(usr.getUid())).findAny().orElse(null);
                    if (existingUser != null) {
                        if (user.getFirstName() != null) {

                            user.setAssociatedAccounts(2);
                            userDetails.set(0, user);
                        }
                    } else {
                        user.setAssociatedAccounts(1);
                        userDetails.add(user);
                    }
                }
            }
        }
        return userDetails.size() > 0 ? userDetails.get(0) : null;
    }
}
