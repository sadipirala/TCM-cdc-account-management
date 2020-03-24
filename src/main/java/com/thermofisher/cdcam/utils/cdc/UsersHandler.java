package com.thermofisher.cdcam.utils.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.enums.cdc.AccountTypes;
import com.thermofisher.cdcam.model.CDCAccount;
import com.thermofisher.cdcam.model.CDCSearchResponse;
import com.thermofisher.cdcam.model.Profile;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.services.CDCAccountsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class UsersHandler {

    private Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    CDCAccountsService cdcAccountsService;

    public List<UserDetails> getUsers(List<String> uids) throws IOException {
        final int ONE_ACCOUNT = 1;
        final int TWO_ACCOUNTS = 2;

        logger.info(String.format("Requested user details for multiple users. Count: %d", uids.size()));

        List<UserDetails> userDetails = new ArrayList<>();

        String joinedUids = uids.stream()
                .map(s -> "'" + s + "'")
                .collect(Collectors.joining(", "));
        String query = String.format("SELECT UID, profile.email, profile.firstName, profile.lastName, isRegistered FROM accounts WHERE UID in (%s) ", joinedUids);
        GSResponse response = cdcAccountsService.search(query, AccountTypes.FULL_LITE.getValue());

        CDCSearchResponse cdcSearchResponse = new ObjectMapper().readValue(response.getResponseText(), CDCSearchResponse.class);
        if (cdcSearchResponse.getErrorCode() == 0) {
            if (cdcSearchResponse.getTotalCount() > 0) {
                for (CDCAccount result : cdcSearchResponse.getResults()) {

                    Profile profile = result.getProfile();
                    Object isReg = result.getIsRegistered();
                    UserDetails user = UserDetails.builder()
                            .uid(result.getUID())
                            .email(profile.getEmail())
                            .firstName(profile.getFirstName())
                            .lastName(profile.getLastName())
                            .isEmailOnly(isReg == null)
                            .build();

                    UserDetails existingUser = userDetails.stream().filter(usr -> result.getUID().equals(usr.getUid())).findAny().orElse(null);
                    if (existingUser != null) {
                        if (user.getFirstName() != null) {
                            int indexOfUser = userDetails.indexOf(existingUser);
                            user.setAssociatedAccounts(TWO_ACCOUNTS);
                            userDetails.set(indexOfUser, user);
                        }
                    } else {
                        user.setAssociatedAccounts(ONE_ACCOUNT);
                        userDetails.add(user);
                    }
                }
            }
        } else {
            logger.error(String.format("An error occurred when searching users. Error: %s", cdcSearchResponse.getStatusReason()));
        }

        return userDetails;
    }
}
