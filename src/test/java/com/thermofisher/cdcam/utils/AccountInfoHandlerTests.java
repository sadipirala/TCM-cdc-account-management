package com.thermofisher.cdcam.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.cdcam.model.AccountInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * AccountInfoHandlerTests
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AccountInfoHandler.class)
public class AccountInfoHandlerTests {
    
    @InjectMocks
    AccountInfoHandler accountHandler;

    private final String MOCK_CIPDC = "us";
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void before() {
        ReflectionTestUtils.setField(accountHandler, "CIPDC", MOCK_CIPDC);
    }

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
        propertiesToRemove.add("acceptsAspireEnrollmentConsent");
        propertiesToRemove.add("isHealthcareProfessional");
        propertiesToRemove.add("isGovernmentEmployee");
        propertiesToRemove.add("isProhibitedFromAcceptingGifts");
        propertiesToRemove.add("acceptsAspireTermsAndConditions");
        json.put("uuid", json.get("uid").asText());
        json.put("cipdc", MOCK_CIPDC);
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
        propertiesToRemove.add("acceptsAspireEnrollmentConsent");
        propertiesToRemove.add("isHealthcareProfessional");
        propertiesToRemove.add("isGovernmentEmployee");
        propertiesToRemove.add("isProhibitedFromAcceptingGifts");
        propertiesToRemove.add("acceptsAspireTermsAndConditions");
        json.remove(propertiesToRemove);
        json.put("cipdc", MOCK_CIPDC);
        return mapper.writeValueAsString(json);
    }

    public String prepareJsonForAspireNotification(ObjectNode json) throws JsonProcessingException {
        List<String> propertiesToRemove = new ArrayList<>();
        propertiesToRemove.add("duplicatedAccountUid");
        propertiesToRemove.add("registrationType");
        propertiesToRemove.add("loginProvider");
        propertiesToRemove.add("timezone");
        propertiesToRemove.add("localeName");
        propertiesToRemove.add("cipdc");
        propertiesToRemove.add("regAttempts");
        propertiesToRemove.add("uid");
        propertiesToRemove.add("password");
        propertiesToRemove.add("ecommerceTransaction");
        propertiesToRemove.add("personalInfoMandatory");
        propertiesToRemove.add("personalInfoOptional");
        propertiesToRemove.add("privateInfoMandatory");
        propertiesToRemove.add("privateInfoOptional");
        propertiesToRemove.add("processingConsignment");
        propertiesToRemove.add("termsOfUse");
        propertiesToRemove.add("interests");
        propertiesToRemove.add("jobRole");
        propertiesToRemove.add("hiraganaName");
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

    @Test
    public void prepareForAspireNotification_ShouldConvertTheAccountInfoObjectAsAJSONString() throws JsonProcessingException {
        // given
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        AccountInfo mockAccount = AccountUtils.getSiteAccount();
        ObjectNode jsonAccount = mapper.valueToTree(mockAccount);
        String expectedAccountToNotify = prepareJsonForAspireNotification(jsonAccount);

        // when
        String parsedAccount = accountHandler.prepareForAspireNotification(mockAccount);

        // then
        assertEquals(parsedAccount.indexOf("\"duplicatedAccountUid\""), -1);
        assertEquals(expectedAccountToNotify, parsedAccount);
    }

    @Test
    public void prepareForGRPNotification_givenAspireAccountIsNotMember_ShouldNotAddAdditionalFieldsToString() throws JsonProcessingException {
        // given
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        AccountInfo mockAccount = AccountUtils.getSiteAccount();
        mockAccount.setMember("false");

        // when
        String parsedAccount = accountHandler.prepareForGRPNotification(mockAccount);

        // then
        assertEquals(parsedAccount.indexOf("\"department\""), -1);
        assertEquals(parsedAccount.indexOf("\"company\""), -1);
        assertEquals(parsedAccount.indexOf("\"city\""), -1);
    }

    @Test
    public void buildMessageAttributesForAccountInfoSNS_ShouldSetMessageAttributes(){
        // given
        AccountInfo mockAccount = AccountUtils.getSiteAccount();
        MessageAttributeValue mockMessageAttributeValue = new MessageAttributeValue();

        // when
        Map<String, MessageAttributeValue> messageAttributes = accountHandler.buildMessageAttributesForAccountInfoSNS(mockAccount);

        // then
        assertTrue(messageAttributes.containsValue(mockMessageAttributeValue.withDataType("String").withStringValue(mockAccount.getCountry())));
    }

    @Test
    public void buildMessageAttributesForAccountInfoSNS_ShouldSetCountryMessageAttributeAsNotAvailableIfCountryValuesIsNotPresent(){
        // given
        AccountInfo mockAccount = AccountUtils.getSiteAccount();
        MessageAttributeValue mockMessageAttributeValue = new MessageAttributeValue();
        final String NA_COUNTRY_VALUE = "NOT_AVAILABLE";
        mockAccount.setCountry("");

        // when
        Map<String, MessageAttributeValue> messageAttributes = accountHandler.buildMessageAttributesForAccountInfoSNS(mockAccount);

        // then
        assertTrue(messageAttributes.containsValue(mockMessageAttributeValue.withDataType("String").withStringValue(NA_COUNTRY_VALUE)));
    }
}
