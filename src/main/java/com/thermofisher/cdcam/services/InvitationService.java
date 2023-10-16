package com.thermofisher.cdcam.services;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.thermofisher.cdcam.model.HttpServiceResponse;
@Slf4j
@Service
public class InvitationService {

    @Value("${identity.invitation.update_country}")
    private String updateCountryUrl;

    @Autowired
    HttpService httpService;

    public Integer updateInvitationCountry(JSONObject body) {
        HttpServiceResponse response = httpService.put(updateCountryUrl, body);
        return response.getStatus();
    }
}
