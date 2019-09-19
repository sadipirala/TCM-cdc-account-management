package com.thermofisher.cdcam;

import com.gigya.socialize.GSResponse;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
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
public class LiteRegHandlerTests {

    @InjectMocks
    LiteRegHandler liteRegHandler;

    @Mock
    CDCAccounts cdcAccounts;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void process_givenEmailListEmpty_returnEmptyEECUserList() throws IOException {
        EmailList emailList = EmailList.builder().emails(new ArrayList<>()).build();
        List<EECUser> output = liteRegHandler.process(emailList);
        Assert.assertTrue(output.isEmpty());
    }

    @Test
    public void process_givenEmailList_ReturnEECUserList() throws IOException {
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponse = "{\n" +
                "  \"totalCount\": 1,\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\",\n" +
                "  \"results\": [\n" +
                "  \t{\n" +
                "  \t\t\"UID\": \"abc123\",\n" +
                "  \t\t\"isRegistered\": true,\n" +
                "  \t\t\"profile\": {\n" +
                "  \t\t\t\"username\": \"armatest\"\n" +
                "  \t\t}\n" +
                "  \t}\n" +
                "  ]\n" +
                "}";

        when(mockSearchResponse.getResponseText()).thenReturn(searchResponse);
        when(cdcAccounts.searchByEmail(anyString())).thenReturn(mockSearchResponse);

        List<String> emails = new ArrayList();
        emails.add("test1");
        emails.add("test2");

        EmailList emailList = EmailList.builder().emails(emails).build();

        List<EECUser> output = liteRegHandler.process(emailList);

        Assert.assertEquals(output.size(), emails.size());
    }

    @Test
    public void process_givenEmailListFoundUserButItsLite_ReturnEECUserListWithRegisteredFlagFalse() throws IOException {
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponse = "{\n" +
                "  \"totalCount\": 1,\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\",\n" +
                "  \"results\": [\n" +
                "  \t{\n" +
                "  \t\t\"UID\": \"abc123\",\n" +
                "  \t\t\"profile\": {\n" +
                "  \t\t\t\"username\": \"armatest\"\n" +
                "  \t\t}\n" +
                "  \t}\n" +
                "  ]\n" +
                "}";

        when(mockSearchResponse.getResponseText()).thenReturn(searchResponse);
        when(cdcAccounts.searchByEmail(anyString())).thenReturn(mockSearchResponse);

        List<String> emails = new ArrayList();
        emails.add("test1");

        EmailList emailList = EmailList.builder().emails(emails).build();

        List<EECUser> output = liteRegHandler.process(emailList);
        EECUser user = output.get(0);

        Assert.assertFalse(user.isRegistered());
    }

    @Test
    public void process_givenEmailsNotFound_LiteRegisterAndReturnEECUserList() throws IOException {
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponse = "{\n" +
                "  \"totalCount\": 0,\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\",\n" +
                "  \"results\": []\n" +
                "}";

        GSResponse mockLiteRegResponse = Mockito.mock(GSResponse.class);
        String liteRegResponse = "{\n" +
                "  \"errorCode\": 0,\n" +
                "  \"errorMessage\": null,\n" +
                "  \"errorDetails\": null,\n" +
                "  \"data\": {\n" +
                "    \"callId\": \"5c62541d1ce341eba0faf1d14642c191\",\n" +
                "    \"UID\": \"9f6f2133e57144d787574d49c0b9908e\",\n" +
                "    \"apiVersion\": 2,\n" +
                "    \"statusReason\": \"OK\",\n" +
                "    \"errorCode\": 0,\n" +
                "    \"time\": \"2019-09-19T16:14:24.983Z\",\n" +
                "    \"statusCode\": 200\n" +
                "  }\n" +
                "}";

        when(mockLiteRegResponse.getResponseText()).thenReturn(liteRegResponse);
        when(cdcAccounts.setLiteReg(anyString())).thenReturn(mockLiteRegResponse);

        when(mockSearchResponse.getResponseText()).thenReturn(searchResponse);
        when(cdcAccounts.searchByEmail(anyString())).thenReturn(mockSearchResponse);

        List<String> emails = new ArrayList();
        emails.add("test1");
        emails.add("test2");

        EmailList emailList = EmailList.builder().emails(emails).build();

        List<EECUser> output = liteRegHandler.process(emailList);

        Assert.assertEquals(output.size(), emails.size());
    }

    @Test
    public void process_givenSearchReturnsNull_returnEECUserWith500Error() throws IOException {
        when(cdcAccounts.searchByEmail(anyString())).thenReturn(null);

        List<String> emails = new ArrayList();
        emails.add("test1");

        EmailList emailList = EmailList.builder().emails(emails).build();

        List<EECUser> output = liteRegHandler.process(emailList);

        EECUser user = output.get(0);
        Assert.assertEquals(user.getCdcResponseCode(), 500);
    }

    @Test
    public void process_givenSearchReturnsErrorCodeMoreThanZero_returnEECUserWithErrorDetails() throws IOException {
        int errorCode = 500;
        String errorMessage = "An error occurred";

        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponse = "{\n" +
                "  \"totalCount\": 0,\n" +
                "  \"errorCode\": " + errorCode + ",\n" +
                "  \"statusCode\": " + errorCode + ",\n" +
                "  \"statusReason\": \"" + errorMessage + "\",\n" +
                "  \"results\": []\n" +
                "}";

        when(mockSearchResponse.getResponseText()).thenReturn(searchResponse);
        when(cdcAccounts.searchByEmail(anyString())).thenReturn(mockSearchResponse);

        List<String> emails = new ArrayList();
        emails.add("test1");

        EmailList emailList = EmailList.builder().emails(emails).build();

        List<EECUser> output = liteRegHandler.process(emailList);

        EECUser user = output.get(0);
        Assert.assertEquals(user.getCdcResponseCode(), errorCode);
        Assert.assertEquals(user.getCdcResponseMessage(), errorMessage);
    }

    @Test
    public void process_givenLiteRegReturnsNull_returnEECUserWithErrorDetails() throws IOException {
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponse = "{\n" +
                "  \"totalCount\": 0,\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\",\n" +
                "  \"results\": []\n" +
                "}";

        when(cdcAccounts.setLiteReg(anyString())).thenReturn(null);

        when(mockSearchResponse.getResponseText()).thenReturn(searchResponse);
        when(cdcAccounts.searchByEmail(anyString())).thenReturn(mockSearchResponse);

        List<String> emails = new ArrayList();
        emails.add("test1");

        EmailList emailList = EmailList.builder().emails(emails).build();

        List<EECUser> output = liteRegHandler.process(emailList);

        EECUser user = output.get(0);
        Assert.assertEquals(user.getCdcResponseCode(), 500);
    }

    @Test
    public void process_givenLiteRegReturnsErrorCodeMoreThanZero_returnEECUserWithErrorDetails() throws IOException {
        int errorCode = 400006;

        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponse = "{\n" +
                "  \"totalCount\": 0,\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\",\n" +
                "  \"results\": []\n" +
                "}";

        GSResponse mockLiteRegResponse = Mockito.mock(GSResponse.class);
        String liteRegResponse = "{\n" +
                "  \"errorCode\": " + errorCode + ",\n" +
                "  \"errorMessage\": \"Invalid parameter value\",\n" +
                "  \"errorDetails\": \"Schema validation failed\",\n" +
                "  \"data\": {\n" +
                "    \"callId\": \"349272dd0ec242d89e2be84c6692d0d2\",\n" +
                "    \"apiVersion\": 2,\n" +
                "    \"statusReason\": \"Bad Request\",\n" +
                "    \"errorMessage\": \"Invalid parameter value\",\n" +
                "    \"errorCode\": " + errorCode + ",\n" +
                "    \"validationErrors\": [\n" +
                "      {\n" +
                "        \"fieldName\": \"profile.email\",\n" +
                "        \"errorCode\": 400006,\n" +
                "        \"message\": \"Unallowed value for field: email\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"time\": \"2019-09-19T16:15:20.508Z\",\n" +
                "    \"errorDetails\": \"Schema validation failed\",\n" +
                "    \"statusCode\": 400\n" +
                "  }\n" +
                "}";

        when(mockLiteRegResponse.getResponseText()).thenReturn(liteRegResponse);
        when(cdcAccounts.setLiteReg(anyString())).thenReturn(mockLiteRegResponse);

        when(mockSearchResponse.getResponseText()).thenReturn(searchResponse);
        when(cdcAccounts.searchByEmail(anyString())).thenReturn(mockSearchResponse);

        List<String> emails = new ArrayList();
        emails.add("test1");

        EmailList emailList = EmailList.builder().emails(emails).build();

        List<EECUser> output = liteRegHandler.process(emailList);

        EECUser user = output.get(0);
        Assert.assertEquals(user.getCdcResponseCode(), errorCode);
    }
}
