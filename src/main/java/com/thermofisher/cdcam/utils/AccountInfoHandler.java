package com.thermofisher.cdcam.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.cdcam.enums.NotificationType;
import com.thermofisher.cdcam.model.AccountInfo;

import org.springframework.stereotype.Component;

/**
 * AccountInfoHandler
 */
@Component
public class AccountInfoHandler {

    public static String prepareForProfileInfoNotification(AccountInfo account, String cipdc) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.valueToTree(account);
        ObjectNode cleanJson = removePropertiesForNotification(json);
        cleanJson.put("uuid", account.getUid());
        cleanJson.put("cipdc", cipdc);
        return mapper.writeValueAsString(cleanJson);
    }

    public static String buildRegistrationNotificationPayload(AccountInfo account, String cipdc) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        List<String> propertiesToRemove = new ArrayList<>();
        if (!account.isMarketingConsent()) {
            propertiesToRemove.add("company");
            propertiesToRemove.add("city");
        }
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

        ObjectNode json = mapper.valueToTree(account);
        json.remove(propertiesToRemove);
        json.put("cipdc", cipdc);
        json.put("type", NotificationType.REGISTRATION.getValue());

        return mapper.writeValueAsString(json);
    }

    public static String prepareForAspireNotification(AccountInfo account) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

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

        ObjectNode json = mapper.valueToTree(account);
        json.remove(propertiesToRemove);

        return mapper.writeValueAsString(json);
    }

    public static Map<String, MessageAttributeValue> buildMessageAttributesForAccountInfoSNS(AccountInfo account) {
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

    private static ObjectNode removePropertiesForNotification(ObjectNode objectNode) {
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

        return objectNode.remove(propertiesToRemove);
    }
}
