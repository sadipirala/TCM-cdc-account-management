package com.thermofisher.cdcam.handlers;

import com.gigya.socialize.GSResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.services.GigyaApi;
import com.thermofisher.cdcam.services.GigyaService;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.dto.ProfileInfoDTO;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
//@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest//(classes = CdcamApplication.class)
public class UsersHandlerTests {

    @Value("${cdc.main.datacenter}")
    private String mainApiDomain;

    @InjectMocks
    UsersHandler usersHandler;

    @Mock
    GigyaApi gigyaApi;
    
    @Mock
    GigyaService gigyaService;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
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
        when(gigyaApi.search(anyString(), any(), any())).thenReturn(mockSearchResponse);

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
        when(gigyaApi.search(anyString(), any(), any())).thenReturn(mockSearchResponse);

        //execution
        List<UserDetails> userDetails = usersHandler.getUsers(uids);

        //validation
        Assert.assertEquals(userDetails.size(),1);
    }

    @Test
    public void getUsers_GivenAnInValidListOfUIDs_returnEmptyList() throws IOException {
        // given
        List<String> uids = new ArrayList<>();
        uids.add("001");
        uids.add("002");
        uids.add("003");

        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponse = "{\n" + 
                "  \"totalCount\": 1,\n" + 
                "  \"statusCode\": 200,\n" +
                "  \"errorCode\": 400,\n" +
                "  \"statusReason\": \"OK\",\n" + 
                "  \"results\": [\n" + 
                "  ]\n" + 
                "}";

        when(mockSearchResponse.getResponseText()).thenReturn(searchResponse);
        when(gigyaApi.search(anyString(), any(), any())).thenReturn(mockSearchResponse);

        // when
        List<UserDetails> userDetails = usersHandler.getUsers(uids);

        // then
        Assert.assertEquals(userDetails.size(), 0);
    }

    @Test
    public void getUserProfileByUID_GivenAValidUID_returnUserProfile() throws IOException, CustomGigyaErrorException {
        //setup
        String uid = "001";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        AccountInfo accountInfoMock = AccountUtils.getSiteAccount();
        ProfileInfoDTO profileInfoDTOMock = ProfileInfoDTO.build(accountInfoMock);
        
        when(gigyaService.getAccountInfo(anyString())).thenReturn(accountInfoMock);

        //execution
        ProfileInfoDTO profileInfoDTO = usersHandler.getUserProfileByUID(uid);

        //validation
        Assert.assertEquals(gson.toJson(profileInfoDTO), gson.toJson(profileInfoDTOMock));
    }

    @Test
    public void getUserProfileByUID_GivenAnInvalidUID_thenThrowAnCustomGigyaErrorExceptionAndShouldReturnNull() throws IOException, CustomGigyaErrorException {
        //setup
        String uid = "001";

        when(gigyaService.getAccountInfo(anyString())).thenThrow(new CustomGigyaErrorException("UserNotFound"));

        //execution
        ProfileInfoDTO profileInfoDTO = usersHandler.getUserProfileByUID(uid);

        // then
        Assert.assertNull(profileInfoDTO);
    }
}
