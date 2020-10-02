package com.thermofisher.cdcam.builders;

import com.gigya.socialize.GSKeyNotFoundException;
import com.gigya.socialize.GSObject;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.China;
import com.thermofisher.cdcam.model.cdc.Japan;
import com.thermofisher.cdcam.model.cdc.Registration;
import com.thermofisher.cdcam.model.dto.AccountInfoDTO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AccountBuilder {

    private Logger logger = LogManager.getLogger(this.getClass());

    public AccountInfo getAccountInfo(GSObject obj) {
        try {
            String uid = (String) obj.get("UID");
            GSObject data = (GSObject) obj.get("data");
            GSObject profile = (GSObject) obj.get("profile");
            GSObject work = profile.containsKey("work") ? (GSObject) profile.get("work") : null;
            String email = profile.containsKey("email") ? profile.getString("email") : "";
            GSObject password = obj.containsKey("password") ? (GSObject) obj.get("password") : null;
            String company = "";
            String department = "";
            String finalPassword = "";
            Registration registration = getRegistration(data);
            China china = registration != null ? registration.getChina() : null;
            Japan japan = registration != null ? registration.getJapan() : null;

            if (password != null) {
                String hash = password.containsKey("hash") ? password.getString("hash") : "";
                GSObject hashSettings = password.containsKey("hashSettings") ? (GSObject) password.get("hashSettings") : null;
                if(hashSettings != null){
                    String algorithm = hashSettings.containsKey("algorithm") ? hashSettings.getString("algorithm") : "";
                    finalPassword = (algorithm + ":" + hash).toUpperCase();
                }
            }

            if (work != null) {
                company = work.containsKey("company") ? work.getString("company") : "";
                department = work.containsKey("location") ? work.getString("location") : "";
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
                    .department(department)
                    .member(data.containsKey("subscribe") ? data.getString("subscribe") : "false")
                    .localeName(profile.containsKey("locale") ? profile.getString("locale") : "")
                    .loginProvider(obj.containsKey("loginProvider") ? obj.getString("loginProvider") : "")
                    .hiraganaName(getHiraganaName(japan))
                    .jobRole(getJobRole(china))
                    .interest(getInterest(china))
                    .phoneNumber(getPhoneNumber(china))
                    .regAttempts(0)
                    .build();

        } catch (Exception e) {
            logger.error(String.format("Error building account info object: %s", e.getMessage()));
            return null;
        }
    }

    private static Registration getRegistration (GSObject data) throws JsonSyntaxException, GSKeyNotFoundException {
        Gson gson = new Gson();
        return data.containsKey("registration") ? gson.fromJson(data.getString("registration"), Registration.class) : null;
    }

    private static String getHiraganaName (Japan japan) {
        return japan != null ? japan.getHiraganaName() : null;
    }

    private static String getJobRole (China china) {
        return china != null ? china.getJobRole() : null;
    }

    private static String getInterest (China china) {
        return china != null ? china.getInterest() : null;
    }

    private static String getPhoneNumber (China china) {
        return china != null ? china.getPhoneNumber() : null;
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
            .department(accountInfoDTO.getDepartment())
            .city(accountInfoDTO.getCity())
            .country(accountInfoDTO.getCountry())
            .member(accountInfoDTO.getMember())
            .registrationType(accountInfoDTO.getRegistrationType())
            .timezone(accountInfoDTO.getTimezone())
            .hiraganaName(accountInfoDTO.getHiraganaName())
            .jobRole(accountInfoDTO.getJobRole())
            .interest(accountInfoDTO.getInterest())
            .phoneNumber(accountInfoDTO.getPhoneNumber())
            .build();
    }
}
