package com.thermofisher.cdcam;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.controller.AccountController;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.services.HashValidationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.internal.util.MockUtil.createMock;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class AccountControllerTests {

    @InjectMocks
    private AccountController notificationController = new AccountController();

    @Mock
    private SNSHandler snsHandler;

    @Mock
    CDCAccounts accounts;

    @Mock
    HashValidationService hashValidationService;

    private AccountInfo federationAccount = AccountInfo.builder()
            .username("federatedUser@OIDC.com")
            .emailAddress("federatedUser@OIDC.com")
            .firstName("first")
            .lastName("last")
            .country("country")
            .localeName("en_US")
            .loginProvider("oidc")
            .password("Password1")
            .regAttempts(0)
            .city("testCity")
            .department("dep")
            .company("myCompany")
            .build();

    private AccountInfo nonFederationAccount = AccountInfo.builder()
            .username("User@test.com")
            .emailAddress("User@test.com")
            .firstName("first")
            .lastName("last")
            .country("country")
            .localeName("en_US")
            .loginProvider("site")
            .password("Password1")
            .regAttempts(0)
            .city("testCity")
            .department("dep")
            .company("myCompany")
            .build();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void notifyRegistration_ifGivenAFederationUserUIDisSent_returnFederationAccount() {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accounts.getAccount(anyString())).thenReturn(federationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString())).thenReturn("Test");
        //execution
        ResponseEntity<String> res = notificationController.notifyRegistration("Test", mockBody);
        //validation
        Assert.assertTrue(res.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void notifyRegistration_ifGivenANonFederationUserUIDisSent_returnError() {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accounts.getAccount(anyString())).thenReturn(nonFederationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString())).thenReturn("Test");
        //execution
        ResponseEntity<String> res = notificationController.notifyRegistration("Test", mockBody);
        //validation
        Assert.assertEquals(res.getBody(), "The user was not created through federation");
    }

    @Test
    public void notifyRegistration_ifConnectionIsLost_throwException() {
        //setup
        Mockito.when(hashValidationService.isValidHash(null, null)).thenReturn(true);
        //execution
        ResponseEntity<String> res = notificationController.notifyRegistration(null, null);
        //validation
        Assert.assertEquals(res.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void notifyRegistration_ifSNSNotificationFails_returnServiceUnavailable() {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accounts.getAccount(anyString())).thenReturn(federationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(false);
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString())).thenReturn("Test");
        //execution
        ResponseEntity<String> res = notificationController.notifyRegistration("Test", mockBody);
        //validation
        Assert.assertEquals(res.getStatusCode(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    public void notifyRegistration_ifGivenAnIncorrectRegistrationType_returnError() {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountCreated\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accounts.getAccount(anyString())).thenReturn(federationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString())).thenReturn("Test");
        //execution
        ResponseEntity<String> res = notificationController.notifyRegistration("Test", mockBody);
        //validation
        Assert.assertEquals(res.getBody(), "the event type was not recognized");
    }

    @Test
    public void notifyRegistration_ifNoEventsAreFound_returnError() {
        //setup
        String mockBody = "{\"events\":[]}";
        Mockito.when(accounts.getAccount(anyString())).thenReturn(federationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString())).thenReturn("Test");
        //execution
        ResponseEntity<String> res = notificationController.notifyRegistration("Test", mockBody);
        //validation
        Assert.assertTrue(res.getStatusCode().is4xxClientError());
    }

    @Test
    public void notifyRegistration_ifGivenAnInvalidSignature_returnError() {
        //setup
        String mockBody = "{\"events\":[]}";
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(false);
        Mockito.when(hashValidationService.getHashedString(anyString())).thenReturn("Test");
        //execution
        ResponseEntity<String> res = notificationController.notifyRegistration("Test", mockBody);
        //validation
        Assert.assertTrue(res.getStatusCode().is4xxClientError());
    }

    @Test
    public void notifyRegistration_ifNoUserIsFound_returnBadRequest() {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accounts.getAccount(anyString())).thenReturn(null);
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString())).thenReturn("Test");
        //execution
        ResponseEntity<String> res = notificationController.notifyRegistration("Test", mockBody);
        //validation
        Assert.assertTrue(res.getStatusCode().is4xxClientError());
    }
}
