package com.thermofisher.cdcam.services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.thermofisher.cdcam.model.cdc.Profile;
import com.thermofisher.cdcam.model.dto.ProfileInfoDTO;
import com.thermofisher.cdcam.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Slf4j
public class UpdateAccountService {
    private static final int SUCCESS_CODE = 200;
    private static final int BAD_REQUEST = 400;

    @Value("${account.legacy_username.validation}")
    private boolean isLegacyValidationEnabled;

    @Autowired
    GigyaService gigyaService;

    public HttpStatus updateTimezoneInCDC(String uid, String timezone) throws JSONException {
        log.info(String.format("Account update for time zone triggered. UID: %s", uid));

        Profile profile = Profile.builder().timezone(timezone).build();
        JSONObject jsonAccount = new JSONObject();

        JSONObject cleanProfile = Utils.removeNullValuesFromJsonObject(new JSONObject(profile));

        jsonAccount.put("uid", uid);
        jsonAccount.put("profile", cleanProfile);
        ObjectNode response = gigyaService.update(jsonAccount);

        if (response.get("code").asInt() == SUCCESS_CODE) {
            log.info(String.format("Account update success. UID: %s", uid));
        } else {
            log.error(String.format("Account update failed. UID: %s. Error: %s", uid, response.get("log").asText()));
        }

        return HttpStatus.valueOf(response.get("code").asInt());
    }

    public HttpStatus updateProfile(ProfileInfoDTO profileInfoDTO) throws JSONException {
        String uid = profileInfoDTO.getUid();
        log.info(String.format("User Profile update by UID: %s", uid));
        if (Utils.isNullOrEmpty(uid)) {
            log.error("UID is null or empty");
            return HttpStatus.valueOf(BAD_REQUEST);
        }

        Profile profile = Profile.build(profileInfoDTO);
        JSONObject jsonAccount = new JSONObject();
        if (!Utils.isNullOrEmpty(profileInfoDTO.getEmail())) {
            if (!profileInfoDTO.getActualEmail().equalsIgnoreCase(profileInfoDTO.getActualUsername())) {
                jsonAccount.put("removeLoginEmails", profileInfoDTO.getActualEmail());
            }

            if (StringUtils.isBlank(profileInfoDTO.getActualUsername()) ||
                    (isLegacyValidationEnabled && profileInfoDTO.isALegacyProfile()) ||
                    (!profileInfoDTO.getActualUsername().equalsIgnoreCase(profileInfoDTO.getEmail()))) {
                jsonAccount.put("username", profileInfoDTO.getEmail().toLowerCase(Locale.ENGLISH));

            }
        }

        JSONObject cleanProfile = new JSONObject(new Gson().toJson(profile));
        jsonAccount.put("uid", uid);
        jsonAccount.put("profile", cleanProfile);

        ObjectNode response = gigyaService.update(jsonAccount);
        if (response.get("code").asInt() == SUCCESS_CODE) {
            log.info(String.format("Profile update success. UID: %s", uid));
        } else {
            log.error(String.format("Profile update failed. UID: %s. Error: %s", uid, response.get("log").asText()));
        }

        return HttpStatus.valueOf(response.get("code").asInt());
    }
}
