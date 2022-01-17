package com.thermofisher.cdcam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.gigya.socialize.GSKeyNotFoundException;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.controller.AccountsController;
import com.thermofisher.cdcam.enums.RegistrationType;
import com.thermofisher.cdcam.enums.aws.CdcamSecrets;
import com.thermofisher.cdcam.enums.cdc.WebhookEvent;
import com.thermofisher.cdcam.model.AccountAvailabilityResponse;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.model.UserTimezone;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.dto.AccountInfoDTO;
import com.thermofisher.cdcam.model.dto.CIPAuthDataDTO;
import com.thermofisher.cdcam.model.dto.ChangePasswordDTO;
import com.thermofisher.cdcam.model.dto.MarketingConsentDTO;
import com.thermofisher.cdcam.model.dto.ProfileInfoDTO;
import com.thermofisher.cdcam.model.dto.UsernameRecoveryDTO;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaLowScoreException;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaUnsuccessfulResponseException;
import com.thermofisher.cdcam.services.AccountRequestService;
import com.thermofisher.cdcam.services.CookieService;
import com.thermofisher.cdcam.services.DataProtectionService;
import com.thermofisher.cdcam.services.JWTValidator;
import com.thermofisher.cdcam.services.NotificationService;
import com.thermofisher.cdcam.services.ReCaptchaService;
import com.thermofisher.cdcam.services.SecretsService;
import com.thermofisher.cdcam.services.UpdateAccountService;
import com.thermofisher.cdcam.services.hashing.HashingService;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.EmailRequestBuilderUtils;
import com.thermofisher.cdcam.utils.IdentityProviderUtils;
import com.thermofisher.cdcam.utils.PasswordUtils;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
import com.thermofisher.cdcam.utils.cdc.CDCTestsUtils;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
    private final UserTimezone validUserTimezone = UserTimezone.builder().uid("1234567890").timezone("America/Tijuana").build();
    private final UserTimezone invalidUserTimezone = UserTimezone.builder().uid("1234567890").timezone(null).build();
    private final UsernameRecoveryDTO usernameRecoveryDTO = EmailRequestBuilderUtils.buildUsernameRecoveryDTO();
    private final int associatedAccounts = 1;
    private final String CIPHERTEXT = "VTJGc2RHVmtYMStuTTlOT3ExeHZtNG5rUHpqNjhMTmhKbHRkVkJZU0xlMnpGTm5QVk1oV0oycUhrVm1JQ2ozSXJVUWdCK2pBaDBuczMyM0ZMYkdLVm1tSzJ4R3BENmtJZ1VGbm1JeURVMUNpSkcxcU1aNzBvRjBJeG80dHVCbHhmdU02TDJFMmtLUDdvUzdGMWpidU53PT0=";
    private final String COOKIE_CIP_AUTHDATA_VALID = "eyJjbGllbnRJZCI6ImNsaWVudElkIiwicmVkaXJlY3RVcmkiOiJyZWRpcmVjdFVyaSIsInN0YXRlIjoic3RhdGUiLCJzY29wZSI6InNjb3BlIiwicmVzcG9uc2VUeXBlIjoicmVzcG9uc2VUeXBlIn0=";
    private final MarketingConsentDTO marketingConsentDTO = MarketingConsentDTO.builder()
        .city("city")
        .company("company")
        .country("country")
        .consent(true)
        .build();
    private final ProfileInfoDTO profileInfoDTO = ProfileInfoDTO.builder()
            .uid("1234567890")
            .firstName("firstName")
            .lastName("lastName")
            .email("email@test.com")
            .username("username")
            .marketingConsentDTO(marketingConsentDTO)
            .build();

    @InjectMocks
    AccountsController accountsController;

    @Mock
    AccountRequestService accountRequestService;

    @Mock
    CDCResponseHandler cdcResponseHandler;

    @Mock
    CookieService cookieService;

    @Mock
    DataProtectionService dataProtectionService;

    @Mock
    LiteRegHandler liteRegHandler;

    @Mock
    NotificationService notificationService;

    @Mock
    ReCaptchaService reCaptchaService;

    @Mock
    SecretsService secretsService;

    @Mock
    UpdateAccountService updateAccountService;

    @Mock
    UsersHandler usersHandler;

    @Captor
    ArgumentCaptor<String> reCaptchaSecretCaptor;

    @Captor
    ArgumentCaptor<AccountInfo> accountInfoCaptor;

    private CDCResponseData getValidCDCResponse(String uid) {
        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID(uid);
        cdcResponseData.setStatusCode(200);
        cdcResponseData.setStatusReason("");
        return cdcResponseData;
    }

    private CDCResponseData getEmailVerificationCDCResponse(String uid) {
        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID(uid);
        cdcResponseData.setStatusCode(206);
        cdcResponseData.setErrorCode(206006);
        cdcResponseData.setStatusReason("Partial Content");
        return cdcResponseData;
    }
    
    private JSONObject getValidDecryptionResponse(String ciphertext) throws JSONException {
        JSONObject decryptionResponse = new JSONObject();
        decryptionResponse.put("statusCode", 200);
        JSONObject body = new JSONObject();
        body.put("firstName", "John");
        body.put("lastName", "Doe");
        body.put("email", "john.doe@mail.com");
        decryptionResponse.put("body", body);
        return decryptionResponse;
    }

    JSONObject reCaptchaResponse;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(accountsController, "isLegacyEmailVerificationEnabled", true);
        reCaptchaResponse = new JSONObject();
        uids.add("001");
        uids.add("002");
        uids.add("003");
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());
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
    public void emailOnlyRegistration_whenIllegalArgumentExceptionIsThrown_returnBadRequest() throws IOException {
        // given
        List<String> emails = new ArrayList<>();
        EmailList emailList = EmailList.builder().emails(emails).build();
        doThrow(new IllegalArgumentException()).when(liteRegHandler).createLiteAccountsV1(emailList);

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
        Mockito.when(liteRegHandler.createLiteAccountsV1(any())).thenReturn(mockResult);
        liteRegHandler.requestLimit = 1000;
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
        when(liteRegHandler.createLiteAccountsV1(any())).thenThrow(IOException.class);
        liteRegHandler.requestLimit = 1000;
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
        Mockito.when(liteRegHandler.createLiteAccountsV1(any())).thenThrow(IOException.class);

        liteRegHandler.requestLimit = 1;
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
    public void emailOnlyRegistrationV2_WhenEmailListEmpty_returnBadRequest() {
        // given
        List<String> emails = new ArrayList<>();
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistrationV2(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistrationV2_whenIllegalArgumentExceptionIsThrown_returnBadRequest() throws IOException {
        // given
        List<String> emails = new ArrayList<>();
        EmailList emailList = EmailList.builder().emails(emails).build();
        doThrow(new IllegalArgumentException()).when(liteRegHandler).createLiteAccountsV2(emailList);

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistrationV2(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistrationV2_WhenEmailListNull_returnBadRequest() {
        // given
        EmailList emailList = EmailList.builder().emails(null).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistrationV2(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistrationV2_WhenEmailListHasValues_returnOK() throws IOException {
        // given
        List<EECUser> mockResult = new ArrayList<>();
        mockResult.add(Mockito.mock(EECUser.class));
        Mockito.when(liteRegHandler.createLiteAccountsV2(any())).thenReturn(mockResult);
        liteRegHandler.requestLimit = 1000;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistrationV2(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void emailOnlyRegistrationV2_WhenHandlerProcessThrowsException_returnInternalServerError() throws IOException {
        // given
        when(liteRegHandler.createLiteAccountsV2(any())).thenThrow(IOException.class);
        liteRegHandler.requestLimit = 1000;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistrationV2(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void emailOnlyRegistrationV2_WhenRequestLimitExceeded_returnBadRequest() throws IOException {
        // given
        Mockito.when(liteRegHandler.createLiteAccountsV2(any())).thenThrow(IOException.class);

        liteRegHandler.requestLimit = 1;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistrationV2(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistrationV2_WhenRequestHeaderInvalid_returnBadRequest() {
        // given
        EmailList emailList = EmailList.builder().emails(null).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistrationV2(emailList);

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
        Mockito.when(usersHandler.getUsers(uids)).thenThrow(IOException.class);

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
    public void onAccountRegistered_givenMethodCalled_WhenJWTIsValid_returnOk() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 1;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.REGISTRATION, numberOfWebhookEvents);
        when(cdcResponseHandler.getJWTPublicKey()).thenReturn(null);
        doNothing().when(accountRequestService).onAccountRegistered(any());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(true);

            // when
            ResponseEntity<String> response = accountsController.onAccountRegistered(jwt, body);

            // then
            Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Test
    public void onAccountRegistered_givenMethodCalled_WhenJWTIsNotValid_ThenOnAccountRegisteredShouldNotBeCalled() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 1;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.REGISTRATION, numberOfWebhookEvents);
        when(cdcResponseHandler.getJWTPublicKey()).thenReturn(null);
        doNothing().when(accountRequestService).onAccountRegistered(any());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(false);

            // when
            accountsController.onAccountRegistered(jwt, body);

            // then
            verify(accountRequestService, times(0)).onAccountRegistered(body);
        }
    }

    @Test
    public void onAccountRegistered_GivenTheMethodIsCalled_ThenAccountsRequestService_onAccountRegisteredMethodShouldBeCalledSameTimesAsWebhookEventsAmount() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 2;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.REGISTRATION, numberOfWebhookEvents);
        when(cdcResponseHandler.getJWTPublicKey()).thenReturn(null);
        doNothing().when(accountRequestService).onAccountRegistered(anyString());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(true);
            
            // when
            accountsController.onAccountRegistered(jwt, body);

            // then
            verify(accountRequestService, times(numberOfWebhookEvents)).onAccountRegistered(anyString());
        }
    }

    @Test
    public void newAccount_givenReCaptchaVersionIsV2_ThenReCaptchaServiceShouldGetCalledWithReCaptchaV2Secret()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, IOException {
        // given
        String expectedReCaptchaV2Secret = RandomStringUtils.random(10);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setIsReCaptchaV2(true);
        when(secretsService.get(CdcamSecrets.RECAPTCHAV2.getKey())).thenReturn(expectedReCaptchaV2Secret);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(reCaptchaService).verifyToken(anyString(), reCaptchaSecretCaptor.capture());
        String reCaptchaSecret = reCaptchaSecretCaptor.getValue();
        assertEquals(expectedReCaptchaV2Secret, reCaptchaSecret);
    }

    @Test
    public void newAccount_givenReCaptchaVersionIsV3_ThenReCaptchaServiceShouldGetCalledWithReCaptchaV3Secret()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, IOException {
        // given
        String expectedReCaptchaV3Secret = RandomStringUtils.random(10);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(secretsService.get(CdcamSecrets.RECAPTCHAV3.getKey())).thenReturn(expectedReCaptchaV3Secret);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(reCaptchaService).verifyToken(anyString(), reCaptchaSecretCaptor.capture());
        String reCaptchaSecret = reCaptchaSecretCaptor.getValue();
        assertEquals(expectedReCaptchaV3Secret, reCaptchaSecret);
    }

    @Test
    public void newAccount_givenReCaptchaVerificationThrowsReCaptchaLowScoreException_ThenResponseEntityShouldBeOfAcceptedType()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, IOException {
        // given
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(reCaptchaService.verifyToken(any(), any())).thenThrow(new ReCaptchaLowScoreException(""));

        // when
        ResponseEntity<?> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        assertEquals(response.getStatusCode().value(), HttpStatus.ACCEPTED.value());
    }

    @Test
    public void newAccount_givenReCaptchaVerificationThrowsReCaptchaUnsuccessfulException_ThenResponseEntityShouldBeOfBadRequestType()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, IOException {
        // given
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(reCaptchaService.verifyToken(any(), any())).thenThrow(new ReCaptchaUnsuccessfulResponseException(""));

        // when
        ResponseEntity<?> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        assertEquals(response.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void newAccount_givenReCaptchaVerificationThrowsJSONException_ThenResponseEntityShouldBeInternalServerError()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, IOException {
        // given
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(reCaptchaService.verifyToken(any(), any())).thenThrow(new JSONException(""));

        // when
        ResponseEntity<?> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        assertEquals(response.getStatusCode().value(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void newAccount_givenReCaptchaIsValid_ThenContinueWithRegistrationProcess() 
            throws IOException, JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.createAccount(any())).thenReturn(cdcResponseData);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(accountRequestService).createAccount(any());
    }

    @Test
    public void newAccount_givenAnAccountWithValidEncryptedDataIsProvided_ThenACallToDecryptShouldBeMade() throws JSONException, IOException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setCiphertext(CIPHERTEXT);
        JSONObject decryptionResponse = getValidDecryptionResponse(CIPHERTEXT);
        when(dataProtectionService.decrypt(any())).thenReturn(decryptionResponse);
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.createAccount(any())).thenReturn(cdcResponseData);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(dataProtectionService, times(1)).decrypt(CIPHERTEXT);
    }

    @Test
    public void newAccount_givenAnAccountDTOWithValidEncryptedDataIsProvided_ThenAccountInfoShouldBeSetWithDecryptionResponse() throws JSONException, IOException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setCiphertext(CIPHERTEXT);
        JSONObject decryptionResponse = getValidDecryptionResponse(CIPHERTEXT);
        accountDTO.setFirstName(decryptionResponse.getJSONObject("body").getString("firstName"));
        accountDTO.setLastName(decryptionResponse.getJSONObject("body").getString("lastName"));
        accountDTO.setEmailAddress(decryptionResponse.getJSONObject("body").getString("email"));


        AccountInfo accountInfo = AccountBuilder.buildFrom(accountDTO);

        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(dataProtectionService.decrypt(CIPHERTEXT)).thenReturn(decryptionResponse);
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.createAccount(any())).thenReturn(cdcResponseData);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        Assert.assertEquals(accountInfo.getFirstName(),accountDTO.getFirstName());
        Assert.assertEquals(accountInfo.getLastName(),accountDTO.getLastName());
        Assert.assertEquals(accountInfo.getEmailAddress(),accountDTO.getEmailAddress());
    }

    @Test
    public void newAccount_givenAnAccountWithBlankPassword_returnBadRequest() throws IOException, JSONException {
        // given
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setPassword("");

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void newAccount_GivenCipAuthDataCookiesIsValid_ShouldAssignProviderClientIdToAccount() throws IOException, JSONException, CustomGigyaErrorException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        CIPAuthDataDTO cipAuthDataDTO = cookieService.decodeCIPAuthDataCookie(COOKIE_CIP_AUTHDATA_VALID);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(accountRequestService.createAccount(any())).thenReturn(new CDCResponseData());

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(accountRequestService).createAccount(accountInfoCaptor.capture());
        AccountInfo capturedAccountInfo = accountInfoCaptor.getValue();
        assertEquals(cipAuthDataDTO.getClientId(), capturedAccountInfo.getOpenIdProviderId());
    }

    @Test
    public void newAccount_givenABackendError_returnInternalServerError()
            throws IOException, JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(accountRequestService.createAccount(any())).thenThrow(new CustomGigyaErrorException(""));

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        Assert.assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
    }

    @Test
    public void newAccount_givenAValidAccount_returnCdcResponseWithUID() throws IOException, JSONException, ReCaptchaLowScoreException,
            ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.createAccount(any())).thenReturn(cdcResponseData);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        Assert.assertEquals(response.getBody().getUID(), AccountUtils.uid);
    }

    @Test
    public void newAccount_givenEmailVerificationIsEnabled_thenShouldReturnAPartialContentErrorCode() throws IOException, JSONException, ReCaptchaLowScoreException,
            ReCaptchaUnsuccessfulResponseException, CustomGigyaErrorException {
        ReflectionTestUtils.setField(accountsController, "isLegacyEmailVerificationEnabled", false);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = getEmailVerificationCDCResponse(AccountUtils.uid);
        when(accountRequestService.createAccount(any())).thenReturn(cdcResponseData);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        Assert.assertEquals(cdcResponseData.getErrorCode(), response.getBody().getErrorCode());
    }

    @Test
    public void newAccount_GivenSuccessfulRegistration_ThenNotifyAccountRegistrationWithHashedPassword() throws CustomGigyaErrorException, IOException, JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException {
        // given
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(accountRequestService.createAccount(any())).thenReturn(cdcResponseData);
        doNothing().when(notificationService).sendAccountRegisteredNotification(any(), any());

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(notificationService).sendAccountRegisteredNotification(accountInfoCaptor.capture(), any());
        AccountInfo capturedAccount = accountInfoCaptor.getValue();
        String expectedPassword = HashingService.toMD5(accountDTO.getPassword());
        assertEquals(expectedPassword, capturedAccount.getPassword());
    }

    @Test
    public void newAccount_givenRegistrationSuccessfulAndEmailVerificationIsEnabled_sendVerificationEmailShouldBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        ReflectionTestUtils.setField(accountsController, "isLegacyEmailVerificationEnabled", true);
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(accountRequestService, times(1)).sendVerificationEmail(any());
    }
    
    @Test
    public void newAccount_givenRegistrationNotSuccessful_sendVerificationEmailShouldNotBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(accountRequestService.createAccount(any())).thenThrow(new CustomGigyaErrorException(""));
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(accountRequestService, times(0)).sendVerificationEmail(any());
    }

    @Test
    public void newAccount_givenRegistrationNotSuccessfulAndEmailVerificationIsDisabled_sendVerificationEmailShouldNotBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        ReflectionTestUtils.setField(accountsController, "isLegacyEmailVerificationEnabled", false);
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setStatusCode(400);
        cdcResponseData.setStatusReason("");
        when(accountRequestService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(accountRequestService, times(0)).sendVerificationEmail(any());
    }

    @Test
    public void newAccount_givenRegistrationIsValid_AndAspireFieldsAreNull_sendAspireSNSShouldNotBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setAcceptsAspireEnrollmentConsent(null);
        accountDTO.setIsHealthcareProfessional(null);
        accountDTO.setAcceptsAspireTermsAndConditions(null);
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(notificationService, times(0)).sendAspireRegistrationNotification(any());
    }

    @Test
    public void newAccount_givenAspireRegistrationIsValid_sendAspireSNSShouldBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setAcceptsAspireEnrollmentConsent(true);
        accountDTO.setIsHealthcareProfessional(false);
        accountDTO.setAcceptsAspireTermsAndConditions(true);
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(notificationService, times(1)).sendAspireRegistrationNotification(any());
    }

    @Test
    public void newAccount_givenAspireEnrollmentIsNotAccepted_sendAspireSNSShouldNotBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setAcceptsAspireEnrollmentConsent(false);
        accountDTO.setIsHealthcareProfessional(false);
        accountDTO.setAcceptsAspireTermsAndConditions(true);
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(notificationService, times(0)).sendAspireRegistrationNotification(any());
    }

    @Test
    public void newAccount_givenUserIsHealthCareProfessional_sendAspireSNSShouldNotBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setAcceptsAspireEnrollmentConsent(true);
        accountDTO.setIsHealthcareProfessional(true);
        accountDTO.setAcceptsAspireTermsAndConditions(true);
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(notificationService, times(0)).sendAspireRegistrationNotification(any());
    }

    @Test
    public void newAccount_givenUserDoesNotAcceptAspireTermsAndConditions_sendAspireSNSShouldNotBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setAcceptsAspireEnrollmentConsent(true);
        accountDTO.setIsHealthcareProfessional(false);
        accountDTO.setAcceptsAspireTermsAndConditions(false);
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(notificationService, times(0)).sendAspireRegistrationNotification(any());
    }

    @Test
    public void newAccount_givenAValidAccount_And_RegistrationTypeIsBasic_sendConfirmationEmailShouldBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setRegistrationType(RegistrationType.BASIC.getValue());
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(notificationService, times(1)).sendConfirmationEmailNotification(any());
    }

    @Test
    public void newAccount_givenAValidAccount_And_RegistrationTypeIsNotBasic_sendConfirmationEmailShouldNotBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setRegistrationType("dummy");
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountRequestService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        verify(notificationService, times(0)).sendConfirmationEmailNotification(any());
    }

    @Test
    public void sendUsernameRecoveryEmail_shouldSearchForAccountInfoByEmail() throws IOException, CustomGigyaErrorException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        when(cdcResponseHandler.getAccountInfoByEmail(anyString())).thenReturn(accountInfo);

        // when
        accountsController.sendRecoverUsernameEmail(usernameRecoveryDTO);

        // then
        verify(cdcResponseHandler).getAccountInfoByEmail(anyString());
    }

    @Test
    public void sendUsernameRecoveryEmail_shouldSendUsernameRecoveryEmail() throws IOException, CustomGigyaErrorException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        when(cdcResponseHandler.getAccountInfoByEmail(anyString())).thenReturn(accountInfo);

        // when
        accountsController.sendRecoverUsernameEmail(usernameRecoveryDTO);

        // then
        verify(notificationService, times(1)).sendRecoveryUsernameEmailNotification(any(), any());
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
    public void newAccount_givenAnAccountWithLongFirstName_returnBadRequest() throws IOException {
        // given
        final String LONG_FIRST_NAME = RandomStringUtils.random(31);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setFirstName(LONG_FIRST_NAME);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongLastName_returnBadRequest() throws IOException {
        // given
        final String LONG_LAST_NAME = RandomStringUtils.random(31);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setLastName(LONG_LAST_NAME);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongEmail_returnBadRequest() throws IOException {
        // given
        final String LONG_EMAIL = RandomStringUtils.random(51);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setEmailAddress(LONG_EMAIL);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    public void newAccount_givenAnAccountWithShortPassword_returnBadRequest() throws IOException {
        // given
        final String SHORT_PASSWORD = RandomStringUtils.random(7);;
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setPassword(SHORT_PASSWORD);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongPassword_returnBadRequest() throws IOException {
        // given
        final String LONG_PASSWORD = RandomStringUtils.random(21);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setPassword(LONG_PASSWORD);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongCompany_returnBadRequest() throws IOException {
        // given
        final String LONG_COMPANY = RandomStringUtils.random(51);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setCompany(LONG_COMPANY);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongCity_returnBadRequest() throws IOException {
        // given
        final String LONG_CITY = RandomStringUtils.random(31);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setCity(LONG_CITY);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID);

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void isAvailableLoginID_GivenIdIsAvailableInCDC_ItShouldReturnOk() throws Exception {
        // given
        String loginID = "test@mail.com";
        when(cdcResponseHandler.isAvailableLoginId(loginID)).thenReturn(true);

        // when
        ResponseEntity<AccountAvailabilityResponse> response = accountsController.isAvailableLoginID(loginID);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertTrue(response.getBody().getIsCDCAvailable());
    }

    @Test
    public void isAvailableLoginID_GivenIdIsNotAvailableInCDC_ItShouldReturnOk() throws Exception {
        // given
        String loginID = "test@mail.com";
        when(cdcResponseHandler.isAvailableLoginId(loginID)).thenReturn(false);

        // when
        ResponseEntity<AccountAvailabilityResponse> response = accountsController.isAvailableLoginID(loginID);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertFalse(response.getBody().getIsCDCAvailable());
    }

    @Test
    public void isAvailableLoginID_GivenAnExceptionOccursWhenCheckingCDC_ItShouldReturnInternalServerError() throws Exception {
        // given
        String loginID = "test@mail.com";
        when(cdcResponseHandler.isAvailableLoginId(loginID)).thenThrow(CustomGigyaErrorException.class);

        // when
        ResponseEntity<AccountAvailabilityResponse> response = accountsController.isAvailableLoginID(loginID);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void onAccountsMerge_GivenTheMethodIsCalled_WhenJWTIsNotValid_ThenNoAccountLinkingMethodsShouldBeCalled() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 1;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.MERGE, numberOfWebhookEvents);
        when(cdcResponseHandler.getJWTPublicKey()).thenReturn(null);
        doNothing().when(accountRequestService).onAccountMerged(anyString());
        doNothing().when(accountRequestService).onAccountUpdated(anyString());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(false);
            
            // when
            accountsController.onAccountsMerge(jwt, body);

            // then
            verify(accountRequestService, never()).onAccountMerged(anyString());
            verify(accountRequestService, never()).onAccountUpdated(anyString());
        }
    }

    @Test
    public void onAccountsMerge_GivenTheMethodIsCalled_WhenNotificationTypeIsNotMerge_ThenAccountRequestService_OnAccountMergedShouldNotBeCalled() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 1;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.REGISTRATION, numberOfWebhookEvents);
        when(cdcResponseHandler.getJWTPublicKey()).thenReturn(null);
        doNothing().when(accountRequestService).onAccountMerged(anyString());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(false);
            
            // when
            accountsController.onAccountsMerge(jwt, body);

            // then
            verify(accountRequestService, never()).onAccountMerged(anyString());
        }
    }

    @Test
    public void onAccountsMerge_GivenTheMethodIsCalled_ThenAccountsRequestService_OnAccountMergedMethodShouldBeCalledSameTimesAsWebhookEventsAmount() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 2;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.MERGE, numberOfWebhookEvents);
        when(cdcResponseHandler.getJWTPublicKey()).thenReturn(null);
        doNothing().when(accountRequestService).onAccountMerged(anyString());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(true);
            
            // when
            accountsController.onAccountsMerge(jwt, body);

            // then
            verify(accountRequestService, times(numberOfWebhookEvents)).onAccountMerged(anyString());
        }
    }

    @Test
    public void onAccountUpdated_GivenTheMethodIsCalled_WhenNotificationTypeIsNotUpdated_ThenOnAccountUpdatedShouldNotBeCalled() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 1;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.REGISTRATION, numberOfWebhookEvents);
        when(cdcResponseHandler.getJWTPublicKey()).thenReturn(null);
        doNothing().when(accountRequestService).onAccountUpdated(anyString());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(false);
            
            // when
            accountsController.onAccountsMerge(jwt, body);

            // then
            verify(accountRequestService, never()).onAccountUpdated(anyString());
        }
    }

    @Test
    public void onAccountUpdated_GivenTheMethodIsCalled_ThenOnAccountUpdatedMethodShouldBeCalledSameTimesAsWebhookEventsAmount() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 2;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.UPDATE, numberOfWebhookEvents);
        when(cdcResponseHandler.getJWTPublicKey()).thenReturn(null);
        doNothing().when(accountRequestService).onAccountUpdated(anyString());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(true);
            
            // when
            accountsController.onAccountsMerge(jwt, body);

            // then
            verify(accountRequestService, times(numberOfWebhookEvents)).onAccountUpdated(anyString());
        }
    }

    @Test
    public void getProfileUserByUID_GivenAValidUID_ShouldReturnUserProfile() throws IOException {
        // given
        String uid = uids.get(0);
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        ProfileInfoDTO profileInfoDTO = ProfileInfoDTO.build(accountInfo);
        Mockito.when(usersHandler.getUserProfileByUID(uid)).thenReturn(profileInfoDTO);

        // when
        ResponseEntity<ProfileInfoDTO> resp = accountsController.getUserProfileByUID(uid);

        // then
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void getProfileUserByUID_GivenAValidUID_whenProfileDoesntExist_ShouldReturnNotFound() throws IOException {
        // given
        String uid = uids.get(0);
        Mockito.when(usersHandler.getUserProfileByUID(uid)).thenReturn(null);

        // when
        ResponseEntity<ProfileInfoDTO> resp = accountsController.getUserProfileByUID(uid);

        // then
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void getProfileUserByUID_GivenAnIOError_returnInternalServerError() throws IOException {
        // given
        String uid = uids.get(0);
        Mockito.when(usersHandler.getUserProfileByUID(uid)).thenThrow(IOException.class);

        // when
        ResponseEntity<ProfileInfoDTO> resp = accountsController.getUserProfileByUID(uid);

        // then
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void updateUserProfile_GivenNullProfileInfoDTO_WhenRequestUpdate_ThenShouldReturnBadRequest() throws Exception {
        // given
        Mockito.when(updateAccountService.updateProfile(null)).thenReturn(HttpStatus.BAD_REQUEST);

        // when
        ResponseEntity<String> resp = accountsController.updateUserProfile(null);

        // then
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void updateUserProfile_GivenAValidProfileInfoDTO_WhenRequestUpdate_ThenShouldReturnOK() throws Exception {
        // given
        when(updateAccountService.updateProfile(profileInfoDTO)).thenReturn(HttpStatus.OK);
        when(cdcResponseHandler.getAccountInfo(any())).thenReturn(AccountUtils.getSiteAccount());
        doNothing().when(notificationService).sendPublicAccountUpdatedNotification(any());
        doNothing().when(notificationService).sendPrivateAccountUpdatedNotification(any());

        // when
        ResponseEntity<String> resp = accountsController.updateUserProfile(profileInfoDTO);

        // then
        verify(notificationService).sendPublicAccountUpdatedNotification(any());
        verify(notificationService).sendPrivateAccountUpdatedNotification(any());
        assertEquals(resp.getStatusCode(),HttpStatus.OK);
    }

    @Test
    public void changePassword_whenNoExceptionIsThrown_returnOkNoContent() throws CustomGigyaErrorException {
        // given
        String uid = Long.toString(1L);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setNewPassword("Hello");
        changePasswordDTO.setPassword("World");
        doNothing().when(cdcResponseHandler).changePassword(anyString(), anyString(), anyString());

        try (MockedStatic<PasswordUtils> passwordUtilsMock = Mockito.mockStatic(PasswordUtils.class)) {
            passwordUtilsMock.when(() -> PasswordUtils.isPasswordValid(anyString())).thenReturn(true);

            // when
            ResponseEntity<?> response = accountsController.changePassword(uid, changePasswordDTO);

            // then
            Assert.assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
        }
    }

    @Test
    public void changePassword_whenPasswordIsInvalid_returnBadRequest() throws CustomGigyaErrorException {
        // given
        String uid = Long.toString(1L);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setNewPassword("Hello");
        changePasswordDTO.setPassword("World");
        doNothing().when(cdcResponseHandler).changePassword(anyString(), anyString(), anyString());

        // when
        ResponseEntity<?> response = accountsController.changePassword(uid, changePasswordDTO);

        // then
        Assert.assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void changePassword_whenPasswordChangeIsSuccess_ThenPasswordShouldBeHashed() throws CustomGigyaErrorException {
        // given
        String uid = Long.toString(1L);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setNewPassword("P@ssw0rd");
        changePasswordDTO.setPassword("World");
        doNothing().when(cdcResponseHandler).changePassword(anyString(), anyString(), anyString());
        doNothing().when(notificationService).sendPasswordUpdateNotification(any());

        // when

        try (MockedStatic<HashingService> hashing = Mockito.mockStatic(HashingService.class)) {
            hashing.when(() -> HashingService.toMD5(anyString())).thenCallRealMethod();

            // when
            accountsController.changePassword(uid, changePasswordDTO);

            // then
            hashing.verify(() -> HashingService.toMD5(changePasswordDTO.getNewPassword()));
        }
    }

    @Test
    public void changePassword_whenPasswordChangeIsSuccess_ThenPasswordUpdateNotificationShouldBeSent() throws CustomGigyaErrorException {
        // given
        String uid = Long.toString(1L);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setNewPassword("Hello");
        changePasswordDTO.setPassword("World");
        doNothing().when(cdcResponseHandler).changePassword(anyString(), anyString(), anyString());
        doNothing().when(notificationService).sendPasswordUpdateNotification(any());

        // when

        try (MockedStatic<PasswordUtils> passwordUtilsMock = Mockito.mockStatic(PasswordUtils.class)) {
            passwordUtilsMock.when(() -> PasswordUtils.isPasswordValid(anyString())).thenReturn(true);

            // when
            accountsController.changePassword(uid, changePasswordDTO);

            // then
            verify(notificationService).sendPasswordUpdateNotification(any());
        }
    }

    @Test
    public void changePassword_whenCustomGigyaExceptionIsThrown_returnBadRequest() throws CustomGigyaErrorException {
        // given
        String uid = Long.toString(1L);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setNewPassword("Hello");
        changePasswordDTO.setPassword("World");
        doThrow(new CustomGigyaErrorException("")).when(cdcResponseHandler).changePassword(anyString(), anyString(), anyString());

        // when
        ResponseEntity<?> response = accountsController.changePassword(uid, changePasswordDTO);

        // then
        Assert.assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void changePassword_whenIllegalArgumentExceptionIsThrown_returnBadRequest() throws CustomGigyaErrorException {
        // given
        String uid = Long.toString(1L);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setNewPassword("Hello");
        changePasswordDTO.setPassword("World");
        doThrow(new IllegalArgumentException()).when(cdcResponseHandler).changePassword(anyString(), anyString(), anyString());

        // when
        ResponseEntity<?> response = accountsController.changePassword(uid, changePasswordDTO);

        // then
        Assert.assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void changePassword_whenOtherExceptionIsThrown_returnInternalServerError() {
        // given
        String uid = Long.toString(1L);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setNewPassword("Hello");
        changePasswordDTO.setPassword("World");
        
        try (MockedStatic<PasswordUtils> passwordUtilsMock = Mockito.mockStatic(PasswordUtils.class)) {
            passwordUtilsMock.when(() -> PasswordUtils.isPasswordValid(anyString())).thenThrow(new NullPointerException());

            // when
            ResponseEntity<?> response = accountsController.changePassword(uid, changePasswordDTO);

            // then
            Assert.assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
