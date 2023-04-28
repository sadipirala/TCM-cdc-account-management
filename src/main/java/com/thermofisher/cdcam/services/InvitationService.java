package com.thermofisher.cdcam.services;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.thermofisher.cdcam.model.HttpServiceResponse;

@Service
public class InvitationService {
    Logger logger = LoggerFactory.getLogger(InvitationService.class);

    @Value("${identity.invitation.update_country}")
    private String updateCountryUrl;

    @Autowired
    HttpService httpService;

    public Integer updateInvitationCountry(JSONObject body) {
        HttpServiceResponse response = httpService.put(updateCountryUrl, body);
        return response.getStatus();
    }
}
