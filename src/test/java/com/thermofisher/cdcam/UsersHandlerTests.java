package com.thermofisher.cdcam;


import com.gigya.socialize.GSResponse;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class UsersHandlerTests {

    @InjectMocks
    UsersHandler usersHandler;

    @Mock
    CDCAccounts cdcAccounts;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getUser_GivenAValidUID_returnUserDetails() throws IOException {

        //setup
        String uid = "test123";

        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponse = "{\n" +
                "  \"totalCount\": 1,\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\",\n" +
                "  \"results\": [\n" +
                "  \t{\n" +
                "  \t\t\"UID\": \"" + uid + "\",\n" +
                "  \t\t\"isRegistered\": true,\n" +
                "  \t\t\"profile\": {\n" +
                "  \t\t\t\"lastName\": \"last\",\n" +
                "  \t\t\t\"firstName\": \"first\",\n" +
                "  \t\t\t\"email\": \"email@test.com\"\n" +
                "  \t\t}\n" +
                "  \t}\n" +
                "  ]\n" +
                "}";

        when(mockSearchResponse.getResponseText()).thenReturn(searchResponse);
        when(cdcAccounts.search(anyString(),anyString())).thenReturn(mockSearchResponse);

        //execution
        UserDetails userDetails = usersHandler.getUser(uid);

        //validation
        Assert.assertNotNull(userDetails);
    }
@Test
    public void getUser_GivenAValidUIDWithMoreThanOneAccount_returnOneUserDetails() throws IOException {

        //setup
        String uid = "test123";

        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponse = "{\n" +
                "  \"totalCount\": 1,\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\",\n" +
                "  \"results\": [\n" +
                "  \t{\n" +
                "  \t\t\"UID\": \"" + uid + "\",\n" +
                "  \t\t\"isRegistered\": true,\n" +
                "  \t\t\"profile\": {\n" +
                "  \t\t\t\"lastName\": \"last\",\n" +
                "  \t\t\t\"firstName\": \"first\",\n" +
                "  \t\t\t\"email\": \"email@test.com\"\n" +
                "  \t\t}\n" +
                "  \t},\n" +
                "  \t{\n" +
                "  \t\t\"UID\": \"" + uid + "\",\n" +
                "  \t\t\"isRegistered\": true,\n" +
                "  \t\t\"profile\": {\n" +
                "  \t\t\t\"lastName\": \"last\",\n" +
                "  \t\t\t\"firstName\": \"first\",\n" +
                "  \t\t\t\"email\": \"email@test.com\"\n" +
                "  \t\t}\n" +
                "  \t}\n" +
                "  ]\n" +
                "}";

        when(mockSearchResponse.getResponseText()).thenReturn(searchResponse);
        when(cdcAccounts.search(anyString(),anyString())).thenReturn(mockSearchResponse);

        //execution
        UserDetails userDetails = usersHandler.getUser(uid);

        //validation
        Assert.assertNotNull(userDetails);
        Assert.assertEquals(userDetails.getAssociatedAccounts(),2);
    }

    @Test
    public void getUser_GivenAnInValidUID_returnNull() throws IOException {

        //setup
        String uid = "test123";

        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponse = "{\n" +
                "  \"totalCount\": 1,\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\",\n" +
                "  \"results\": [\n" +
                "  ]\n" +
                "}";

        when(mockSearchResponse.getResponseText()).thenReturn(searchResponse);
        when(cdcAccounts.search(anyString(),anyString())).thenReturn(mockSearchResponse);

        //execution
        UserDetails userDetails = usersHandler.getUser(uid);

        //validation
        Assert.assertNull(userDetails);
    }


    @Test
    public void getUsers_GivenAValidUID_returnUserDetails() throws IOException {

        //setup
        List<String> uids = new ArrayList<>();
        uids.add("001");
        uids.add("002");
        uids.add("003");

        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponse = "{\n" +
                "  \"totalCount\": 1,\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\",\n" +
                "  \"results\": [\n" +
                "  \t{\n" +
                "  \t\t\"UID\": \"" + uids.get(0) + "\",\n" +
                "  \t\t\"isRegistered\": true,\n" +
                "  \t\t\"profile\": {\n" +
                "  \t\t\t\"lastName\": \"last\",\n" +
                "  \t\t\t\"firstName\": \"first\",\n" +
                "  \t\t\t\"email\": \"email@test.com\"\n" +
                "  \t\t}\n" +
                "  \t}\n" +
                "  ]\n" +
                "}";

        when(mockSearchResponse.getResponseText()).thenReturn(searchResponse);
        when(cdcAccounts.search(anyString(),anyString())).thenReturn(mockSearchResponse);

        //execution
        List<UserDetails> userDetails = usersHandler.getUsers(uids);

        //validation
        Assert.assertEquals(userDetails.size(),1);
    }
    @Test
    public void getUsers_GivenAValidUIDWithMoreThanOneAccount_returnOneUserDetails() throws IOException {

        //setup
        List<String> uids = new ArrayList<>();
        uids.add("001");
        uids.add("002");
        uids.add("003");

        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponse = "{\n" +
                "  \"totalCount\": 1,\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\",\n" +
                "  \"results\": [\n" +
                "  \t{\n" +
                "  \t\t\"UID\": \"" + uids.get(0) + "\",\n" +
                "  \t\t\"isRegistered\": true,\n" +
                "  \t\t\"profile\": {\n" +
                "  \t\t\t\"lastName\": \"last\",\n" +
                "  \t\t\t\"firstName\": \"first\",\n" +
                "  \t\t\t\"email\": \"email@test.com\"\n" +
                "  \t\t}\n" +
                "  \t},\n" +
                "  \t{\n" +
                "  \t\t\"UID\": \"" + uids.get(0) + "\",\n" +
                "  \t\t\"isRegistered\": true,\n" +
                "  \t\t\"profile\": {\n" +
                "  \t\t\t\"lastName\": \"last\",\n" +
                "  \t\t\t\"firstName\": \"first\",\n" +
                "  \t\t\t\"email\": \"email@test.com\"\n" +
                "  \t\t}\n" +
                "  \t}\n" +
                "  ]\n" +
                "}";

        when(mockSearchResponse.getResponseText()).thenReturn(searchResponse);
        when(cdcAccounts.search(anyString(),anyString())).thenReturn(mockSearchResponse);

        //execution
        List<UserDetails> userDetails = usersHandler.getUsers(uids);

        //validation
        Assert.assertEquals(userDetails.size(),1);
    }

    @Test
    public void getUsers_GivenAnInValidListOfUIDs_returnEmptyList() throws IOException {

        //setup
        List<String> uids = new ArrayList<>();
        uids.add("001");
        uids.add("002");
        uids.add("003");

        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponse = "{\n" +
                "  \"totalCount\": 1,\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\",\n" +
                "  \"results\": [\n" +
                "  ]\n" +
                "}";

        when(mockSearchResponse.getResponseText()).thenReturn(searchResponse);
        when(cdcAccounts.search(anyString(),anyString())).thenReturn(mockSearchResponse);

        //execution
        List<UserDetails> userDetails = usersHandler.getUsers(uids);

        //validation
        Assert.assertEquals(userDetails.size(),0);
    }
}
