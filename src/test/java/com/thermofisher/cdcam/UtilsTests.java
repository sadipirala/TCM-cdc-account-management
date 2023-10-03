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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ActiveProfiles("test")
//@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest//(classes = Utils.class)
public class UtilsTests {

    @Test
    public void getAlphaNumericString_ifNumberIsProvided_returnSameSizeString() {
        // given
        int stringSize = 10;

        // when
        String value = Utils.getAlphaNumericString(stringSize);

        // then
        Assert.assertEquals(value.length(), stringSize);
    }

    @Test
    public void ApiMethodGet_ifCalled_returnGETMethod() {
        // given
        String getAccount = "accounts.getAccountInfo";

        // when
        String enumGET = APIMethods.GET.getValue();

        // then
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

    @Test
    public void isNullOrEmpty_ShouldReturnTrue_WhenThePassedListIsNull() {
        List<String> list = null;

        // when
        boolean result = Utils.isNullOrEmpty(list);

        // then
        assertTrue(result);
    }

    @Test
    public void isNullOrEmpty_ShouldReturnTrue_WhenThePassedListIsEmpty() {
        // given
        List<String> list = new ArrayList<String>();

        // when
        boolean result = Utils.isNullOrEmpty(list);

        // then
        assertTrue(result);
    }

    @Test
    public void isNullOrEmpty_ShouldReturnFalse_WhenThePassedListIsNotNull() {
        // given
        List<String> list = new ArrayList<String>();
        list.add("");

        // when
        boolean result = Utils.isNullOrEmpty(list);

        // then
        assertFalse(result);
    }

    @Test
    public void isNullOrEmpty_ShouldReturnFalse_WhenThePassedListIsNotEmpty() {
        // given
        List<String> list = new ArrayList<String>();
        list.add("");

        // when
        boolean result = Utils.isNullOrEmpty(list);

        // then
        assertFalse(result);
    }

    @Test
    public void isNullOrEmpty_ShouldReturnTrue_WhenThePassedStringIsNull() {
        String text = null;

        // when
        boolean result = Utils.isNullOrEmpty(text);

        // then
        assertTrue(result);
    }

    @Test
    public void isNullOrEmpty_ShouldReturnTrue_WhenThePassedStringIsEmpty() {
        // given
        String text = "";

        // when
        boolean result = Utils.isNullOrEmpty(text);

        // then
        assertTrue(result);
    }

    @Test
    public void isNullOrEmpty_ShouldReturnFalse_WhenThePassedStringIsNotNull() {
        // given
        String text = "200";

        // when
        boolean result = Utils.isNullOrEmpty(text);

        // then
        assertFalse(result);
    }

    @Test
    public void isNullOrEmpty_ShouldReturnFalse_WhenThePassedStringIsNotEmpty() {
        // given
        String text = "200";

        // when
        boolean result = Utils.isNullOrEmpty(text);

        // then
        assertFalse(result);
    }

    @Test
    public void hasNullOrEmptyValues_GivenAListHasNullValues_ThenItShouldReturnTrue() {
        // given
        ArrayList<String> list = new ArrayList<String>();
        list.add(null);

        // when
        boolean result = Utils.hasNullOrEmptyValues(list);

        // then
        assertTrue(result);
    }

    @Test
    public void hasNullOrEmptyValues_GivenAListDoesNotHasNullValues_ThenItShouldReturnFalse() {
        // given
        ArrayList<String> list = new ArrayList<String>();

        // when
        boolean result = Utils.hasNullOrEmptyValues(list);

        // then
        assertFalse(result);
    }

    @Test
    public void hasNullOrEmptyValues_GivenAListHasEmptyValues_ThenItShouldReturnTrue() {
        // given
        ArrayList<String> list = new ArrayList<String>();
        list.add("");

        // when
        boolean result = Utils.hasNullOrEmptyValues(list);

        // then
        assertTrue(result);
    }

    @Test
    public void hasNullOrEmptyValues_GivenAListDoesNotHasEmptyValues_ThenItShouldReturnFalse() {
        // given
        ArrayList<String> list = new ArrayList<String>();
        list.add("200");

        // when
        boolean result = Utils.hasNullOrEmptyValues(list);

        // then
        assertFalse(result);
    }

    @Test
    public void isAValidEmail_GivenMethodIsCalledWithAValidEmail_ThenItShouldReturnTrue() {
        // given
        String email = "test@test.com";

        // when
        boolean result = Utils.isAValidEmail(email);

        // then
        assertTrue(result);
    }

    @Test
    public void isAValidEmail_GivenMethodIsCalledWithAInvalidEmail_ThenItShouldReturnFalse() {
        // given
        String email = "test@@test.com";

        // when
        boolean result = Utils.isAValidEmail(email);

        // then
        assertFalse(result);
    }

    @Test
    public void isAValidEmail_GivenMethodIsCalledWithConsecutivePeriods_ThenItShouldReturnFalse() {
        // given
        String email = "test..data@mail.com";

        // when
        boolean result = Utils.isAValidEmail(email);

        // then
        assertFalse(result);
    }
}
