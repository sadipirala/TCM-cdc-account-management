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
import com.thermofisher.cdcam.enums.NotificationType;
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

    private final String MOCK_CIPDC = "us";
    private ObjectMapper mapper = new ObjectMapper();

    @After
    public void after() { 
        mapper.setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS);
    }
    
    private String prepareJsonForNotification(ObjectNode json) throws JsonProcessingException {
        List<String> propertiesToRemove = new ArrayList<>();
        propertiesToRemove.add("localeName");
        propertiesToRemove.add("loginProvider");
        propertiesToRemove.add("socialProviders");
        propertiesToRemove.add("regAttempts");
        propertiesToRemove.add("uid");
        propertiesToRemove.add("password");
        propertiesToRemove.add("registrationType");
        propertiesToRemove.add("timezone");
        propertiesToRemove.add("receiveMarketingInformation");
        propertiesToRemove.add("thirdPartyTransferPersonalInfoMandatory");
        propertiesToRemove.add("thirdPartyTransferPersonalInfoOptional");
        propertiesToRemove.add("collectionAndUsePersonalInfoMandatory");
        propertiesToRemove.add("collectionAndUsePersonalInfoOptional");
        propertiesToRemove.add("collectionAndUsePersonalInfoMarketing");
        propertiesToRemove.add("overseasTransferPersonalInfoMandatory");
        propertiesToRemove.add("overseasTransferPersonalInfoOptional");
        propertiesToRemove.add("hiraganaName");
        propertiesToRemove.add("acceptsAspireEnrollmentConsent");
        propertiesToRemove.add("isHealthcareProfessional");
        propertiesToRemove.add("isGovernmentEmployee");
        propertiesToRemove.add("isProhibitedFromAcceptingGifts");
        propertiesToRemove.add("acceptsAspireTermsAndConditions");
        propertiesToRemove.add("openIdProviderId");
        propertiesToRemove.add("phoneNumber");
        json.put("uuid", json.get("uid").asText());
        json.put("cipdc", MOCK_CIPDC);
        json.remove(propertiesToRemove);
        return mapper.writeValueAsString(json);
    }

    private String buildRegistrationNotificationPayload(ObjectNode json) throws JsonProcessingException {
        List<String> propertiesToRemove = new ArrayList<>();
        propertiesToRemove.add("loginProvider");
        propertiesToRemove.add("socialProviders");
        propertiesToRemove.add("timezone");
        propertiesToRemove.add("jobRole");
        propertiesToRemove.add("phoneNumber");
        propertiesToRemove.add("interest");
        propertiesToRemove.add("receiveMarketingInformation");
        propertiesToRemove.add("thirdPartyTransferPersonalInfoMandatory");
        propertiesToRemove.add("thirdPartyTransferPersonalInfoOptional");
        propertiesToRemove.add("collectionAndUsePersonalInfoMandatory");
        propertiesToRemove.add("collectionAndUsePersonalInfoOptional");
        propertiesToRemove.add("collectionAndUsePersonalInfoMarketing");
        propertiesToRemove.add("overseasTransferPersonalInfoMandatory");
        propertiesToRemove.add("overseasTransferPersonalInfoOptional");
        propertiesToRemove.add("acceptsAspireEnrollmentConsent");
        propertiesToRemove.add("isHealthcareProfessional");
        propertiesToRemove.add("isGovernmentEmployee");
        propertiesToRemove.add("isProhibitedFromAcceptingGifts");
        propertiesToRemove.add("acceptsAspireTermsAndConditions");
        propertiesToRemove.add("openIdProviderId");
        json.remove(propertiesToRemove);
        json.put("cipdc", MOCK_CIPDC);
        json.put("type", NotificationType.REGISTRATION.getValue());
        return mapper.writeValueAsString(json);
    }

    public String prepareJsonForAspireNotification(ObjectNode json) throws JsonProcessingException {
        List<String> propertiesToRemove = new ArrayList<>();
        propertiesToRemove.add("registrationType");
        propertiesToRemove.add("loginProvider");
        propertiesToRemove.add("socialProviders");
        propertiesToRemove.add("timezone");
        propertiesToRemove.add("localeName");
        propertiesToRemove.add("cipdc");
        propertiesToRemove.add("regAttempts");
        propertiesToRemove.add("password");
        propertiesToRemove.add("receiveMarketingInformation");
        propertiesToRemove.add("thirdPartyTransferPersonalInfoMandatory");
        propertiesToRemove.add("thirdPartyTransferPersonalInfoOptional");
        propertiesToRemove.add("collectionAndUsePersonalInfoMandatory");
        propertiesToRemove.add("collectionAndUsePersonalInfoOptional");
        propertiesToRemove.add("collectionAndUsePersonalInfoMarketing");
        propertiesToRemove.add("overseasTransferPersonalInfoMandatory");
        propertiesToRemove.add("overseasTransferPersonalInfoOptional");
        propertiesToRemove.add("interests");
        propertiesToRemove.add("jobRole");
        propertiesToRemove.add("hiraganaName");
        propertiesToRemove.add("openIdProviderId");
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
        String parsedAccount = AccountInfoHandler.prepareForProfileInfoNotification(account, AccountUtils.cipdc);
        
        // then
        assertEquals(parsedAccount.indexOf("\"password\""), -1);
        assertEquals(parsedAccount.indexOf("\"phoneNumber\""), -1);
        assertEquals(expectedAccountToNotify, parsedAccount);
    }

    @Test
    public void buildRegistrationNotificationPayload_ShouldConvertTheAccountInfoObjectAsAJSONString() throws JsonProcessingException {
        // given
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        AccountInfo mockAccount = AccountUtils.getSiteAccount();
        ObjectNode jsonAccount = mapper.valueToTree(mockAccount);
        String expectedAccountToNotify = buildRegistrationNotificationPayload(jsonAccount);

        // when
        String parsedAccount = AccountInfoHandler.buildRegistrationNotificationPayload(mockAccount, AccountUtils.cipdc);

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
        String parsedAccount = AccountInfoHandler.prepareForAspireNotification(mockAccount);

        // then
        assertEquals(expectedAccountToNotify, parsedAccount);
    }

    @Test
    public void buildRegistrationNotificationPayload_givenAspireAccountIsNotMember_ShouldNotAddAdditionalFieldsToString() throws JsonProcessingException {
        // given
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        AccountInfo mockAccount = AccountUtils.getSiteAccount();
        mockAccount.setMarketingConsent(false);

        // when
        String parsedAccount = AccountInfoHandler.buildRegistrationNotificationPayload(mockAccount, AccountUtils.cipdc);

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
        Map<String, MessageAttributeValue> messageAttributes = AccountInfoHandler.buildMessageAttributesForAccountInfoSNS(mockAccount);

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
        Map<String, MessageAttributeValue> messageAttributes = AccountInfoHandler.buildMessageAttributesForAccountInfoSNS(mockAccount);

        // then
        assertTrue(messageAttributes.containsValue(mockMessageAttributeValue.withDataType("String").withStringValue(NA_COUNTRY_VALUE)));
    }
}
