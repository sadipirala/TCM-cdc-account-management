package com.thermofisher.cdcam.builders;


import java.util.Locale;
import java.util.Objects;
import com.gigya.socialize.GSKeyNotFoundException;
import com.gigya.socialize.GSObject;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.Korea;
import com.thermofisher.cdcam.model.cdc.Preferences;
import com.thermofisher.cdcam.model.cdc.Registration;
import com.thermofisher.cdcam.model.cdc.Thermofisher;
import com.thermofisher.cdcam.model.dto.AccountInfoDTO;
import com.thermofisher.cdcam.model.dto.RegistrationDTO;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.RegistrationAttributesHandler;
import com.thermofisher.cdcam.utils.cdc.ThermofisherAttributesHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Slf4j
public class AccountBuilder {

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

            String providerClientId = getProviderClientId(registration);

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
                    .marketingConsent(data.containsKey("subscribe") ? data.getBool("subscribe") : false)
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
                    .openIdProviderId(providerClientId)
                    .build();

        } catch (Exception e) {
            log.error(String.format("Error building account info object. UID: %s. Message: %s", uid, Utils.stackTraceToString(e)));
            return null;
        }
    }

    public AccountInfo getAccountInfoV2(GSObject obj) {
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
            Preferences preferences = getPreferences(obj);
            Registration registration = getRegistration(data);
            RegistrationAttributesHandler registrationAttributesHandler = new RegistrationAttributesHandler(registration);
            Thermofisher thermofisher = getThermofisher(data);
            ThermofisherAttributesHandler thermofisherAttributesHandler = new ThermofisherAttributesHandler(thermofisher);


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

            String providerClientId = getProviderClientId(registration);
            Korea koreaConsents = getKoreaConsents(preferences);

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
                    .marketingConsent(getIsConsentGranted(preferences))
                    .localeName(profile.containsKey("locale") ? profile.getString("locale") : "")
                    .loginProvider(obj.containsKey("loginProvider") ? obj.getString("loginProvider") : "")
                    .socialProviders(socialProviders)
                    .hiraganaName(registrationAttributesHandler.getHiraganaName())
                    .jobRole(registrationAttributesHandler.getJobRole())
                    .interest(registrationAttributesHandler.getInterest())
                    .phoneNumber(registrationAttributesHandler.getPhoneNumber())
                    .receiveMarketingInformation(koreaConsents.getReceiveMarketingInformation())
                    .thirdPartyTransferPersonalInfoMandatory(koreaConsents.getThirdPartyTransferPersonalInfoMandatory())
                    .thirdPartyTransferPersonalInfoOptional(koreaConsents.getThirdPartyTransferPersonalInfoOptional())
                    .collectionAndUsePersonalInfoMandatory(koreaConsents.getCollectionAndUsePersonalInfoMandatory())
                    .collectionAndUsePersonalInfoOptional(koreaConsents.getCollectionAndUsePersonalInfoOptional())
                    .collectionAndUsePersonalInfoMarketing(koreaConsents.getCollectionAndUsePersonalInfoMarketing())
                    .overseasTransferPersonalInfoMandatory(koreaConsents.getOverseasTransferPersonalInfoMandatory())
                    .overseasTransferPersonalInfoOptional(koreaConsents.getOverseasTransferPersonalInfoOptional())
                    .regAttempts(0)
                    .openIdProviderId(providerClientId)
                    .legacyUserName(thermofisherAttributesHandler.getLegacyUsername())
                    .build();

        } catch (Exception e) {
            log.error(String.format("Error building account info object. UID: %s. Message: %s", uid, Utils.stackTraceToString(e)));
            return null;
        }
    }

    private static Registration getRegistration(GSObject data) throws JsonSyntaxException, GSKeyNotFoundException {
        Gson gson = new Gson();
        if (data.containsKey("registration")) {
            RegistrationDTO registrationDTO = gson.fromJson(data.getString("registration"), RegistrationDTO.class);
            return Registration.build(registrationDTO);
        }

        return null;
    }

    private static Thermofisher getThermofisher(GSObject data) throws JsonSyntaxException, GSKeyNotFoundException {

        Gson gson = new Gson();
        if (data.containsKey("thermofisher")) {
            Thermofisher thermofisher = gson.fromJson(data.getString("thermofisher"), Thermofisher.class);
            return thermofisher;
        }

        return null;
    }


    private boolean getIsConsentGranted(Preferences preferences) {
        return Objects.nonNull(preferences) && Objects.nonNull(preferences.getMarketing()) && Objects.nonNull(preferences.getMarketing().getConsent()) && preferences.getMarketing().getConsent().isConsentGranted();
    }

    private Korea getKoreaConsents(Preferences preferences) {
        return Korea.buildFromPreferences(preferences);
    }

    private Preferences getPreferences(GSObject obj) throws JsonSyntaxException, GSKeyNotFoundException {
        Gson gson = new Gson();
        if (obj.containsKey("preferences")) {
            return gson.fromJson(obj.getString("preferences"), Preferences.class);
        }

        return null;
    }

    private String getProviderClientId(Registration registration) {
        if (Objects.nonNull(registration) && Objects.nonNull(registration.getOpenIdProvider()) && StringUtils.isNotBlank(registration.getOpenIdProvider().getClientID())) {
            return registration.getOpenIdProvider().getClientID();
        }

        return "";
    }

    public static AccountInfo buildFrom(AccountInfoDTO accountInfoDTO) {
        return AccountInfo.builder()
            .firstName(accountInfoDTO.getFirstName())
            .lastName(accountInfoDTO.getLastName())
            .username(accountInfoDTO.getUsername().toLowerCase(Locale.ENGLISH))
            .emailAddress(accountInfoDTO.getEmailAddress().toLowerCase(Locale.ENGLISH))
            .password(accountInfoDTO.getPassword())
            .localeName(accountInfoDTO.getLocaleName())
            .company(accountInfoDTO.getCompany())
            .city(accountInfoDTO.getCity())
            .country(accountInfoDTO.getCountry())
            .marketingConsent(accountInfoDTO.isMarketingConsent())
            .registrationType(accountInfoDTO.getRegistrationType())
            .timezone(accountInfoDTO.getTimezone())
            .hiraganaName(accountInfoDTO.getHiraganaName())
            .jobRole(accountInfoDTO.getJobRoles())
            .interest(accountInfoDTO.getInterests())
            .phoneNumber(accountInfoDTO.getPhoneNumber())
            .receiveMarketingInformation(accountInfoDTO.isReceiveMarketingInformation())
            .thirdPartyTransferPersonalInfoMandatory(accountInfoDTO.isThirdPartyTransferPersonalInfoMandatory())
            .thirdPartyTransferPersonalInfoOptional(accountInfoDTO.isThirdPartyTransferPersonalInfoOptional())
            .collectionAndUsePersonalInfoMandatory(accountInfoDTO.isCollectionAndUsePersonalInfoMandatory())
            .collectionAndUsePersonalInfoOptional(accountInfoDTO.isCollectionAndUsePersonalInfoOptional())
            .collectionAndUsePersonalInfoMarketing(accountInfoDTO.isCollectionAndUsePersonalInfoMarketing())
            .overseasTransferPersonalInfoMandatory(accountInfoDTO.isOverseasTransferPersonalInfoMandatory())
            .overseasTransferPersonalInfoOptional(accountInfoDTO.isOverseasTransferPersonalInfoOptional())
            .acceptsAspireEnrollmentConsent(accountInfoDTO.getAcceptsAspireEnrollmentConsent())
            .isHealthcareProfessional(accountInfoDTO.getIsHealthcareProfessional())
            .isGovernmentEmployee(accountInfoDTO.getIsGovernmentEmployee())
            .isProhibitedFromAcceptingGifts(accountInfoDTO.getIsProhibitedFromAcceptingGifts())
            .acceptsAspireTermsAndConditions(accountInfoDTO.getAcceptsAspireTermsAndConditions())
            .build();

    }
}
