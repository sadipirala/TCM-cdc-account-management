package com.thermofisher.cdcam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thermofisher.cdcam.enums.cdc.APIMethods;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Utils.class)
public class UtilsTests {

    @Test
    public void getAlphaNumericString_ifNumberIsProvided_returnSameSizeString() {
        //setup
        int stringSize = 10;
        //execution
        String value = Utils.getAlphaNumericString(stringSize);

        //validation
        Assert.assertEquals(value.length(), stringSize);
    }

    @Test
    public void ApiMethodGet_ifCalled_returnGETMethod() {
        //setup
        String getAccount = "accounts.getAccountInfo";
        //execution
        String enumGET = APIMethods.GET.getValue();
        //validation
        Assert.assertEquals(getAccount, enumGET);
    }

    @Test
    public void getStringFromJSON_ifValidData_returnObject() throws JSONException {
        String testData = "{\"message\": \"test\"}";
        JSONObject object = new JSONObject(testData);
        Assert.assertEquals(Utils.getStringFromJSON(object, "message"), "test");
    }

    @Test
    public void getStringFromJSON_ifInvalidData_returnEmptyString() throws JSONException {
        String testData = "{\"message\": \"test\"}";
        JSONObject object = new JSONObject(testData);
        Assert.assertEquals(Utils.getStringFromJSON(object, "notValid"), "");
    }

    @Test
    public void parseLocale_ShouldReturnOnlyTheLanguageFromALocaleString() {
        // given
        String locale = "es_MX";
        String expectedLocale = "es";

        // when
        String result = Utils.parseLocale(locale);

        // then
        assertEquals(expectedLocale, result);
    }

    @Test
    public void parseLocale_givenChinaComesAsTheLocale_ThenTheLocaleShouldBeParsedAsCDCNeeds() {
        // given
        String locale = "zh_CN";
        String expectedLocale = "zh-cn";

        // when
        String result = Utils.parseLocale(locale);

        // then
        assertEquals(expectedLocale, result);
    }

    @Test
    public void convertJavaToJsonString_ifValidString_returnObject() throws IOException {
        List<String> list = new ArrayList<>();
        EmailList data = EmailList.builder().emails(list).build();
        String jsonString = Utils.convertJavaToJsonString(data);

        EmailList testUser = new ObjectMapper().readValue(jsonString, EmailList.class);
        Assert.assertNotNull(testUser);
    }

}
