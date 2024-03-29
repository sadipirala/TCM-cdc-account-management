package com.thermofisher.cdcam.utils.cdc;

import com.gigya.socialize.GSKeyNotFoundException;
import com.thermofisher.cdcam.enums.ResponseCode;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EECUserV1;
import com.thermofisher.cdcam.model.EECUserV2;
import com.thermofisher.cdcam.model.EECUserV3;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CDCSearchResponse;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.SearchResponse;
import com.thermofisher.cdcam.model.dto.LiteAccountDTO;
import com.thermofisher.cdcam.services.GigyaService;
import com.thermofisher.cdcam.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LiteRegistrationService {
    private final int GENERIC_ERROR_CODE = 500;
    private final int BAD_REQUEST_ERROR_CODE = 400;
    private final String ERROR_MSG = "Something went wrong, please contact the system administrator.";

    @Value("${is-email-validation-enabled}")
    private boolean isEmailValidationEnabled;

    @Value("${cdc.main.datacenter.name}")
    public String mainDataCenterName;

    @Value("${identity.registration.oidc.rp.redirect_uri}")
    private String registrationRedirectionUri;

    @Value("${eec.v3.request.limit}")
    public int requestLimitV3;

    @Autowired
    GigyaService gigyaService;

    public List<EECUserV3> registerLiteAccounts(List<LiteAccountDTO> liteAccountList) throws IllegalArgumentException {
        log.info(String.format("Lite registration initiated. %d users requested", liteAccountList.size()));
        String errorMessage = "";

        if (Utils.isNullOrEmpty(liteAccountList)) {
            errorMessage = "No users requested.";
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        } else if (liteAccountList.size() > requestLimitV3) {
            errorMessage = String.format("Requested users exceed request limit: %s.", requestLimitV3);
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        List<EECUserV3> liteAccounts = new ArrayList<>();

        for (LiteAccountDTO account : liteAccountList) {
            try {
                if (Utils.isNullOrEmpty(account.getEmail())) {
                    throw new IllegalArgumentException("Email is null or empty.");
                }
                if (!Utils.isValidEmail(account.getEmail())) {
                    throw new IllegalArgumentException("Email is invalid.");
                }
                EECUserV3 user = registerLiteAccount(account);
                liteAccounts.add(user);
            } catch (CustomGigyaErrorException e) {
                log.error(String.format("Error with email: %s. CDC Error code: %d. CDC Error message: %s", account.getEmail(), e.getErrorCode(), e.getMessage()));
                EECUserV3 invalidEECUser = EECUserV3.buildInvalidUser(account.getEmail(), e.getErrorCode(), e.getMessage());
                liteAccounts.add(invalidEECUser);
            } catch (IllegalArgumentException e) {
                log.error(String.format("Error with email: %s. Cause: %s", account.getEmail(), e.getMessage()));
                EECUserV3 invalidEECUser = EECUserV3.buildInvalidUser(account.getEmail(), BAD_REQUEST_ERROR_CODE, e.getMessage());
                liteAccounts.add(invalidEECUser);
            } catch (Throwable e) {
                log.error(String.format("Error with email: %s. Cause: %s", account.getEmail(), e.getMessage()));
                EECUserV3 invalidEECUser = EECUserV3.buildInvalidUser(account.getEmail(), GENERIC_ERROR_CODE, ERROR_MSG);
                liteAccounts.add(invalidEECUser);
            }
        }

        return liteAccounts;
    }

    public List<EECUserV2> registerEmailAccounts(EmailList emailList) throws IOException {
        log.info(String.format("Lite registration initiated. %d users requested.", emailList.getEmails().size()));

        List<EECUserV2> emailAccounts = new ArrayList<>();
        List<String> emails = emailList.getEmails();

        if (Utils.hasNullOrEmptyValues(emails)) {
            String error = "Email list has null or empty values.";
            throw new IllegalArgumentException(error);
        }

        for (String email : emails) {
            try {
                if (isEmailValidationEnabled && !Utils.isValidEmail(email)) {
                    String error = "Email is invalid.";
                    throw new IllegalArgumentException(error);
                }
                EECUserV2 user = registerEmailAccount(email);
                emailAccounts.add(user);
            } catch (CustomGigyaErrorException e) {
                log.error(String.format("Error with email: %s. CDC Error code: %d. CDC Error message: %s", email, e.getErrorCode(), e.getMessage()));
                EECUserV2 invalidEECUser = EECUserV2.buildInvalidUser(email, e.getErrorCode(), e.getMessage());
                emailAccounts.add(invalidEECUser);
            } catch (IllegalArgumentException e) {
                log.error(String.format("Error with email: %s. Cause: %s", email, e));
                EECUserV2 invalidEECUser = EECUserV2.buildInvalidUser(email, BAD_REQUEST_ERROR_CODE, e.getMessage());
                emailAccounts.add(invalidEECUser);
            } catch (Throwable e) {
                log.error(String.format("Error with email: %s. Cause: %s", email, e));
                EECUserV2 invalidEECUser = EECUserV2.buildInvalidUser(email, GENERIC_ERROR_CODE, ERROR_MSG);
                emailAccounts.add(invalidEECUser);
            }
        }

        log.info(String.format("%d lite registration users processed.", emailAccounts.size()));
        return emailAccounts;
    }

    public EECUserV2 registerEmailAccount(String email) throws IOException, GSKeyNotFoundException, CustomGigyaErrorException {
        SearchResponse searchResponse = gigyaService.searchInBothDC(email);
        CDCSearchResponse cdcSearchResponse = searchResponse.getCdcSearchResponse();
        List<CDCAccount> accounts = cdcSearchResponse.getResults();

        EECUserV2 user;
        if (accounts.size() == 0) {
            user = createLiteAccount(email);
            user.setDataCenter(mainDataCenterName);
        } else {
            log.info(String.format("%s already exists, getting full registered account, lite otherwise.", email));
            CDCAccount account = findFullRegisteredAccountOrFirstFrom(accounts);
            user = EECUserV2.buildFromExistingAccount(account);
            user.setDataCenter(searchResponse.getDataCenter().getValue());
        }

        return user;
    }

    private EECUserV3 registerLiteAccount(LiteAccountDTO liteAccountDTO) throws CustomGigyaErrorException, IOException, GSKeyNotFoundException, JSONException {
        SearchResponse searchResponse = gigyaService.searchInBothDC(liteAccountDTO.getEmail());
        CDCSearchResponse cdcSearchResponse = searchResponse.getCdcSearchResponse();
        List<CDCAccount> accounts = cdcSearchResponse.getResults();

        EECUserV3 user;
        if (accounts.size() == 0) {
            user = createLiteAccount(liteAccountDTO);
        } else {
            log.info(String.format("%s already exists, getting full registered account, lite otherwise.", liteAccountDTO.getEmail()));
            CDCAccount account = findFullRegisteredAccountOrFirstFrom(accounts);
            user = EECUserV3.buildFromExistingAccount(account);
        }

        return user;
    }

    private EECUserV3 createLiteAccount(LiteAccountDTO liteAccountDTO) throws GSKeyNotFoundException, CustomGigyaErrorException, IOException, JSONException {
        log.info(String.format("Registering lite account: %s", liteAccountDTO.getEmail()));
        CDCResponseData cdcResponseData = gigyaService.registerLiteAccount(liteAccountDTO);
        String UID = cdcResponseData.getUID();
        return EECUserV3.buildLiteRegisteredUser(UID, liteAccountDTO.getEmail(), registrationRedirectionUri);
    }

    private EECUserV2 createLiteAccount(String email) throws GSKeyNotFoundException, IOException, CustomGigyaErrorException {
        log.info(String.format("Registering lite account: %s", email));
        CDCResponseData cdcResponseData = gigyaService.registerLiteAccount(email);
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

        for (EECUserV2 eecuser : liteRegisteredUsersV2) {
            EECUser eecUserV1 = EECUserV1.build(eecuser);

            if (eecUserV1.getResponseCode() == ResponseCode.LOGINID_ALREADY_EXISTS.getValue()) {
                eecUserV1.setResponseCode(ResponseCode.SUCCESS.getValue());
            }

            liteRegisteredUsersV1.add(eecUserV1);
        }
        return liteRegisteredUsersV1;
    }
}
