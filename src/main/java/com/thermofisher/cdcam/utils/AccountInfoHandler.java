package com.thermofisher.cdcam.utils;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.cdcam.model.AccountInfo;

/**
 * AccountInfoHandler
 */
public class AccountInfoHandler {

    public String parseToNotify(AccountInfo account) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        List<String> propertiesToRemove = new ArrayList<>();
        propertiesToRemove.add("member");
        propertiesToRemove.add("localeName");
        propertiesToRemove.add("loginProvider");
        propertiesToRemove.add("regAttempts");
        
        ObjectNode json = mapper.valueToTree(account);
        json.remove(propertiesToRemove);
        
        return mapper.writeValueAsString(json);
    }
}