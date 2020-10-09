package com.thermofisher.cdcam.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.cdcam.model.AccountInfo;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * AccountInfoHandlerTests
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AccountInfoHandler.class)
public class AccountInfoHandlerTests {
    private final AccountInfoHandler accountHandler = new AccountInfoHandler();
    private ObjectMapper mapper = new ObjectMapper();

    @After
    public void after() { 
        mapper.setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS);
    }
    
    private String prepareJsonForNotification(ObjectNode json) throws JsonProcessingException {
        List<String> propertiesToRemove = new ArrayList<>();
        propertiesToRemove.add("member");
        propertiesToRemove.add("localeName");
        propertiesToRemove.add("loginProvider");
        propertiesToRemove.add("regAttempts");
        propertiesToRemove.add("uid");
        propertiesToRemove.add("password");
        propertiesToRemove.add("duplicatedAccountUid");
        propertiesToRemove.add("registrationType");
        propertiesToRemove.add("timezone");
        propertiesToRemove.add("ecommerceTransaction");
        propertiesToRemove.add("personalInfoMandatory");
        propertiesToRemove.add("personalInfoOptional");
        propertiesToRemove.add("privateInfoMandatory");
        propertiesToRemove.add("privateInfoOptional");
        propertiesToRemove.add("processingConsignment");
        propertiesToRemove.add("termsOfUse");
        propertiesToRemove.add("hiraganaName");
        json.put("uuid", json.get("uid").asText());
        json.remove(propertiesToRemove);
        return mapper.writeValueAsString(json);
    }

    private String prepareJsonForGRP(ObjectNode json) throws JsonProcessingException {
        List<String> propertiesToRemove = new ArrayList<>();
        propertiesToRemove.add("loginProvider");
        propertiesToRemove.add("timezone");
        propertiesToRemove.add("jobRole");
        propertiesToRemove.add("phoneNumber");
        propertiesToRemove.add("interest");
        propertiesToRemove.add("ecommerceTransaction");
        propertiesToRemove.add("personalInfoMandatory");
        propertiesToRemove.add("personalInfoOptional");
        propertiesToRemove.add("privateInfoMandatory");
        propertiesToRemove.add("privateInfoOptional");
        propertiesToRemove.add("processingConsignment");
        propertiesToRemove.add("termsOfUse");
        json.remove(propertiesToRemove);
        return mapper.writeValueAsString(json);
    }

    @Test
    public void prepareForProfileInfoNotification_ShouldConvertTheAccountInfoObjectAsAJSONString() throws JsonProcessingException {
        // given
        AccountInfo account = AccountUtils.getSiteAccount();
        ObjectNode jsonAccount = mapper.valueToTree(account);
        String expectedAccountToNotify = prepareJsonForNotification(jsonAccount);

        // when
        String parsedAccount = accountHandler.prepareForProfileInfoNotification(account);
        
        // then
        assertEquals(parsedAccount.indexOf("\"password\""), -1);
        assertEquals(expectedAccountToNotify, parsedAccount);
    }

    @Test
    public void prepareForGRPNotification_ShouldConvertTheAccountInfoObjectAsAJSONString() throws JsonProcessingException {
        // given
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        AccountInfo mockAccount = AccountUtils.getSiteAccount();
        ObjectNode jsonAccount = mapper.valueToTree(mockAccount);
        String expectedAccountToNotify = prepareJsonForGRP(jsonAccount);

        // when
        String parsedAccount = accountHandler.prepareForGRPNotification(mockAccount);

        // then
        assertTrue(parsedAccount.indexOf("\"loginProvider\"") == -1);
        assertTrue(expectedAccountToNotify.equals(parsedAccount));
    }
}
