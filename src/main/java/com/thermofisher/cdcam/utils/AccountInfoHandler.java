package com.thermofisher.cdcam.utils;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.Profile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AccountInfoHandler
 */
@Component
public class AccountInfoHandler {

    @Value("${general.cipdc}")
    private String CIPDC;

    public String prepareForProfileInfoNotification(AccountInfo account) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.valueToTree(account);
        ObjectNode cleanJson = removePropertiesForNotification(json);
        cleanJson.put("uuid", account.getUid());
        cleanJson.put("cipdc", CIPDC);
        return mapper.writeValueAsString(cleanJson);
    }

    public String prepareForGRPNotification(AccountInfo account) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        List<String> propertiesToRemove = new ArrayList<>();
        if (account.getMember().equals("false")) {
            propertiesToRemove.add("department");
            propertiesToRemove.add("company");
            propertiesToRemove.add("city");
        }
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

        ObjectNode json = mapper.valueToTree(account);
        json.remove(propertiesToRemove);
        json.put("cipdc", CIPDC);

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

    public String prepareForAspireNotification(AccountInfo account) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

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

        ObjectNode json = mapper.valueToTree(account);
        json.remove(propertiesToRemove);

        return mapper.writeValueAsString(json);
    }

    public Map<String, MessageAttributeValue> buildMessageAttributesForAccountInfoSNS(AccountInfo account) {
        final String NA_COUNTRY_VALUE =  "NOT_AVAILABLE";
        String countryValue = account.getCountry();
        String country = (countryValue != null && !countryValue.trim().isEmpty()) ? countryValue : NA_COUNTRY_VALUE;
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();

        MessageAttributeValue messageAttributeValue = new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(country);
        messageAttributes.put("country", messageAttributeValue);

        return messageAttributes;
    }

    private ObjectNode removePropertiesForNotification(ObjectNode objectNode) {
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

        return objectNode.remove(propertiesToRemove);
    }
}
