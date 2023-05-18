package com.thermofisher.cdcam.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.controller.EmailAccountsController;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EECUserV2;
import com.thermofisher.cdcam.model.EECUserV3;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.dto.LiteAccountDTO;
import com.thermofisher.cdcam.utils.cdc.LiteRegistrationService;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class EmailAccountsControllerTests {

    private static final String TEST_EMAIL = "TEST-EMAIL";

    @InjectMocks
    EmailAccountsController emailAccountsController;

    @Mock
    LiteRegistrationService liteRegistrationService;

    private void setProperties() {
        ReflectionTestUtils.setField(emailAccountsController, "requestLimit", 1000);
    }
    
    @Test
    public void emailOnlyRegistration_WhenEmailListEmpty_returnBadRequest() {
        // given
        List<String> emails = new ArrayList<>();
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = emailAccountsController.emailOnlyRegistration(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_whenIllegalArgumentExceptionIsThrown_returnBadRequest() throws IOException {
        // given
        List<String> emails = new ArrayList<>();
        EmailList emailList = EmailList.builder().emails(emails).build();
        doThrow(new IllegalArgumentException()).when(liteRegistrationService).createLiteAccountsV1(emailList);

        // when
        ResponseEntity<List<EECUser>> res = emailAccountsController.emailOnlyRegistration(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListNull_returnBadRequest() {
        // given
        EmailList emailList = EmailList.builder().emails(null).build();

        // when
        ResponseEntity<List<EECUser>> res = emailAccountsController.emailOnlyRegistration(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListHasValues_returnOK() throws IOException {
        // given
        setProperties();
        List<EECUser> mockResult = new ArrayList<>();
        mockResult.add(Mockito.mock(EECUser.class));
        Mockito.when(liteRegistrationService.createLiteAccountsV1(any())).thenReturn(mockResult);
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = emailAccountsController.emailOnlyRegistration(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void emailOnlyRegistration_WhenHandlerProcessThrowsException_returnInternalServerError() throws IOException {
        // given
        setProperties();
        when(liteRegistrationService.createLiteAccountsV1(any())).thenThrow(IOException.class);
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = emailAccountsController.emailOnlyRegistration(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void emailOnlyRegistration_WhenRequestLimitExceeded_returnBadRequest() throws IOException {
        // given
        ReflectionTestUtils.setField(emailAccountsController, "requestLimit", 1);
        Mockito.when(liteRegistrationService.createLiteAccountsV1(any())).thenThrow(IOException.class);

        List<String> emails = new ArrayList<>();
        emails.add("email1");
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = emailAccountsController.emailOnlyRegistration(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenRequestHeaderInvalid_returnBadRequest() {
        // given
        EmailList emailList = EmailList.builder().emails(null).build();

        // when
        ResponseEntity<List<EECUser>> res = emailAccountsController.emailOnlyRegistration(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenIllegalArgumentExceptionIsThrown_ThenReturnBadRequest() throws IOException {
        // given
        setProperties();
        Mockito.when(liteRegistrationService.createLiteAccountsV1(any())).thenThrow(IllegalArgumentException.class);

        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = emailAccountsController.emailOnlyRegistration(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void register_WhenEmailListEmpty_returnBadRequest() {
        // given
        List<String> emails = new ArrayList<>();
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUserV2>> res = emailAccountsController.register(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void register_whenIllegalArgumentExceptionIsThrown_returnBadRequest() throws IOException {
        // given
        List<String> emails = new ArrayList<>();
        EmailList emailList = EmailList.builder().emails(emails).build();
        doThrow(new IllegalArgumentException()).when(liteRegistrationService).registerEmailAccounts(emailList);

        // when
        ResponseEntity<List<EECUserV2>> res = emailAccountsController.register(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void register_WhenEmailListNull_returnBadRequest() {
        // given
        EmailList emailList = EmailList.builder().emails(null).build();

        // when
        ResponseEntity<List<EECUserV2>> res = emailAccountsController.register(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void register_WhenEmailListHasValues_returnOK() throws IOException {
        // given
        setProperties();
        List<EECUserV2> mockResult = new ArrayList<>();
        mockResult.add(Mockito.mock(EECUserV2.class));
        Mockito.when(liteRegistrationService.registerEmailAccounts(any())).thenReturn(mockResult);
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUserV2>> res = emailAccountsController.register(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void register_WhenHandlerProcessThrowsException_returnInternalServerError() throws IOException {
        // given
        setProperties();
        when(liteRegistrationService.registerEmailAccounts(any())).thenThrow(IOException.class);
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUserV2>> res = emailAccountsController.register(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void register_WhenRequestLimitExceeded_returnBadRequest() throws IOException {
        // given
        ReflectionTestUtils.setField(emailAccountsController, "requestLimit", 1);
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUserV2>> res = emailAccountsController.register(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void register_WhenRequestHeaderInvalid_returnBadRequest() {
        // given
        EmailList emailList = EmailList.builder().emails(null).build();

        // when
        ResponseEntity<List<EECUserV2>> res = emailAccountsController.register(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void register_WhenIllegalArgumentExceptionIsThrown_ThenReturnBadRequest() throws IOException {
        // given
        setProperties();
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();
        when(liteRegistrationService.registerEmailAccounts(any())).thenThrow(IllegalArgumentException.class);

        // when
        ResponseEntity<List<EECUserV2>> res = emailAccountsController.register(emailList);

        // then
        assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void addLiteAccount_WhenValidRequest_ThenReturnOK() throws IllegalArgumentException {
        // given
        setProperties();
        List<LiteAccountDTO> request = Collections.singletonList(LiteAccountDTO.builder().email(TEST_EMAIL).build());
        when(liteRegistrationService.registerLiteAccounts(request)).thenReturn(Collections.singletonList(EECUserV3.builder().email(TEST_EMAIL).build()));
        
        // when 
        ResponseEntity<List<EECUserV3>> response = emailAccountsController.addLiteAccount(request);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody().get(0).getEmail(), TEST_EMAIL);
    }

    @Test
    public void addLiteAccount_WhenExceptionOccurred_ThenReturnInternalServerError() throws IllegalArgumentException {
        // given
        setProperties();
        List<LiteAccountDTO> request = Collections.singletonList(LiteAccountDTO.builder().email(TEST_EMAIL).build());
        when(liteRegistrationService.registerLiteAccounts(request)).thenThrow(RuntimeException.class);
        
        // when 
        ResponseEntity<List<EECUserV3>> response = emailAccountsController.addLiteAccount(request);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
