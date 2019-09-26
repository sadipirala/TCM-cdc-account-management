package com.thermofisher.cdcam;

import static org.mockito.ArgumentMatchers.anyString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSResponse;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.model.dto.FedUserUpdateDTO;
import com.thermofisher.cdcam.services.CDCAccountsService;

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

/**
 * CDCAccountsServiceTests
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class CDCAccountsServiceTests {
    private final String uid = "c1c691f4-556b-4ad1-ab75-841fc4e94dcd";
    private final String username = "federatedUser@OIDC.com";

    @InjectMocks
    CDCAccountsService cdcAccountsService;

    @Mock
    CDCAccounts cdcAccounts;

    @Test
    public void updateFedUser_WhenGSResponseCodeIsZero_AnObjectNodeWith200ErrorCodeShouldBeReturned()
            throws JsonProcessingException {
        // given
        String message = "Success";
        FedUserUpdateDTO fedUserUpdate = FedUserUpdateDTO.builder().uid(uid).username(username).regStatus(true).build();
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        Mockito.when(cdcAccounts.setUserInfo(anyString(), anyString(), anyString())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(0);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        // when
        ObjectNode updateResponse = cdcAccountsService.updateFedUser(fedUserUpdate);

        // then
        Assert.assertEquals(HttpStatus.OK.value(), updateResponse.get("code").asInt());
        Assert.assertEquals(message, updateResponse.get("message").asText());
    }

    @Test
    public void updateFedUser_WhenGSResponseCodeIsZero_AnObjectNodeWith599999ErrorCodeShouldBeReturned()
            throws JsonProcessingException {
        // given
        String message = "Something went bad.";
        int errorCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        FedUserUpdateDTO fedUserUpdate = FedUserUpdateDTO.builder().uid(uid).username(username).regStatus(true).build();
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        Mockito.when(cdcAccounts.setUserInfo(anyString(), anyString(), anyString())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(errorCode);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        // when
        ObjectNode updateResponse = cdcAccountsService.updateFedUser(fedUserUpdate);

        // then
        Assert.assertEquals(errorCode, updateResponse.get("code").asInt());
        Assert.assertEquals(message, updateResponse.get("message").asText());
    }
}