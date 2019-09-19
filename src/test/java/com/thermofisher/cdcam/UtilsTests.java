package com.thermofisher.cdcam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thermofisher.cdcam.enums.cdc.APIMethods;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.UserList;
import com.thermofisher.cdcam.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    public void emaiList_givenEmailListPopulated_returnElementsShouldBePossible() {
        List<String> list = new ArrayList<>();
        list.add("test1");
        list.add("test2");

        EmailList emailList = EmailList.builder().emails(list).build();
        Assert.assertEquals(list.size(), emailList.getEmails().size());
    }

    @Test
    public void userList_givenUserListPopulated_returnELementsShouldBePossible() {
        List<EECUser> eecUsers = new ArrayList<>();
        eecUsers.add(EECUser.builder().uid("A1").email("email").registered(true).cdcResponseCode(200).cdcResponseMessage("OK").build());
        eecUsers.add(EECUser.builder().uid("A2").email("email").registered(true).cdcResponseCode(200).cdcResponseMessage("OK").build());

        UserList userList = UserList.builder().eecUsers(eecUsers).build();
        Assert.assertEquals(eecUsers.size(), userList.getEecUsers().size());
    }

    @Test
    public void convertJavaToJsonString_ifCalled_returnValidJSONString() throws JsonProcessingException, JSONException {
        List<EECUser> eecUsers = new ArrayList<>();
        eecUsers.add(EECUser.builder().uid("A1").email("email").registered(true).cdcResponseCode(200).cdcResponseMessage("OK").build());
        eecUsers.add(EECUser.builder().uid("A2").email("email").registered(true).cdcResponseCode(200).cdcResponseMessage("OK").build());

        UserList userList = UserList.builder().eecUsers(eecUsers).build();

        String jsonString = Utils.convertJavaToJsonString(userList);
        JSONObject jsonObject = new JSONObject(jsonString);
        Assert.assertNotNull(jsonObject);
    }

    @Test
    public void getValueFromJSON_ifValidData_returnObject() throws JSONException {
        String testData = "{\"message\": \"test\"}";
        JSONObject object = new JSONObject(testData);
        Assert.assertEquals(Utils.getValueFromJSON(object, "message"), "test");
    }

    @Test
    public void getValueFromJSON_ifInvalidData_returnEmptyString() throws JSONException {
        String testData = "{\"message\": \"test\"}";
        JSONObject object = new JSONObject(testData);
        Assert.assertEquals(Utils.getValueFromJSON(object, "notValid"), "");
    }

}
