package com.thermofisher.cdcam.models;

import static org.junit.Assert.assertEquals;

import com.thermofisher.cdcam.model.cdc.*;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class CDCNewAccountTests {
    Profile profile;
    Data data;

    @Before
    public void setup() {
        profile = AccountUtils.getProfile();
        data = AccountUtils.getData();
    }
    
    @Test
    public void whenBuildingCDCNewAccount_ThenDataPropertiesWithNullValuesShouldBeRemoved()
            throws JSONException {
        // given
        String expectedData = prepareData(data);
        
        // when
        CDCNewAccount result = CDCNewAccount.builder()
            .username(AccountUtils.username)
            .email(AccountUtils.email)
            .password(AccountUtils.password)
            .data(data)
            .profile(profile)
            .build();

        // then
        assertEquals(expectedData, result.getData());
    }

    @Test
    public void whenBuildingCDCNewAccount_ThenProfilePropertiesWithNullValuesShouldBeRemoved()
            throws JSONException {
        // given
        String expectedProfile = prepareProfile(profile);
        
        // when
        CDCNewAccount result = CDCNewAccount.builder()
            .username(AccountUtils.username)
            .email(AccountUtils.email)
            .password(AccountUtils.password)
            .data(data)
            .profile(profile)
            .build();

        // then
        assertEquals(expectedProfile, result.getProfile());
    }

    private static String prepareData(Data data) throws JSONException {
        JSONObject dataJson = Utils.removeNullValuesFromJsonObject(new JSONObject(data));
        return dataJson.toString();
    }

    private static String prepareProfile(Profile profile) throws JSONException {
        JSONObject profileJson = Utils.removeNullValuesFromJsonObject(new JSONObject(profile));
        return profileJson.toString();
    }
}