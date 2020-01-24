package com.thermofisher.cdcam.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * AccountInfoHandler
 */
@Component
public class AccountInfoHandler {

    public String prepareForProfileInfoNotification(AccountInfo account) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        List<String> propertiesToRemove = new ArrayList<>();
        propertiesToRemove.add("member");
        propertiesToRemove.add("localeName");
        propertiesToRemove.add("loginProvider");
        propertiesToRemove.add("regAttempts");
        propertiesToRemove.add("uid");
        propertiesToRemove.add("password");
        
        ObjectNode json = mapper.valueToTree(account);
        json.remove(propertiesToRemove);
        json.put("uuid", account.getUid());
        
        return mapper.writeValueAsString(json);
    }

    public String prepareForGRPNotification(AccountInfo account) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        List<String> propertiesToRemove = new ArrayList<>();
        propertiesToRemove.add("loginProvider");
        
        ObjectNode json = mapper.valueToTree(account);
        json.remove(propertiesToRemove);

        return mapper.writeValueAsString(json);
    }

    public String prepareProfileForRegistration(Profile profile) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> propertiesToRemove = new ArrayList<>();
        propertiesToRemove.add("username");
        propertiesToRemove.add("email");

        ObjectNode json = mapper.valueToTree(profile);
        json.remove(propertiesToRemove);

        return mapper.writeValueAsString(json);
    }
}