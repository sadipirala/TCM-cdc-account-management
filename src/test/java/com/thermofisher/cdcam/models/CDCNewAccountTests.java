package com.thermofisher.cdcam.models;

import com.google.gson.Gson;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.model.cdc.Data;
import com.thermofisher.cdcam.model.cdc.Profile;
import com.thermofisher.cdcam.utils.AccountUtils;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

@ExtendWith(MockitoExtension.class)
public class CDCNewAccountTests {
    Profile profile;
    Data data;

    @BeforeEach
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
        Assertions.assertEquals(expectedData, result.getData());
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
        Assertions.assertEquals(expectedProfile, result.getProfile());
    }

    private static String prepareData(Data data) throws JSONException {
        return new Gson().toJson(data);
    }

    private static String prepareProfile(Profile profile) throws JSONException {
        return new Gson().toJson(profile);
    }
}
