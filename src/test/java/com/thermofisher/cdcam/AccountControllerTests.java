package com.thermofisher.cdcam;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.controller.AccountController;
import com.thermofisher.cdcam.model.AccountInfo;
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
import static org.mockito.ArgumentMatchers.anyString;

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

    AccountInfo federationAccount = AccountInfo.builder()
            .username("federatedUser@OIDC.com")
            .emailAddress("federatedUser@OIDC.com")
            .firstName("first")
            .lastName("last")
            .country("country")
            .localeName("en_US")
            .loginProvider("oidc")
            .password("Password1")
            .regAttepmts(0)
            .city("testCity")
            .department("dep")
            .company("myCompany")
            .build();

    AccountInfo nonFederationAccount = AccountInfo.builder()
            .username("User@test.com")
            .emailAddress("User@test.com")
            .firstName("first")
            .lastName("last")
            .country("country")
            .localeName("en_US")
            .loginProvider("site")
            .password("Password1")
            .regAttepmts(0)
            .city("testCity")
            .department("dep")
            .company("myCompany")
            .build();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void notifyRegistration_ifFederationUserUIDisSent_returnFederationAccount(){
       //setup
        Mockito.when(accounts.getAccount(anyString())).thenReturn(federationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);
        //execution
        ResponseEntity<String> res = notificationController.notifyRegistration();
        //validation
        Assert.assertTrue(res.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void notifyRegistration_ifNonFederationUserUIDisSent_returnError(){
        //setup
        Mockito.when(accounts.getAccount(anyString())).thenReturn(nonFederationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);
        //execution
        ResponseEntity<String> res = notificationController.notifyRegistration();
        //validation
        Assert.assertEquals(res.getBody(), "NON FEDERATION USER");
    }

    @Test
    public void notifyRegistration_ifConnectionIsLost_throwException(){
        //setup
        Mockito.when(notificationController.notifyRegistration()).thenThrow(Exception.class);
        //execution
        ResponseEntity<String> res = notificationController.notifyRegistration();
        //validation
        Assert.assertEquals(res.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Test
    public void notifyRegistration_ifSNSNotificationFails_returnServiceUnavailable(){
        //setup
        Mockito.when(accounts.getAccount(anyString())).thenReturn(federationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(false);
        //execution
        ResponseEntity<String> res = notificationController.notifyRegistration();
        //validation
        Assert.assertEquals(res.getStatusCode(), HttpStatus.SERVICE_UNAVAILABLE);
    }
}
