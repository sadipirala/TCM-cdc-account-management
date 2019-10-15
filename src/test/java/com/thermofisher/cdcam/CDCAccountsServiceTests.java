package com.thermofisher.cdcam;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSResponse;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.services.CDCAccountsService;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.anyString;

/**
 * CDCAccountsServiceTests
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class CDCAccountsServiceTests {

    @InjectMocks
    CDCAccountsService cdcAccountsService;

    @Mock
    CDCAccounts cdcAccounts;

    @Test
    public void updateFedUser_WhenGSResponseCodeIsZero_AnObjectNodeWith200ErrorCodeShouldBeReturned()
            throws JSONException {
        // given
        String message = "Success";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        Mockito.when(cdcAccounts.setUserInfo(anyString(), anyString(), anyString())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(0);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        JSONObject user = new JSONObject("{\"data\":{\"regStatus\":true},\"profile\":{\"username\":\"test@test.com\"}}");

        // when
        ObjectNode updateResponse = cdcAccountsService.update(user);

        // then
        Assert.assertEquals(HttpStatus.OK.value(), updateResponse.get("code").asInt());
        Assert.assertEquals(message, updateResponse.get("message").asText());
    }

    @Test
    public void updateFedUser_WhenGSResponseCodeIsZero_AnObjectNodeWith599999ErrorCodeShouldBeReturned()
            throws JSONException {
        // given
        String message = "Something went bad.";
        int errorCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        Mockito.when(cdcAccounts.setUserInfo(anyString(), anyString(), anyString())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(errorCode);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        JSONObject user = new JSONObject("{\"data\":{\"regStatus\":true},\"profile\":{\"username\":\"test@test.com\"}}");

        // when
        ObjectNode updateResponse = cdcAccountsService.update(user);

        // then
        Assert.assertEquals(errorCode, updateResponse.get("code").asInt());
        Assert.assertEquals(message, updateResponse.get("message").asText());
    }
}
