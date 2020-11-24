package com.thermofisher.cdcam;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.controller.AccountsController;
import com.thermofisher.cdcam.enums.RegistrationType;
import com.thermofisher.cdcam.model.*;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.dto.UsernameRecoveryDTO;
import com.thermofisher.cdcam.services.AccountRequestService;
import com.thermofisher.cdcam.services.EmailService;
import com.thermofisher.cdcam.services.UpdateAccountService;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.EmailRequestBuilderUtils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
import com.thermofisher.cdcam.model.dto.AccountInfoDTO;
import com.thermofisher.cdcam.services.ReCaptchaService;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;

import org.json.JSONException;
import org.json.JSONObject;
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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class AccountsControllerTests {
    private final List<String> uids = new ArrayList<>();
    private final String username = "federatedUser@OIDC.com";
    private final String firstName = "first";
    private final String lastName = "last";
    private final UserTimezone emptyUserTimezone = UserTimezone.builder().uid("").timezone("").build();
    private final UserTimezone validUserTimezone = UserTimezone.builder().uid("1234567890").timezone("America/Tijuana")
            .build();
    private final UserTimezone invalidUserTimezone = UserTimezone.builder().uid("1234567890").timezone(null).build();
    private final int associatedAccounts = 1;
    private final UsernameRecoveryDTO usernameRecoveryDTO = EmailRequestBuilderUtils.buildUsernameRecoveryDTO();

    @InjectMocks
    AccountsController accountsController;

    @Mock
    CDCResponseHandler cdcResponseHandler;

    @Mock
    EmailService emailService;

    @Mock
    LiteRegHandler mockLiteRegHandler;

    @Mock
    UsersHandler usersHandler;

    @Mock
    AccountRequestService accountRequestService;

    @Mock
    ReCaptchaService reCaptchaService;

    @Mock
    UpdateAccountService updateAccountService;

    private CDCResponseData getValidCDCResponse(String uid) {
        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID(uid);
        cdcResponseData.setStatusCode(200);
        cdcResponseData.setStatusReason("");
        return cdcResponseData;
    }

    JSONObject reCaptchaResponse;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        reCaptchaResponse = new JSONObject();
        ReflectionTestUtils.setField(accountsController, "RECAPTCHA_MIN_THRESHOLD", 0.5);
        uids.add("001");
        uids.add("002");
        uids.add("003");
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListEmpty_returnBadRequest() {
        // given
        List<String> emails = new ArrayList<>();
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListNull_returnBadRequest() {
        // given
        EmailList emailList = EmailList.builder().emails(null).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListHasValues_returnOK() throws IOException {
        // given
        List<EECUser> mockResult = new ArrayList<>();
        mockResult.add(Mockito.mock(EECUser.class));
        Mockito.when(mockLiteRegHandler.process(any())).thenReturn(mockResult);
        mockLiteRegHandler.requestLimit = 1000;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void emailOnlyRegistration_WhenHandlerProcessThrowsException_returnInternalServerError() throws IOException {
        // given
        when(mockLiteRegHandler.process(any())).thenThrow(IOException.class);
        mockLiteRegHandler.requestLimit = 1000;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void emailOnlyRegistration_WhenRequestLimitExceeded_returnBadRequest() throws IOException {
        // given
        Mockito.when(mockLiteRegHandler.process(any())).thenThrow(IOException.class);

        mockLiteRegHandler.requestLimit = 1;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenRequestHeaderInvalid_returnBadRequest() {
        // given
        EmailList emailList = EmailList.builder().emails(null).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getUsers_GivenAValidListOfUID_ShouldReturnUserDetails() throws IOException {
        // given
        List<UserDetails> userDetailsList = new ArrayList<>();
        userDetailsList.add(UserDetails.builder().uid(uids.get(0)).email(username).firstName(firstName)
                .lastName(lastName).associatedAccounts(associatedAccounts).build());
        userDetailsList.add(UserDetails.builder().uid(uids.get(1)).email(username).firstName(firstName)
                .lastName(lastName).associatedAccounts(associatedAccounts).build());
        userDetailsList.add(UserDetails.builder().uid(uids.get(2)).email(username).firstName(firstName)
                .lastName(lastName).associatedAccounts(associatedAccounts).build());
        Mockito.when(usersHandler.getUsers(uids)).thenReturn(userDetailsList);

        // when
        ResponseEntity<List<UserDetails>> resp = accountsController.getUsers(uids);

        // then
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.OK);

    }

    @Test
    public void getUsers_GivenAnIOError_returnInternalServerError() throws IOException {
        // given
        Mockito.when(usersHandler.getUsers(uids)).thenThrow(Exception.class);

        // when
        ResponseEntity<List<UserDetails>> resp = accountsController.getUsers(uids);

        // then
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Test
    public void handleHttpMessageNotReadableExceptions_givenHttpMessageNotReadableException_ReturnErrorMessage() {
        // given
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("");

        // when
        String resp = accountsController.handleHttpMessageNotReadableExceptions(ex);

        // then
        Assert.assertEquals(resp, "Invalid input format. Message not readable.");
    }

    @Test
    public void handleHttpMessageNotReadableExceptions_givenParseException_ReturnErrorMessage() {
        // given
        ParseException ex = new ParseException(1);

        // when
        String resp = accountsController.handleHttpMessageNotReadableExceptions(ex);

        // then
        Assert.assertEquals(resp, "Invalid input format. Message not readable.");
    }

    @Test
    public void notifyRegistration_givenMethodCalled_returnOk() {
        // given
        doNothing().when(accountRequestService).processRequest(any(), any());

        // when
        ResponseEntity<String> response = accountsController.notifyRegistration("test", "test");

        // then
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void newAccount_givenAValidAccountIsReceived_WhenTheTokenIsInvalid_returnBadRequest()
            throws JSONException, IOException {
        // given
        reCaptchaResponse.put("success", false);
        reCaptchaResponse.put("score", 0.5);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();


        // then
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

        // then
        Assert.assertEquals(response.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void newAccount_givenAValidAccountIsReceived_WhenTheReCaptchaTokenIsValidAndScoreIsEqualsToMinThreshold_ThenARegistrationShouldBeMade()
            throws JSONException, IOException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.processRegistrationRequest(any())).thenReturn(cdcResponseData);

        // then
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

        // then
        Assert.assertEquals(response.getBody().getUID(), AccountUtils.uid);
    }

     @Test
     public void newAccount_givenAValidAccountIsReceived_WhenTheReCaptchaTokenIsValidButTheScoreIsLessThanMinThreshold_returnBadRequest()
             throws JSONException, IOException {
         // given
         reCaptchaResponse.put("success", true);
         reCaptchaResponse.put("score", 0.3);
         when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
         AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();

         // then
         ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

         // then
         Assert.assertEquals(response.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
     }

    @Test
    public void newAccount_givenAnAccountWithBlankPassword_returnBadRequest() throws IOException, JSONException {
        // given
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setPassword("");

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void newAccount_givenABackendError_returnInternalServerError() throws IOException, JSONException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(accountRequestService.processRegistrationRequest(any())).thenReturn(null);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

        // then
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void newAccount_givenAValidAccount_returnUID() throws IOException, JSONException {
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.processRegistrationRequest(any())).thenReturn(cdcResponseData);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

        // then
        Assert.assertEquals(response.getBody().getUID(), AccountUtils.uid);
    }

    @Test
    public void newAccount_givenAValidAccount_And_RegistrationTypeIsBasic_sendConfirmationEmailShouldBeCalled() throws IOException,
            JSONException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setRegistrationType(RegistrationType.BASIC.getValue());
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.processRegistrationRequest(any())).thenReturn(cdcResponseData);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

        // then
        Assert.assertEquals(response.getBody().getUID(), AccountUtils.uid);
        verify(accountRequestService, times(1)).sendConfirmationEmail(any());
    }

    @Test
    public void newAccount_givenAValidAccount_And_RegistrationTypeIsNotBasic_sendConfirmationEmailShouldNotBeCalled() throws IOException,
            JSONException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setRegistrationType("dummy");
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.processRegistrationRequest(any())).thenReturn(cdcResponseData);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

        // then
        Assert.assertEquals(response.getBody().getUID(), AccountUtils.uid);
        verify(accountRequestService, times(0)).sendConfirmationEmail(any());
    }

    @Test
    public void newAccount_givenRegistrationSuccessful_sendVerificationEmailShouldBeCalled() throws IOException,
            JSONException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.processRegistrationRequest(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO);

        // then
        verify(accountRequestService, times(1)).sendVerificationEmail(any());
    }

    @Test
    public void newAccount_givenRegistrationNotSuccessful_sendVerificationEmailShouldNotBeCalled() throws IOException,
            JSONException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setStatusCode(400);
        cdcResponseData.setStatusReason("");
        when(accountRequestService.processRegistrationRequest(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO);

        // then
        verify(accountRequestService, times(0)).sendVerificationEmail(any());
    }

    @Test
    public void newAccount_givenAValidAccount_And_RegistrationFails_nullUIDisReturned() throws IOException,
            JSONException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setStatusCode(400);
        cdcResponseData.setStatusReason("");
        Mockito.when(accountRequestService.processRegistrationRequest(any())).thenReturn(cdcResponseData);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

        // then
        Assert.assertEquals(response.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void updateTimezone_GivenEmptyUserUIDOrTimezoneShouldReturnBadRequest() throws Exception {
        // given
        Mockito.when(updateAccountService.updateTimezoneInCDC(emptyUserTimezone.getUid(), emptyUserTimezone.getTimezone())).thenReturn(HttpStatus.BAD_REQUEST);

        // when
        ResponseEntity<String> resp = accountsController.setTimezone(emptyUserTimezone);

        // then
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void updateTimezone_GivenAValidUserUIDAndTimezoneShouldReturnOK() throws Exception {
        // given
        Mockito.when(updateAccountService.updateTimezoneInCDC(any(String.class), any(String.class))).thenReturn(HttpStatus.OK);

        // when
        ResponseEntity<String> resp = accountsController.setTimezone(validUserTimezone);

        // then
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void updateTimezone_MissingRequestBodyParamShouldReturnBadRequest() throws Exception {
        // given
        Mockito.when(updateAccountService.updateTimezoneInCDC(invalidUserTimezone.getUid(), null)).thenReturn(HttpStatus.BAD_REQUEST);

        // when
        ResponseEntity<String> resp = accountsController.setTimezone(invalidUserTimezone);

        // then
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void sendVerificationEmail_shouldSearchForAccountInfoByEmail() throws IOException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        when(cdcResponseHandler.getAccountInfoByEmail(anyString())).thenReturn(accountInfo);

        // when
        accountsController.sendRecoverUsernameEmail(usernameRecoveryDTO);

        // then
        verify(cdcResponseHandler).getAccountInfoByEmail(anyString());
    }

    @Test
    public void sendVerificationEmail_shouldSendUsernameRecoveryEmail() throws IOException {
        // given
        EmailSentResponse response = EmailSentResponse.builder().statusCode(200).build();
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        when(cdcResponseHandler.getAccountInfoByEmail(anyString())).thenReturn(accountInfo);
        when(emailService.sendUsernameRecoveryEmail(any())).thenReturn(response);

        // when
        accountsController.sendRecoverUsernameEmail(usernameRecoveryDTO);

        // then
        verify(emailService).sendUsernameRecoveryEmail(any());
    }
    
    @Test
    public void sendVerificationEmail_WhenResponseReceived_ReturnSameStatus() {
        // given
        HttpStatus mockStatus = HttpStatus.OK;

        CDCResponseData mockResponse = Mockito.mock(CDCResponseData.class);
        when(mockResponse.getStatusCode()).thenReturn(mockStatus.value());

        when(accountRequestService.sendVerificationEmailSync(any())).thenReturn(mockResponse);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.sendVerificationEmail("test");

        // then
        Assert.assertEquals(response.getStatusCode(), mockStatus);
    }

    @Test
    public void newAccount_givenAnAccountWithLongFirstName_returnBadRequest() throws IOException, JSONException {
        // given
        final String LONG_FIRST_NAME = "SKILZAGkGrDySoz7ikrkuTePmXUm5DG";
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setFirstName(LONG_FIRST_NAME);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongLastName_returnBadRequest() throws IOException, JSONException {
        // given
        final String LONG_LAST_NAME = "SKILZAGkGrDySoz7ikrkuTePmXUm5DG";
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setLastName(LONG_LAST_NAME);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongEmail_returnBadRequest() throws IOException, JSONException {
        // given
        final String LONG_EMAIL = "dOKzIGsinJuhzqJ6CJwv2aKf2BNSGy1atgH1L3P@DokSb2xW4sKP.com";
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setEmailAddress(LONG_EMAIL);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongPassword_returnBadRequest() throws IOException, JSONException {
        // given
        final String LONG_PASSWORD = "z09H4j9QqlwDleHaM8N4t";
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setPassword(LONG_PASSWORD);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongCompany_returnBadRequest() throws IOException, JSONException {
        // given
        final String LONG_COMPANY = "199hvjoVi3t7QSF676unFTfLmbBWiJ3nmb0kXmfrr2Mu3DrJoEN";
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setCompany(LONG_COMPANY);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongDepartment_returnBadRequest() throws IOException, JSONException {
        // given
        final String LONG_DEPARTMENT = "199hvjoVi3t7QSF676unFTfLmbBWiJ3nmb0kXmfrr2Mu3DrJoEN";
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setDepartment(LONG_DEPARTMENT);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongCity_returnBadRequest() throws IOException, JSONException {
        // given
        final String LONG_CITY = "TI1YPaFY9MAzdtUiHAP3cYfGWVDs11z";
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setCity(LONG_CITY);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void isAvailableLoginID_GivenIdIsAvailable_ItShouldReturn404Response() throws Exception {
        // given
        String loginID = "test@mail.com";
        when(cdcResponseHandler.isAvailableLoginID(any())).thenReturn(true);

        // when
        ResponseEntity<?> response = accountsController.isAvailableLoginID(loginID);

        // then
        assertTrue(response.getStatusCode().value() == HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void isAvailableLoginID_GivenIdIsNotAvailable_ItShouldReturn200Response() throws Exception {
        // given
        String loginID = "test@mail.com";
        when(cdcResponseHandler.isAvailableLoginID(any())).thenReturn(false);

        // when
        ResponseEntity<?> response = accountsController.isAvailableLoginID(loginID);

        // then
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }
}
