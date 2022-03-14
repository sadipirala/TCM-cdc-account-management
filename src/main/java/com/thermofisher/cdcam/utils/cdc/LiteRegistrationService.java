package com.thermofisher.cdcam.utils.cdc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gigya.socialize.GSKeyNotFoundException;
import com.thermofisher.cdcam.enums.ResponseCode;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EECUserV1;
import com.thermofisher.cdcam.model.EECUserV2;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CDCSearchResponse;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.SearchResponse;
import com.thermofisher.cdcam.utils.Utils;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LiteRegistrationService {
    private Logger logger = LogManager.getLogger(this.getClass());
    private final int GENERIC_ERROR_CODE = 500;
    private final String ERROR_MSG = "Something went wrong, please contact the system administrator.";

    @Value("${cdc.main.datacenter.name}")
    public String mainDataCenterName;

    @Autowired
    CDCResponseHandler cdcResponseHandler;

    public List<EECUserV2> registerEmailAccounts(EmailList emailList) throws IOException {
        logger.info("Lite registration initiated. %d users requested.", emailList.getEmails().size());

        List<EECUserV2> emailAccounts = new ArrayList<>();
        List<String> emails = emailList.getEmails();

        if (Utils.hasNullOrEmptyValues(emails)) {
            String error = "Email list has null or empty values.";
            throw new IllegalArgumentException(error);
        }

        for (String email : emails) {
            try {
                EECUserV2 user = registerEmailAccount(email);
                emailAccounts.add(user);
            } catch (CustomGigyaErrorException e) {
                logger.error(String.format("Error with email: %s. CDC Error code: %d. CDC Error message: %s", email, e.getErrorCode(), e.getMessage()));
                EECUserV2 invalidEECUser = EECUserV2.buildInvalidUser(email, e.getErrorCode(), e.getMessage());
                emailAccounts.add(invalidEECUser);
            } catch (Throwable e) {
                logger.error(String.format("Error with email: %s. Cause: %s", email, e));
                EECUserV2 invalidEECUser = EECUserV2.buildInvalidUser(email, GENERIC_ERROR_CODE, ERROR_MSG);
                emailAccounts.add(invalidEECUser);
            }
        }

        logger.info(String.format("%d lite registration users processed.", emailAccounts.size()));
        return emailAccounts;
    }

    public EECUserV2 registerEmailAccount(String email) throws IOException, GSKeyNotFoundException, CustomGigyaErrorException {
        SearchResponse searchResponse = cdcResponseHandler.searchInBothDC(email);
        CDCSearchResponse cdcSearchResponse = searchResponse.getCdcSearchResponse();
        List<CDCAccount> accounts = cdcSearchResponse.getResults();

        EECUserV2 user;
        if (accounts.size() == 0) {
            user = createLiteAccount(email);
            user.setDataCenter(mainDataCenterName);
        } else {
            logger.info(String.format("%s already exists, getting full registered account, lite otherwise.", email));
            CDCAccount account = findFullRegisteredAccountOrFirstFrom(accounts);
            user = EECUserV2.buildFromExistingAccount(account);
            user.setDataCenter(searchResponse.getDataCenter().getValue());
        }

        return user;
    }

    private EECUserV2 createLiteAccount(String email) throws GSKeyNotFoundException, IOException, CustomGigyaErrorException {
        logger.info(String.format("Registering lite account: %s", email));
        CDCResponseData cdcResponseData = cdcResponseHandler.registerLiteAccount(email);
        String UID = cdcResponseData.getUID();
        return EECUserV2.buildLiteRegisteredUser(UID, email);
    }

    private CDCAccount findFullRegisteredAccountOrFirstFrom(List<CDCAccount> accounts) {
        return accounts.stream().filter(account -> BooleanUtils.isTrue(account.getIsRegistered())).findFirst().orElse(accounts.get(0));
    }

    @Deprecated
    public List<EECUser> createLiteAccountsV1(EmailList emailList) throws IOException {
        List<EECUserV2> liteRegisteredUsersV2 = registerEmailAccounts(emailList);
        List<EECUser> liteRegisteredUsersV1 = new ArrayList<>();

        for(EECUserV2 eecuser : liteRegisteredUsersV2) {
            EECUser eecUserV1 = EECUserV1.build(eecuser);
            
            if (eecUserV1.getResponseCode() == ResponseCode.LOGINID_ALREADY_EXISTS.getValue()) {
                eecUserV1.setResponseCode(ResponseCode.SUCCESS.getValue());
            }

            liteRegisteredUsersV1.add(eecUserV1);
        }
        return liteRegisteredUsersV1;
    }
}
