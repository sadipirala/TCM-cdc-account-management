package com.thermofisher.cdcam.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.cdcam.model.AccountInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * AccountInfoHandler
 */
@Component
public class AccountInfoHandler {
    static final Logger logger = LogManager.getLogger("CdcamApp");

    public String parseToNotify(AccountInfo account) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        List<String> propertiesToRemove = new ArrayList<>();
        propertiesToRemove.add("member");
        propertiesToRemove.add("localeName");
        propertiesToRemove.add("loginProvider");
        propertiesToRemove.add("regAttempts");
        propertiesToRemove.add("uid");
        propertiesToRemove.add("password");
        
        ObjectNode json = mapper.valueToTree(account);
        logger.fatal("Get json " + mapper.writeValueAsString(json));
        json.remove(propertiesToRemove);
        json.put("uuid", account.getUid());
        
        return mapper.writeValueAsString(json);
    }
}