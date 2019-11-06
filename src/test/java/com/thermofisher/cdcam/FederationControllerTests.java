package com.thermofisher.cdcam;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.controller.FederationController;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.services.CDCAccountsService;
import com.thermofisher.cdcam.services.HashValidationService;
import com.thermofisher.cdcam.services.NotificationService;
import com.thermofisher.cdcam.services.UpdateAccountService;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import com.thermofisher.cdcam.utils.AccountInfoUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.BasicHttpEntity;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class FederationControllerTests {

    @InjectMocks
    FederationController federationController;

    @Mock
    private SNSHandler snsHandler;

    @Mock
    SecretsManager secretsManager;

    @Mock
    CDCAccountsService accountsService;

    @Mock
    HashValidationService hashValidationService;

    @Mock
    NotificationService notificationService;

    @Mock
    AccountInfoHandler accountInfoHandler;

    @Mock
    UpdateAccountService updateAccountService;

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
            .loginProvider("nonfederation")
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
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        
        //execution
        ResponseEntity<String> res = federationController.notifyRegistration("Test", mockBody);

        //validation
        Assert.assertTrue(res.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void notifyRegistration_ifGivenANonFederationUserUIDisSent_returnError() {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(nonFederationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        
        //execution
        ResponseEntity<String> res = federationController.notifyRegistration("Test", mockBody);
        
        //validation
        Assert.assertEquals(res.getBody(), "The user was not created through federation.");
    }

    @Test
    public void notifyRegistration_ifConnectionIsLost_throwException() {
        //setup
        Mockito.when(hashValidationService.isValidHash(null, null)).thenReturn(true);
        
        //execution
        ResponseEntity<String> res = federationController.notifyRegistration(null, null);
        
        //validation
        Assert.assertEquals(res.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void notifyRegistration_ifSNSNotificationFails_returnServiceUnavailable() {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(false);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        
        //execution
        ResponseEntity<String> res = federationController.notifyRegistration("Test", mockBody);
        
        //validation
        Assert.assertEquals(res.getStatusCode(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    public void notifyRegistration_ifGivenAnIncorrectRegistrationType_returnError() {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountCreated\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        
        //execution
        ResponseEntity<String> res = federationController.notifyRegistration("Test", mockBody);
        
        //validation
        Assert.assertEquals(res.getBody(), "the event type was not recognized");
    }

    @Test
    public void notifyRegistration_ifNoEventsAreFound_returnError() {
        //setup
        String mockBody = "{\"events\":[]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        
        //execution
        ResponseEntity<String> res = federationController.notifyRegistration("Test", mockBody);
        
        //validation
        Assert.assertTrue(res.getStatusCode().is4xxClientError());
    }

    @Test
    public void notifyRegistration_ifGivenAnInvalidSignature_returnError() {
        //setup
        String mockBody = "{\"events\":[]}";
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(false);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        
        //execution
        ResponseEntity<String> res = federationController.notifyRegistration("Test", mockBody);
        
        //validation
        Assert.assertTrue(res.getStatusCode().is4xxClientError());
    }

    @Test
    public void notifyRegistration_ifNoUserIsFound_returnBadRequest() {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(null);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        
        //execution
        ResponseEntity<String> res = federationController.notifyRegistration("Test", mockBody);
        
        //validation
        Assert.assertTrue(res.getStatusCode().is4xxClientError());
    }

    @Test
    public void notifyRegistration_givenARegistrationOccurs_ThenNotificationServicePostRequestShouldBeCalled() throws IOException {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        String mockAccountToNotify = "Test Account";
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        Mockito.when(accountInfoHandler.parseToNotify(any())).thenReturn(mockAccountToNotify);
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(AccountInfoUtils.getAccount());
        doNothing().when(updateAccountService).updateLegacyDataInCDC(any(), any());

        //execution
        federationController.notifyRegistration("Test", mockBody);

        //validation
        Mockito.verify(notificationService).postRequest(any(), any());
    }

    @Test
    public void notifyRegistration_givenGNSPostRequestExecute_ShouldReceiveRequestResponse() throws IOException {
        //set up
        ReflectionTestUtils.setField(federationController,"regNotificationUrl", "http://google.com");
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        String mockAccountToNotify = "Test Account";
        CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class, Mockito.RETURNS_DEEP_STUBS);
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream("".getBytes()));

        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(AccountInfoUtils.getAccount());
        Mockito.when(accountInfoHandler.parseToNotify(any())).thenReturn(mockAccountToNotify);
        Mockito.when(mockResponse.getEntity()).thenReturn(entity);
        Mockito.when(mockResponse.getStatusLine().getStatusCode()).thenReturn(200);
        Mockito.when(notificationService.postRequest(anyString(), anyString())).thenReturn(mockResponse);
        doNothing().when(mockResponse).close();
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);
        doNothing().when(updateAccountService).updateLegacyDataInCDC(any(), any());

        //execution
        ResponseEntity response = federationController.notifyRegistration("Test", mockBody);

        //validation
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }
}
