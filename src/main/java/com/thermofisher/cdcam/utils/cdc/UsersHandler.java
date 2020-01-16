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

    static final Logger logger = LogManager.getLogger("CdcamApp");

    @Autowired
    CDCAccountsService cdcAccountsService;

    public UserDetails getUser(String uid) throws IOException {
        List<UserDetails> userDetails = new ArrayList<>();
        logger.info(String.format("%s user requested...", uid));
        String query = String.format("SELECT UID, profile.email, profile.firstName, profile.lastName, isRegistered FROM accounts WHERE UID = '%s' ", uid);
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

    public List<UserDetails> getUsers(List<String> uids) throws IOException {
        final int ONE_ACCOUNT = 1;
        final int TWO_ACCOUNTS = 2;
        List<UserDetails> userDetails = new ArrayList<>();
        logger.info(String.format("%s users requested...", uids.size()));
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
        }
        return userDetails;
    }
}
