package com.thermofisher.cdcam;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.services.*;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import com.thermofisher.cdcam.utils.AccountInfoUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.json.JSONException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class AccountRequestServiceTests {
    private final List<String> uids = new ArrayList<>();
    private final String hashedString = "QJERFC2183DASJ=";

    @InjectMocks
    AccountRequestService accountRequestService;

    @Mock
    AccountInfoHandler accountInfoHandler;

    @Mock
    SNSHandler snsHandler;

    @Mock
    SecretsManager secretsManager;

    @Mock
    HashValidationService hashValidationService;

    @Mock
    CDCAccountsService accountsService;

    @Mock
    NotificationService notificationService;

    @Mock
    UpdateAccountService updateAccountService;

    private AccountInfo federationAccount = AccountInfo.builder().username("federatedUser@OIDC.com")
            .emailAddress("federatedUser@OIDC.com").firstName("first").lastName("last").country("country")
            .localeName("en_US").loginProvider("oidc").password("Randompassword1").regAttempts(0).city("testCity")
            .department("dep").company("myCompany").build();

    private AccountInfo nonFederationAccount = AccountInfo.builder().username("nonFederatedUser@email.com")
            .emailAddress("nonFederatedUser@email.com").firstName("first").lastName("last").country("country")
            .localeName("en_US").loginProvider("site").password("Randompassword1").regAttempts(0).city("testCity")
            .department("dep").company("myCompany").build();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        uids.add("001");
        uids.add("002");
        uids.add("003");
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn(hashedString);
    }

    @Test
    public void processRequest_IfGivenAUID_searchAccountInfo() throws JSONException, IOException{
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        doNothing().when(updateAccountService).updateLegacyDataInCDC(any(), any());
        Mockito.when(accountInfoHandler.prepareForGRPNotification(any())).thenCallRealMethod();
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);

        //execution
        accountRequestService.processRequest("Test", mockBody);

        //validation
        Mockito.verify(accountsService).getAccountInfo(any());
    }


    @Test
    public void processRequest_IfValidHashIsFalse_thenLogError(){
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(nonFederationAccount);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(false);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        accountRequestService.processRequest("Test", mockBody);
    }

    @Test
    public void processRequest_IfEventTypeIsNotRegistration_thenLogError(){
        //setup
        String mockBody = "{\"events\":[{\"type\":\"undefined\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(nonFederationAccount);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        accountRequestService.processRequest("Test", mockBody);
    }

    @Test
    public void processRequest_IfRawBodyHasNoEvents_thenLogError(){
        //setup
        String mockBody = "{\"events\":[]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(nonFederationAccount);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        accountRequestService.processRequest("Test", mockBody);
    }

    @Test
    public void processRequest_IfAccountIsNull_thenLogError(){
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(null);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        accountRequestService.processRequest("Test", mockBody);
    }

    @Test
    public void processRequest_IfGivenAnInvalidUid_thenCatchException(){
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenThrow(Exception.class);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\"`:\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        accountRequestService.processRequest("Test", mockBody);
    }

    @Test
    public void processRequest_IfGivenAccountToNotify_ThenMakePostRequest() throws JSONException, IOException {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        String mockAccountToNotify = "Test Account";
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        Mockito.when(accountInfoHandler.prepareForProfileInfoNotification(any())).thenReturn(mockAccountToNotify);
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(AccountInfoUtils.getFederatedAccount());
        doNothing().when(updateAccountService).updateLegacyDataInCDC(any(), any());

        //execution
        accountRequestService.processRequest("Test", mockBody);

        //validation
        Mockito.verify(notificationService).postRequest(any(), any());
    }

    @Test
    public void processRequest_IfGivenUIDAndEmail_ThenUpdateLegacyDataInCDC() throws JSONException, IOException {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        String mockAccountToNotify = "Test Account";
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        Mockito.when(accountInfoHandler.prepareForProfileInfoNotification(any())).thenReturn(mockAccountToNotify);
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(AccountInfoUtils.getFederatedAccount());
        doNothing().when(updateAccountService).updateLegacyDataInCDC(any(), any());

        //execution
        accountRequestService.processRequest("Test", mockBody);

        //validation
        Mockito.verify(updateAccountService).updateLegacyDataInCDC(any(), any());
    }

    @Test
    public void processRequest_IfGivenAccount_ThenSendNotificationToGRP() throws JSONException, IOException{
        //setup
        ReflectionTestUtils.setField(accountRequestService,"regNotificationUrl", "http://google.com");
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        String mockAccountToNotify = "Test Account";
        CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class, Mockito.RETURNS_DEEP_STUBS);
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream("".getBytes()));

        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(AccountInfoUtils.getFederatedAccount());
        Mockito.when(accountInfoHandler.prepareForProfileInfoNotification(any())).thenReturn(mockAccountToNotify);
        Mockito.when(mockResponse.getEntity()).thenReturn(entity);
        Mockito.when(mockResponse.getStatusLine().getStatusCode()).thenReturn(200);
        Mockito.when(notificationService.postRequest(anyString(), anyString())).thenReturn(mockResponse);
        doNothing().when(mockResponse).close();
        doNothing().when(updateAccountService).updateLegacyDataInCDC(any(), any());
        Mockito.when(accountInfoHandler.prepareForGRPNotification(any())).thenCallRealMethod();
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);

        //execution
        accountRequestService.processRequest("Test", mockBody);

        //validation
        Mockito.verify(snsHandler).sendSNSNotification(anyString());
    }
}
