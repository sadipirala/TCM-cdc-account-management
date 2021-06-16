package com.thermofisher.cdcam.utils.cdc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.thermofisher.cdcam.enums.cdc.AccountType;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EECUserV1;
import com.thermofisher.cdcam.model.EECUserV2;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CDCSearchResponse;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.utils.Utils;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LiteRegHandler {
    private Logger logger = LogManager.getLogger(this.getClass());
    private final int GENERIC_ERROR_CODE = 500;
    private final String ERROR_MSG = "Something went wrong, please contact the system administrator.";

    @Value("${eec.request.limit}")
    public int requestLimit;

    @Autowired
    CDCResponseHandler cdcResponseHandler;

    public List<EECUser> createLiteAccountsV2(EmailList emailList) throws IOException {
        List<EECUser> liteRegisteredUsers = new ArrayList<>();
        List<String> emails = emailList.getEmails();
        logger.info("EEC account processing initiated.");
        logger.info(String.format("%d EEC users requested.", emails.size()));

        if (Utils.hasNullOrEmptyValues(emails)) {
            String error = "Email list has null or empty values.";
            throw new IllegalArgumentException(error);
        }

        for (String email : emails) {
            String query = String.format("SELECT * FROM accounts WHERE profile.username CONTAINS '%1$s' OR profile.email CONTAINS '%1$s'", email);
            
            try {
                CDCSearchResponse searchResponse = cdcResponseHandler.search(query, AccountType.FULL_LITE);
                List<CDCAccount> accounts = searchResponse.getResults();

                if (accounts.size() == 0) {
                    logger.info(String.format("Registering lite account for: %s", email));
                    EECUser user = liteRegisterUser(email);
                    liteRegisteredUsers.add(user);
                } else {
                    logger.info(String.format("%s already exists, getting full registered account, lite otherwise.", email));
                    CDCAccount account = findFullRegisteredAccountOrFirstFromList(accounts);
                    int responseCode = searchResponse.getStatusCode();
                    String responseMessage = searchResponse.getStatusReason();
                    EECUser user = buildValidEECUser(account, email, responseCode, responseMessage);
                    liteRegisteredUsers.add(user);
                }
            } catch (CustomGigyaErrorException e) {
                logger.error(String.format("Error with email: %s. CDC Error code: %d. CDC Error message: %s", email, e.getErrorCode(), e.getMessage()));
                EECUser invalidEECUser = buildInvalidEECUser(email, e.getErrorCode(), e.getMessage());
                liteRegisteredUsers.add(invalidEECUser);
            } catch (Throwable e) {
                logger.error(String.format("Error with email: %s. Cause: %s", email, e));
                EECUser invalidEECUser = buildInvalidEECUser(email, GENERIC_ERROR_CODE, ERROR_MSG);
                liteRegisteredUsers.add(invalidEECUser);
            }
        }

        logger.info(String.format("%d EEC users processed.", liteRegisteredUsers.size()));
        return liteRegisteredUsers;
    }

    public List<EECUser> createLiteAccountsV1(EmailList emailList) throws IOException {
        List<EECUser> liteRegisteredUsersV2 = createLiteAccountsV2(emailList);
        List<EECUser> liteRegisteredUsersV1 = new ArrayList<>();

        for(EECUser eecuser : liteRegisteredUsersV2) {
            liteRegisteredUsersV1.add(EECUserV1.build((EECUserV2) eecuser));
        }
        return liteRegisteredUsersV1;
    }

    private EECUser liteRegisterUser(String email) throws IOException, CustomGigyaErrorException {
        CDCResponseData cdcResponseData = cdcResponseHandler.liteRegisterUser(email);
        CDCAccount account = new CDCAccount();
        account.setUID(cdcResponseData.getUID());
        account.setIsRegistered(false);
        account.setIsActive(false);

        return buildValidEECUser(account, email, cdcResponseData.getStatusCode(), cdcResponseData.getStatusReason());
    }

    private EECUser buildValidEECUser(CDCAccount account, String email, int responseCode, String responseMessage) {
        String username = Objects.isNull(account.getProfile()) ? null : account.getProfile().getUsername();
        boolean isActive = Objects.isNull(account.getIsActive()) ? false : account.getIsActive();
        boolean isRegistered = Objects.isNull(account.getIsRegistered()) ? false : account.getIsRegistered();

        return EECUserV2.builder()
            .uid(account.getUID())
            .username(username)
            .email(email)
            .isRegistered(isRegistered)
            .isActive(isActive)
            .responseCode(responseCode)
            .responseMessage(responseMessage)
            .build();
    }

    private EECUser buildInvalidEECUser(String email, int errorCode, String errorMessage) {
        return EECUserV2.builder()
            .email(email)
            .responseCode(errorCode)
            .responseMessage(errorMessage)
            .build();
    }

    private CDCAccount findFullRegisteredAccountOrFirstFromList(List<CDCAccount> accounts) {
        return accounts.stream().filter(account -> BooleanUtils.isTrue(account.getIsRegistered())).findFirst().orElse(accounts.get(0));
    }
}
