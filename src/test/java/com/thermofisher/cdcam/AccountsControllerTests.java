package com.thermofisher.cdcam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.controller.AccountsController;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.CDCUserUpdate;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.services.HashValidationService;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import org.json.simple.parser.ParseException;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class AccountsControllerTests {
    private String header = "test";
    private final String uid = "c1c691f4-556b-4ad1-ab75-841fc4e94dcd";
    private final String username = "federatedUser@OIDC.com";
    private final String hashedString = "QJERFC2183DASJ=";

    @InjectMocks
    AccountsController accountsController;

    @Mock
    CDCAccounts cdcAccounts;

    @Mock
    LiteRegHandler mockLiteRegHandler;

    @Mock
    SecretsManager secretsManager;

    @Mock
    HashValidationService hashValidationService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn(hashedString);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListEmpty_returnBadRequest()
            throws JsonProcessingException, ParseException {
        // given
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        List<String> emails = new ArrayList<>();
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListNull_returnBadRequest()
            throws JsonProcessingException, ParseException {
        // given
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        EmailList emailList = EmailList.builder().emails(null).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListHasValues_returnOK() throws IOException, ParseException {
        // given
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        List<EECUser> mockResult = new ArrayList<>();
        mockResult.add(Mockito.mock(EECUser.class));
        Mockito.when(mockLiteRegHandler.process(any())).thenReturn(mockResult);
        mockLiteRegHandler.requestLimit = 1000;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void emailOnlyRegistration_WhenHandlerProcessThrowsException_returnInternalServerError() throws IOException, ParseException {
        // given
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        when(mockLiteRegHandler.process(any())).thenThrow(IOException.class);
        mockLiteRegHandler.requestLimit = 1000;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void emailOnlyRegistration_WhenRequestLimitExceeded_returnBadRequest() throws IOException, ParseException {
        // given
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(mockLiteRegHandler.process(any())).thenThrow(IOException.class);
        mockLiteRegHandler.requestLimit = 1;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenRequestHeaderInvalid_returnBadRequest() throws IOException, ParseException {
        // given
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(false);
        EmailList emailList = EmailList.builder().emails(null).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void userUpdate_WhenHashSignatureIsDifferent_returnBadRequest() throws JsonProcessingException, ParseException {
        // given
        String invalidHash = String.format("%s=extraInvalidCharacters", hashedString);
        AccountInfo account = AccountInfo.builder().uid(uid).username(username).emailAddress(username).build();
        CDCUserUpdate user = CDCUserUpdate.builder().uid(uid).username(username).regStatus(true).build();
        Mockito.when(cdcAccounts.getAccount(user.getUid())).thenReturn(account);

        // when
        ResponseEntity<String> res = accountsController.updateUser(invalidHash, user);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    public void userUpdate_WhenUsernameIsDifferentThanCDCEmail_returnBadRequest() throws JsonProcessingException, ParseException {
        // given
        AccountInfo account = AccountInfo.builder().uid(uid).username(username).emailAddress(username).build();
        CDCUserUpdate user = CDCUserUpdate.builder().uid("randomuid").username(username).regStatus(true).build();
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(cdcAccounts.getAccount(user.getUid())).thenReturn(account);

        // when
        ResponseEntity<String> res = accountsController.updateUser(header, user);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }
}
