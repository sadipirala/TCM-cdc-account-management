package com.thermofisher.cdcam.builders;

import com.gigya.socialize.GSKeyNotFoundException;
import com.gigya.socialize.GSObject;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.Registration;
import com.thermofisher.cdcam.model.dto.AccountInfoDTO;

import com.thermofisher.cdcam.model.dto.RegistrationDTO;
import com.thermofisher.cdcam.utils.cdc.RegistrationAttributesHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AccountBuilder {

    private Logger logger = LogManager.getLogger(this.getClass());

    public AccountInfo getAccountInfo(GSObject obj) {
        String uid = null;
        try {
            uid = (String) obj.get("UID");
            GSObject data = (GSObject) obj.get("data");
            GSObject profile = (GSObject) obj.get("profile");
            GSObject work = profile.containsKey("work") ? (GSObject) profile.get("work") : null;
            String email = profile.containsKey("email") ? profile.getString("email") : "";
            GSObject password = obj.containsKey("password") ? (GSObject) obj.get("password") : null;
            String socialProviders = obj.containsKey("socialProviders") ? obj.getString("socialProviders") : "";
            String company = "";
            String finalPassword = "";
            Registration registration = getRegistration(data);
            RegistrationAttributesHandler registrationAttributesHandler = new RegistrationAttributesHandler(registration);

            if (password != null) {
                String hash = password.containsKey("hash") ? password.getString("hash") : "";
                GSObject hashSettings = password.containsKey("hashSettings") ? (GSObject) password.get("hashSettings") : null;
                if (hashSettings != null) {
                    String algorithm = hashSettings.containsKey("algorithm") ? hashSettings.getString("algorithm") : "";
                    finalPassword = (algorithm + ":" + hash).toUpperCase();
                }
            }

            if (work != null) {
                company = work.containsKey("company") ? work.getString("company") : "";
            }

            return AccountInfo.builder()
                    .uid(uid)
                    .username(profile.containsKey("username") ? profile.getString("username") : email)
                    .emailAddress(email)
                    .password(finalPassword)
                    .firstName(profile.containsKey("firstName") ? profile.getString("firstName") : "")
                    .lastName(profile.containsKey("lastName") ? profile.getString("lastName") : "")
                    .company(company)
                    .country(profile.containsKey("country") ? profile.getString("country") : "")
                    .city(profile.containsKey("city") ? profile.getString("city") : "")
                    .member(data.containsKey("subscribe") ? data.getString("subscribe") : "false")
                    .localeName(profile.containsKey("locale") ? profile.getString("locale") : "")
                    .loginProvider(obj.containsKey("loginProvider") ? obj.getString("loginProvider") : "")
                    .socialProviders(socialProviders)
                    .hiraganaName(registrationAttributesHandler.getHiraganaName())
                    .jobRole(registrationAttributesHandler.getJobRole())
                    .interest(registrationAttributesHandler.getInterest())
                    .phoneNumber(registrationAttributesHandler.getPhoneNumber())
                    .receiveMarketingInformation(registrationAttributesHandler.getReceiveMarketingInformation())
                    .thirdPartyTransferPersonalInfoMandatory(registrationAttributesHandler.getThirdPartyTransferPersonalInfoMandatory())
                    .thirdPartyTransferPersonalInfoOptional(registrationAttributesHandler.getThirdPartyTransferPersonalInfoOptional())
                    .collectionAndUsePersonalInfoMandatory(registrationAttributesHandler.getCollectionAndUsePersonalInfoMandatory())
                    .collectionAndUsePersonalInfoOptional(registrationAttributesHandler.getCollectionAndUsePersonalInfoOptional())
                    .collectionAndUsePersonalInfoMarketing(registrationAttributesHandler.getCollectionAndUsePersonalInfoMarketing())
                    .overseasTransferPersonalInfoMandatory(registrationAttributesHandler.getOverseasTransferPersonalInfoMandatory())
                    .overseasTransferPersonalInfoOptional(registrationAttributesHandler.getOverseasTransferPersonalInfoOptional())
                    .regAttempts(0)
                    .build();

        } catch (Exception e) {
            logger.error(String.format("Error building account info object. UID: %s. Message: %s", uid, e.getMessage()));
            return null;
        }
    }

    private static Registration getRegistration (GSObject data) throws JsonSyntaxException, GSKeyNotFoundException {
        Gson gson = new Gson();
        if(data.containsKey("registration")) {
            RegistrationDTO registrationDTO = gson.fromJson(data.getString("registration"), RegistrationDTO.class);
            return Registration.build(registrationDTO);
        }
        return null;
    }

    public static AccountInfo parseFromAccountInfoDTO(AccountInfoDTO accountInfoDTO) {
        return AccountInfo.builder()
            .firstName(accountInfoDTO.getFirstName())
            .lastName(accountInfoDTO.getLastName())
            .username(accountInfoDTO.getUsername())
            .emailAddress(accountInfoDTO.getEmailAddress())
            .password(accountInfoDTO.getPassword())
            .localeName(accountInfoDTO.getLocaleName())
            .company(accountInfoDTO.getCompany())
            .city(accountInfoDTO.getCity())
            .country(accountInfoDTO.getCountry())
            .member(accountInfoDTO.getMember())
            .registrationType(accountInfoDTO.getRegistrationType())
            .timezone(accountInfoDTO.getTimezone())
            .hiraganaName(accountInfoDTO.getHiraganaName())
            .jobRole(accountInfoDTO.getJobRoles())
            .interest(accountInfoDTO.getInterests())
            .phoneNumber(accountInfoDTO.getPhoneNumber())
            .receiveMarketingInformation(accountInfoDTO.getReceiveMarketingInformation())
            .thirdPartyTransferPersonalInfoMandatory(accountInfoDTO.getThirdPartyTransferPersonalInfoMandatory())
            .thirdPartyTransferPersonalInfoOptional(accountInfoDTO.getThirdPartyTransferPersonalInfoOptional())
            .collectionAndUsePersonalInfoMandatory(accountInfoDTO.getCollectionAndUsePersonalInfoMandatory())
            .collectionAndUsePersonalInfoOptional(accountInfoDTO.getCollectionAndUsePersonalInfoOptional())
            .collectionAndUsePersonalInfoMarketing(accountInfoDTO.getCollectionAndUsePersonalInfoMarketing())
            .overseasTransferPersonalInfoMandatory(accountInfoDTO.getOverseasTransferPersonalInfoMandatory())
            .overseasTransferPersonalInfoOptional(accountInfoDTO.getOverseasTransferPersonalInfoOptional())
            .acceptsAspireEnrollmentConsent(accountInfoDTO.getAcceptsAspireEnrollmentConsent())
            .isHealthcareProfessional(accountInfoDTO.getIsHealthcareProfessional())
            .isGovernmentEmployee(accountInfoDTO.getIsGovernmentEmployee())
            .isProhibitedFromAcceptingGifts(accountInfoDTO.getIsProhibitedFromAcceptingGifts())
            .acceptsAspireTermsAndConditions(accountInfoDTO.getAcceptsAspireTermsAndConditions())
            .build();
    }
}
