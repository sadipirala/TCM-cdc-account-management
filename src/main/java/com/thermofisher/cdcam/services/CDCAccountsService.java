package com.thermofisher.cdcam.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.CDCAccount;
import com.thermofisher.cdcam.model.CDCData;
import com.thermofisher.cdcam.model.CDCProfile;
import com.thermofisher.cdcam.model.CDCThermofisher;
import com.thermofisher.cdcam.model.dto.FedUserUpdateDTO;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * CDCAccountsService
 */
@Configuration
public class CDCAccountsService {
    private final int SUCCESS_CODE = 0;
    static final Logger logger = LogManager.getLogger("CdcamApp");
    private final AccountBuilder accountBuilder = new AccountBuilder();

    @Autowired
    CDCAccounts cdcAccounts;

    public AccountInfo getAccountInfo(String uid) {
        GSResponse response = cdcAccounts.getAccount(uid);
        if (response.getErrorCode() == 0) {
            GSObject obj = response.getData();
            return accountBuilder.getAccountInfo(obj);
        } else {
            logger.error(response.getErrorDetails());
            return null;
        }
    }

    public ObjectNode updateFedUser(FedUserUpdateDTO user) throws JsonProcessingException {
        CDCThermofisher thermofisher = CDCThermofisher.builder().regStatus(user.getRegStatus()).build();
        CDCData data = CDCData.builder().thermofisher(thermofisher).build();
        CDCProfile profile = CDCProfile.builder().username(user.getUsername()).build();
        CDCAccount cdcaccount = CDCAccount.builder().data(data).profile(profile).build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String dataJson = mapper.writeValueAsString(cdcaccount.getData());
        String profileJson = mapper.writeValueAsString(cdcaccount.getProfile());

        GSResponse response = cdcAccounts.setUserInfo(user.getUid(), dataJson, profileJson);

        ObjectNode json = JsonNodeFactory.instance.objectNode();
        if (response.getErrorCode() == SUCCESS_CODE) {
            json.put("code", HttpStatus.OK.value());
        } else {
            json.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        json.put("message", response.getErrorMessage());
        
        return json;
    }
}