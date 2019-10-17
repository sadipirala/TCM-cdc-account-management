package com.thermofisher.cdcam.utils;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.cdcam.model.AccountInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;


/**
 * AccountInfoHandler
 */
@Component
public class AccountInfoHandler {
    static final Logger logger = LogManager.getLogger("CdcamApp");

    public String parseToNotify(AccountInfo account) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        List<String> propertiesToRemove = new ArrayList<>();
        if(account.getMember() != null) propertiesToRemove.add("member");
        if(account.getLocaleName() != null) propertiesToRemove.add("localeName");
        if(account.getLoginProvider() != null) propertiesToRemove.add("loginProvider");
        propertiesToRemove.add("regAttempts");
        propertiesToRemove.add("uid");
        
        ObjectNode json = mapper.valueToTree(account);
        logger.fatal("Get json " + mapper.writeValueAsString(json));
        json.remove(propertiesToRemove);
        json.put("uuid", account.getUid());
        
        return mapper.writeValueAsString(json);
    }
}