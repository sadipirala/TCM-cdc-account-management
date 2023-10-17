package com.thermofisher.cdcam.utils.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.enums.cdc.AccountType;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCSearchResponse;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.Profile;
import com.thermofisher.cdcam.model.dto.ProfileInfoDTO;
import com.thermofisher.cdcam.services.GigyaApi;
import com.thermofisher.cdcam.services.GigyaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class UsersHandler {

    @Value("${cdc.main.datacenter}")
    private String mainApiDomain;

    @Autowired
    GigyaApi gigyaApi;

    @Autowired
    GigyaService gigyaService;

    public List<UserDetails> getUsers(List<String> uids) throws IOException {
        final int ONE_ACCOUNT = 1;
        final int TWO_ACCOUNTS = 2;

        log.info(String.format("Requested user details for one or multiple users. Count: %d", uids.size()));

        List<UserDetails> userDetails = new ArrayList<>();

        String joinedUids = uids.stream()
                .map(s -> "'" + s + "'")
                .collect(Collectors.joining(", "));
        String query = String.format("SELECT UID, profile.email, profile.firstName, profile.lastName, isRegistered FROM accounts WHERE UID in (%s) ", joinedUids);
        GSResponse response = gigyaApi.search(query, AccountType.FULL_LITE, mainApiDomain);

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
            log.error(String.format("An error occurred when searching users. Error: %s", cdcSearchResponse.getStatusReason()));
        }

        return userDetails;
    }

    public ProfileInfoDTO getUserProfileByUID(String uid) throws IOException {
        log.info("Requested user profile by UID.");

        ProfileInfoDTO profileInfoDTO = ProfileInfoDTO.builder().build();
        AccountInfo accountInfo;
        try {
            accountInfo = gigyaService.getAccountInfo(uid);
            profileInfoDTO = ProfileInfoDTO.build(accountInfo);
        } catch (CustomGigyaErrorException e) {
            log.error(String.format("An error occurred when searching user. Error: %s", e.getMessage()));
            profileInfoDTO = null;
        }

        return profileInfoDTO;
    }
}
