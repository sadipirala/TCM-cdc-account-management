package com.thermofisher.cdcam.models;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.model.cdc.Data;
import com.thermofisher.cdcam.model.cdc.Profile;
import com.thermofisher.cdcam.utils.AccountUtils;

import org.json.JSONException;
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
        CDCNewAccount result = CDCNewAccount.build(
            AccountUtils.username, 
            AccountUtils.email, 
            AccountUtils.password, 
            data, 
            profile);

        // then
        assertEquals(expectedData, result.getData());
    }

    @Test
    public void whenBuildingCDCNewAccount_ThenProfilePropertiesWithNullValuesShouldBeRemoved()
            throws JSONException {
        // given
        String expectedProfile = prepareProfile(profile);
        
        // when
        CDCNewAccount result = CDCNewAccount.build(
            AccountUtils.username, 
            AccountUtils.email, 
            AccountUtils.password, 
            data, 
            profile);

        // then
        assertEquals(expectedProfile, result.getProfile());
    }

    private static String prepareData(Data data) throws JSONException {
        return new Gson().toJson(data);
    }

    private static String prepareProfile(Profile profile) throws JSONException {
        return new Gson().toJson(profile);
    }
}
