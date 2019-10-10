package com.thermofisher.cdcam.utils;

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
        json.remove(propertiesToRemove);
        return mapper.writeValueAsString(json);
    }

    @Test
    public void parseToNotify_ShouldConvertTheAccountInfoObjectAsAJSONString() throws JsonProcessingException {
        // given
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        AccountInfo mockAccount = AccountInfoUtils.getAccount();
        ObjectNode jsonAccount = mapper.valueToTree(mockAccount);
        String expectedAccountToNotify = prepareJsonForNotification(jsonAccount);
        AccountInfo account = AccountInfoUtils.getAccount();

        // when
        String parsedAccount = accountHandler.parseToNotify(account);
        
        // then
        assertTrue(parsedAccount.indexOf("\"password\"") == -1);
        assertTrue(expectedAccountToNotify.equals(parsedAccount));
    }
}