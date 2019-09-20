package com.thermofisher.cdcam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.controller.AccountsController;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
@Ignore
public class AccountsControllerTests {

    private String header = "test";

    @InjectMocks
    AccountsController accountsController;

    @Mock
    LiteRegHandler mockLiteRegHandler;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListEmpty_returnBadRequest() throws JsonProcessingException, ParseException {
        List<String> emails = new ArrayList<>();
        EmailList emailList = EmailList.builder().emails(emails).build();
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListNull_returnBadRequest() throws JsonProcessingException, ParseException {
        EmailList emailList = EmailList.builder().emails(null).build();
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListHasValues_returnOK() throws IOException, ParseException {
        List<EECUser> mockResult = new ArrayList<>();
        mockResult.add(Mockito.mock(EECUser.class));
        when(mockLiteRegHandler.process(any())).thenReturn(mockResult);
        mockLiteRegHandler.requestLimit = 1000;

        List<String> emails = new ArrayList<>();
        emails.add("email1");

        EmailList emailList = EmailList.builder().emails(emails).build();
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);
        Assert.assertEquals(res.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void emailOnlyRegistration_WhenHandlerProcessThrowsException_returnInternalServerError() throws IOException, ParseException {
        when(mockLiteRegHandler.process(any())).thenThrow(IOException.class);
        mockLiteRegHandler.requestLimit = 1000;

        List<String> emails = new ArrayList<>();
        emails.add("email1");

        EmailList emailList = EmailList.builder().emails(emails).build();
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);
        Assert.assertEquals(res.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void emailOnlyRegistration_WhenRequestLimitExceeded_returnBadRequest() throws IOException, ParseException {
        when(mockLiteRegHandler.process(any())).thenThrow(IOException.class);
        mockLiteRegHandler.requestLimit = 1;

        List<String> emails = new ArrayList<>();
        emails.add("email1");
        emails.add("email1");

        EmailList emailList = EmailList.builder().emails(emails).build();
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }
}
