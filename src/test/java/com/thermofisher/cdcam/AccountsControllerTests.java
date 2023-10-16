package com.thermofisher.cdcam;

import com.gigya.socialize.GSKeyNotFoundException;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.controller.AccountsController;
import com.thermofisher.cdcam.enums.RegistrationType;
import com.thermofisher.cdcam.enums.ResponseCode;
import com.thermofisher.cdcam.enums.cdc.GigyaCodes;
import com.thermofisher.cdcam.enums.cdc.WebhookEvent;
import com.thermofisher.cdcam.model.AccountAvailabilityResponse;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.Ciphertext;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.model.UserTimezone;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.dto.AccountInfoDTO;
import com.thermofisher.cdcam.model.dto.CIPAuthDataDTO;
import com.thermofisher.cdcam.model.dto.ChangePasswordDTO;
import com.thermofisher.cdcam.model.dto.ConsentDTO;
import com.thermofisher.cdcam.model.dto.MarketingConsentDTO;
import com.thermofisher.cdcam.model.dto.ProfileInfoDTO;
import com.thermofisher.cdcam.model.dto.UsernameRecoveryDTO;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaLowScoreException;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaUnsuccessfulResponseException;
import com.thermofisher.cdcam.services.AccountsService;
import com.thermofisher.cdcam.services.CookieService;
import com.thermofisher.cdcam.services.DataProtectionService;
import com.thermofisher.cdcam.services.EmailVerificationService;
import com.thermofisher.cdcam.services.GigyaService;
import com.thermofisher.cdcam.services.InvitationService;
import com.thermofisher.cdcam.services.JWTService;
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
import com.thermofisher.cdcam.utils.cdc.CDCTestsUtils;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
//@ExtendWith(SpringExtension.class)
@SpringBootTest
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

    @Value("${identity.oidc.rp.id}")
    private String defaultClientId;

    @InjectMocks
    AccountsController accountsController;

    @Mock
    AccountsService accountsService;

    @Mock
    CookieService cookieService;

    @Mock
    DataProtectionService dataProtectionService;

    @Mock
    EmailVerificationService emailVerificationService;

    @Mock
    GigyaService gigyaService;

    @Mock
    InvitationService invitationService;

    @Mock
    JWTService jwtService;

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
        cdcResponseData.setErrorCode(0);
        cdcResponseData.setStatusReason("");
        return cdcResponseData;
    }

    private CDCResponseData getEmailVerificationCDCResponse(String uid) {
        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID(uid);
        cdcResponseData.setStatusCode(206);
        cdcResponseData.setErrorCode(GigyaCodes.ACCOUNT_PENDING_REGISTRATION.getValue());
        cdcResponseData.setErrorDetails("Missing required fields for registration: data.verifiedEmailDate");
        return cdcResponseData;
    }

    JSONObject reCaptchaResponse;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        reCaptchaResponse = new JSONObject();
        if(uids.size()==0 ) {
            uids.add("001");
            uids.add("002");
            uids.add("003");
        }
    //  when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());
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
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getUsers_GivenAnIOError_returnInternalServerError() throws IOException {
        // given
        Mockito.when(usersHandler.getUsers(uids)).thenThrow(IOException.class);

        // when
        ResponseEntity<List<UserDetails>> resp = accountsController.getUsers(uids);

        // then
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void handleHttpMessageNotReadableExceptions_givenHttpMessageNotReadableException_ReturnErrorMessage() {
        // given
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("");

        // when
        String resp = accountsController.handleHttpMessageNotReadableExceptions(ex);

        // then
        Assertions.assertThat(resp).isEqualTo("Invalid input format. Message not readable.");
    }

    /*@Test
    public void handleHttpMessageNotReadableExceptions_givenParseException_ReturnErrorMessage() {
        // given
        ParseException ex = new ParseException(1);

        // when
        String resp = accountsController.handleHttpMessageNotReadableExceptions(ex);

        // then
        Assertions.assertThat(resp, "Invalid input format. Message not readable.");
    }*/

    @Test
    public void handleNullPointerException_givenNullPointerException_ReturnErrorMessage() {
        // given
        NullPointerException ex = new NullPointerException();

        // when
        String resp = accountsController.handleNullPointerException(ex);

        // then
        Assertions.assertThat(resp).isEqualTo("Invalid input. Null body present.");
    }

    @Test
    public void onAccountRegistered_givenMethodCalled_WhenJWTIsValid_returnOk() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 1;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.REGISTRATION, numberOfWebhookEvents);
        when(gigyaService.getJWTPublicKey()).thenReturn(null);
        doNothing().when(accountsService).onAccountRegistered(any());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(true);

            // when
            ResponseEntity<String> response = accountsController.onAccountRegistered(jwt, body);

            // then
            Assertions.assertThat(HttpStatus.OK).isEqualTo(response.getStatusCode());
        }
    }

    @Test
    public void onAccountRegistered_givenMethodCalled_WhenJWTIsNotValid_ThenOnAccountRegisteredShouldNotBeCalled() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 1;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.REGISTRATION, numberOfWebhookEvents);
        when(gigyaService.getJWTPublicKey()).thenReturn(null);
//        doNothing().when(accountsService).onAccountRegistered(any());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(false);

            // when
            accountsController.onAccountRegistered(jwt, body);

            // then
            verify(accountsService, times(0)).onAccountRegistered(body);
        }
    }

    @Test
    public void onAccountRegistered_GivenTheMethodIsCalled_ThenAccountsRequestService_onAccountRegisteredMethodShouldBeCalledSameTimesAsWebhookEventsAmount() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 2;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.REGISTRATION, numberOfWebhookEvents);
        when(gigyaService.getJWTPublicKey()).thenReturn(null);
        doNothing().when(accountsService).onAccountRegistered(anyString());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(true);
            
            // when
            accountsController.onAccountRegistered(jwt, body);

            // then
            verify(accountsService, times(numberOfWebhookEvents)).onAccountRegistered(anyString());
        }
    }

    @Test
    public void onAccountRegistered_shouldNotCallOnAccountRegistered_whenPublicKeyCantBeRetrieved() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 1;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.REGISTRATION, numberOfWebhookEvents);
        when(gigyaService.getJWTPublicKey()).thenThrow(new CustomGigyaErrorException(""));
//        doNothing().when(accountsService).onAccountRegistered(any());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(false);

            // when
            accountsController.onAccountRegistered(jwt, body);

            // then
            verify(accountsService, times(0)).onAccountRegistered(body);
        }
    }

    @Test
    public void newAccount_givenHoneypotFieldsAreFilled_ThenReturnBadRequest()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, IOException {
        // given
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setHemailAddress(RandomStringUtils.random(10));

        // when
        ResponseEntity<?> result = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void newAccount_ShouldVerifyTheReCaptchaToken()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, IOException {
        // given
        String captchaValidationToken = RandomStringUtils.random(10);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(reCaptchaService.verifyToken(any(), eq(captchaValidationToken))).thenReturn(reCaptchaResponse);
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, captchaValidationToken);

        // then
        verify(reCaptchaService).verifyToken(anyString(), eq(captchaValidationToken));
    }

    @Test
    public void newAccount_givenReCaptchaVerificationThrowsReCaptchaLowScoreException_ThenResponseEntityShouldBeOfAcceptedType()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, IOException {
        // given
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(reCaptchaService.verifyToken(any(), any())).thenThrow(new ReCaptchaLowScoreException(""));
        when(jwtService.create()).thenReturn(RandomStringUtils.random(10));

        // when
        ResponseEntity<?> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertNotNull(response.getHeaders().get(ReCaptchaService.CAPTCHA_TOKEN_HEADER));
        verify(jwtService).create();
    }

    @Test
    public void newAccount_givenReCaptchaVerificationThrowsReCaptchaUnsuccessfulException_ThenResponseEntityShouldBeOfBadRequestType()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, IOException {
        // given
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(reCaptchaService.verifyToken(any(), any())).thenThrow(new ReCaptchaUnsuccessfulResponseException(""));

        // when
        ResponseEntity<?> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void newAccount_givenReCaptchaVerificationThrowsJSONException_ThenResponseEntityShouldBeInternalServerError()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, IOException {
        // given
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(reCaptchaService.verifyToken(any(), any())).thenThrow(new JSONException(""));

        // when
        ResponseEntity<?> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void newAccount_givenReCaptchaIsValid_ThenContinueWithRegistrationProcess() 
            throws IOException, JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        verify(accountsService).createAccount(any());
    }

    @Test
    public void newAccount_givenAnAccountWithValidEncryptedDataIsProvided_ShouldDecryptTheCiphertext() throws JSONException, IOException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setCiphertext(CIPHERTEXT);
        Ciphertext decryptedCiphertext = AccountUtils.getCiphertext();
        when(dataProtectionService.decrypCiphertext(CIPHERTEXT)).thenReturn(decryptedCiphertext);
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        verify(dataProtectionService).decrypCiphertext(CIPHERTEXT);
    }

    @Test
    public void newAccount_givenAnAccountDTOWithValidEncryptedDataIsProvided_ThenAccountInfoShouldBeSetWithDecryptionResponse() throws JSONException, IOException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setCiphertext(CIPHERTEXT);
        AccountInfo accountInfo = AccountBuilder.buildFrom(accountDTO);
        Ciphertext decryptedCiphertext = AccountUtils.getCiphertext();
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(dataProtectionService.decrypCiphertext(CIPHERTEXT)).thenReturn(decryptedCiphertext);
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());
        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then

        Assertions.assertThat(accountInfo.getFirstName()).isEqualTo(accountDTO.getFirstName());
        Assertions.assertThat(accountInfo.getLastName()).isEqualTo(accountDTO.getLastName());
        Assertions.assertThat(accountInfo.getEmailAddress()).isEqualTo(accountDTO.getEmailAddress());
    }

    @Test
    public void newAccount_givenAnAccountWithBlankPassword_returnBadRequest() throws IOException, JSONException {
        // given
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setPassword("");

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(HttpStatus.BAD_REQUEST).isEqualTo(response.getStatusCode());
    }

    @Test
    public void newAccount_GivenCipAuthDataCookiesIsValid_ShouldAssignProviderClientIdToAccount() throws IOException, JSONException, CustomGigyaErrorException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        CIPAuthDataDTO cipAuthDataDTO = cookieService.decodeCIPAuthDataCookie(COOKIE_CIP_AUTHDATA_VALID);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(accountsService.createAccount(any())).thenReturn(new CDCResponseData());

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        verify(accountsService).createAccount(accountInfoCaptor.capture());
        AccountInfo capturedAccountInfo = accountInfoCaptor.getValue();
        Assertions.assertThat(cipAuthDataDTO.getClientId()).isEqualTo(capturedAccountInfo.getOpenIdProviderId());
    }

    @Test
    public void newAccount_GivenCipAuthDataCookiesIsInvalid_ShouldAssignDefaultProviderClientIdToAccount() throws IOException, JSONException, CustomGigyaErrorException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(accountsService.createAccount(any())).thenReturn(new CDCResponseData());
        String COOKIE_CIP_AUTHDATA_INVALID = null;
        ReflectionTestUtils.setField(accountsController, "defaultClientId", defaultClientId);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_INVALID, null);

        // then
        verify(accountsService).createAccount(accountInfoCaptor.capture());
        AccountInfo capturedAccountInfo = accountInfoCaptor.getValue();
        Assertions.assertThat(defaultClientId).isEqualTo(capturedAccountInfo.getOpenIdProviderId());
    }

    @Test
    public void newAccount_givenABackendError_returnInternalServerError()
            throws IOException, JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(accountsService.createAccount(any())).thenThrow(new CustomGigyaErrorException(""));

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(HttpStatus.BAD_GATEWAY).isEqualTo(response.getStatusCode());
    }

    @Test
    public void newAccount_givenAValidAccount_returnCdcResponseWithUID() throws IOException, JSONException, ReCaptchaLowScoreException,
            ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(response.getBody().getUID()).isEqualTo(AccountUtils.uid);
    }

    @Test
    public void newAccount_GivenAnAccountWithInvalidEmail_ThenShouldReturnBadResponseError() throws IOException, JSONException, ReCaptchaLowScoreException,
            ReCaptchaUnsuccessfulResponseException, CustomGigyaErrorException {
//        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        String invalidEmail = "invalid@.com";
        accountDTO.setEmailAddress(invalidEmail);
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
//        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void newAccount_givenAnAccountIsCreated_AndItComesFromInvite_ItShouldBeVerified() throws IOException, JSONException, ReCaptchaLowScoreException,
            ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setCiphertext(CIPHERTEXT);
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(dataProtectionService.decrypCiphertext(CIPHERTEXT)).thenReturn(AccountUtils.getCiphertext());
        when(accountsService.createAccount(any())).thenReturn(getEmailVerificationCDCResponse(AccountUtils.uid));
        when(accountsService.verify(any(), any())).thenReturn(AccountUtils.getCdcResponse());
        when(invitationService.updateInvitationCountry(any())).thenReturn(200);

        try(MockedStatic<EmailVerificationService> emailVerificationServiceMock = Mockito.mockStatic(EmailVerificationService.class)) {
            emailVerificationServiceMock.when(() -> EmailVerificationService.isVerificationPending(any())).thenReturn(true);

            // when
            ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

            // then
            verify(accountsService).verify(any(), any());
            Assertions.assertThat(response.getBody().getErrorCode()).isEqualTo(0);
        }
    }

    @Test
    public void newAccount_givenAnAccountIsCreated_AndEmailVerificationIsPending_ShouldReturnVerificationPendingCode() throws IOException, JSONException, ReCaptchaLowScoreException,
            ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(accountsService.createAccount(any())).thenReturn(getEmailVerificationCDCResponse(AccountUtils.uid));
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        try(MockedStatic<EmailVerificationService> emailVerificationServiceMock = Mockito.mockStatic(EmailVerificationService.class)) {
            emailVerificationServiceMock.when(() -> EmailVerificationService.isVerificationPending(any())).thenReturn(true);

            // when
            ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

            // then
            Assertions.assertThat(response.getBody().getErrorCode()).isEqualTo(ResponseCode.ACCOUNT_PENDING_VERIFICATION.getValue());
        }
    }

    @Test
    public void newAccount_givenAValidAccount_withCIPAuthDataInvalid_returnCdcResponseWithUID() throws IOException, JSONException, ReCaptchaLowScoreException,
            ReCaptchaUnsuccessfulResponseException, CustomGigyaErrorException {
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, null, null);

        // then
        Assertions.assertThat(response.getBody().getUID()).isEqualTo(AccountUtils.uid);
    }

    @Test
    public void newAccount_givenEmailVerificationIsEnabled_thenShouldReturnAPartialContentErrorCode() throws IOException, JSONException, ReCaptchaLowScoreException,
            ReCaptchaUnsuccessfulResponseException, CustomGigyaErrorException {
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = getEmailVerificationCDCResponse(AccountUtils.uid);
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(cdcResponseData.getErrorCode()).isEqualTo(response.getBody().getErrorCode());
    }

    @Test
    public void newAccount_GivenSuccessfulRegistration_ThenNotifyAccountRegistrationWithHashedPassword() throws CustomGigyaErrorException, IOException, JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException {
        // given
        ReflectionTestUtils.setField(accountsController, "isRegistrationNotificationEnabled", true);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        doNothing().when(notificationService).sendAccountRegisteredNotification(any(), any());

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        verify(notificationService).sendAccountRegisteredNotification(accountInfoCaptor.capture(), any());
        AccountInfo capturedAccount = accountInfoCaptor.getValue();
        String expectedPassword = HashingService.toMD5(accountDTO.getPassword());
        Assertions.assertThat(expectedPassword).isEqualTo(capturedAccount.getPassword());
    }

    @Test
    public void newAccount_givenRegistrationSuccessfulAndEmailVerificationIsEnabled_sendVerificationByLinkEmailShouldBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        verify(emailVerificationService, times(1)).sendVerificationByLinkEmail(any());
    }
    
    @Test
    public void newAccount_givenRegistrationNotSuccessful_sendVerificationByLinkEmailShouldNotBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());
        when(accountsService.createAccount(any())).thenThrow(new CustomGigyaErrorException(""));
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        verify(emailVerificationService, times(0)).sendVerificationByLinkEmail(any());
    }

    @Test
    public void newAccount_givenRegistrationNotSuccessfulAndEmailVerificationIsDisabled_sendVerificationByLinkEmailShouldNotBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setStatusCode(400);
        cdcResponseData.setErrorCode(400004);
        cdcResponseData.setStatusReason("");
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        verify(emailVerificationService, times(0)).sendVerificationByLinkEmail(any());
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
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        verify(notificationService, times(0)).sendAspireRegistrationNotification(any());
    }

    @Test
    public void newAccount_givenAspireRegistrationIsValid_sendAspireSNSShouldBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setAcceptsAspireEnrollmentConsent(true);
        accountDTO.setIsHealthcareProfessional(false);
        accountDTO.setAcceptsAspireTermsAndConditions(true);
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        verify(notificationService, times(1)).sendAspireRegistrationNotification(any());
    }

    @Test
    public void newAccount_givenRegistrationIsValid_ThenSendNotifyAccountInfoNotification() throws IOException, CustomGigyaErrorException, JSONException, ReCaptchaUnsuccessfulResponseException, ReCaptchaLowScoreException {
        // given
        ReflectionTestUtils.setField(accountsController, "cipdc", "us");
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        verify(notificationService, times(1)).sendNotifyAccountInfoNotification(any(), anyString());
    }

    @Test
    public void newAccount_GivenRegistrationNotSuccessful_ThenSendNotifyAccountInfoNotificationShouldNotBeSent() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        when(accountsService.createAccount(any())).thenThrow(new CustomGigyaErrorException(""));
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        verify(notificationService, times(0)).sendNotifyAccountInfoNotification(any(), anyString());
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
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        verify(notificationService, times(0)).sendAspireRegistrationNotification(any());
    }

    @Test
    public void newAccount_givenUserIsHealthCareProfessional_sendAspireSNSShouldNotBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setAcceptsAspireEnrollmentConsent(true);
        accountDTO.setIsHealthcareProfessional(true);
        accountDTO.setAcceptsAspireTermsAndConditions(true);
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        verify(notificationService, times(0)).sendAspireRegistrationNotification(any());
    }

    @Test
    public void newAccount_givenUserDoesNotAcceptAspireTermsAndConditions_sendAspireSNSShouldNotBeCalled() throws IOException,
            JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, NoSuchAlgorithmException, CustomGigyaErrorException {
        // given
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setAcceptsAspireEnrollmentConsent(true);
        accountDTO.setIsHealthcareProfessional(false);
        accountDTO.setAcceptsAspireTermsAndConditions(false);
        CDCResponseData cdcResponseData = getValidCDCResponse(AccountUtils.uid);
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

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
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

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
        when(accountsService.createAccount(any())).thenReturn(cdcResponseData);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(cookieService.decodeCIPAuthDataCookie(anyString())).thenReturn(IdentityProviderUtils.buildCIPAuthDataDTO());

        // when
        accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        verify(notificationService, times(0)).sendConfirmationEmailNotification(any());
    }

    @Test
    public void sendUsernameRecoveryEmail_shouldSearchForAccountInfoByEmail() throws IOException, CustomGigyaErrorException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        when(gigyaService.getAccountInfoByEmail(anyString())).thenReturn(accountInfo);

        // when
        accountsController.sendRecoverUsernameEmail(usernameRecoveryDTO);

        // then
        verify(gigyaService).getAccountInfoByEmail(anyString());
    }

    @Test
    public void sendUsernameRecoveryEmail_shouldSendUsernameRecoveryEmail() throws IOException, CustomGigyaErrorException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        when(gigyaService.getAccountInfoByEmail(anyString())).thenReturn(accountInfo);

        // when
        accountsController.sendRecoverUsernameEmail(usernameRecoveryDTO);

        // then
        verify(notificationService, times(1)).sendRecoveryUsernameEmailNotification(any(), any());
    }

    @Test
    public void sendUsernameRecoveryEmail_shouldReturnBadRequest_whenAccountIsNull() throws IOException, CustomGigyaErrorException {
        // given
        when(gigyaService.getAccountInfoByEmail(anyString())).thenReturn(null);

        // when
        ResponseEntity<String> resp = accountsController.sendRecoverUsernameEmail(usernameRecoveryDTO);

        // then
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void sendUsernameRecoveryEmail_shouldReturnInternalServerError_whenAnExceptionIsThrown() throws IOException, CustomGigyaErrorException {
        // given
        when(gigyaService.getAccountInfoByEmail(anyString())).thenThrow(new CustomGigyaErrorException(""));

        // when
        ResponseEntity<String> resp = accountsController.sendRecoverUsernameEmail(usernameRecoveryDTO);

        // then
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void updateTimezone_GivenEmptyUserUIDOrTimezoneShouldReturnBadRequest() throws Exception {
        // given
        Mockito.when(updateAccountService.updateTimezoneInCDC(emptyUserTimezone.getUid(), emptyUserTimezone.getTimezone())).thenReturn(HttpStatus.BAD_REQUEST);

        // when
        ResponseEntity<String> resp = accountsController.setTimezone(emptyUserTimezone);

        // then
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void updateTimezone_GivenAValidUserUIDAndTimezoneShouldReturnOK() throws Exception {
        // given
        Mockito.when(updateAccountService.updateTimezoneInCDC(any(String.class), any(String.class))).thenReturn(HttpStatus.OK);

        // when
        ResponseEntity<String> resp = accountsController.setTimezone(validUserTimezone);

        // then
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void updateTimezone_MissingRequestBodyParamShouldReturnBadRequest() throws Exception {
        // given
        Mockito.when(updateAccountService.updateTimezoneInCDC(invalidUserTimezone.getUid(), null)).thenReturn(HttpStatus.BAD_REQUEST);

        // when
        ResponseEntity<String> resp = accountsController.setTimezone(invalidUserTimezone);

        // then
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    
    @Test
    public void sendVerificationByLinkEmail_WhenResponseReceived_ReturnSameStatus() {
        // given
        HttpStatus mockStatus = HttpStatus.OK;
        CDCResponseData mockResponse = Mockito.mock(CDCResponseData.class);
        when(mockResponse.getStatusCode()).thenReturn(mockStatus.value());
        when(emailVerificationService.sendVerificationByLinkEmailSync(any())).thenReturn(mockResponse);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.sendVerificationEmail("test");

        // then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(mockStatus);
    }

    @Test
    public void newAccount_givenAnAccountWithLongFirstName_returnBadRequest() throws IOException, JSONException {
        // given
        final String LONG_FIRST_NAME = RandomStringUtils.random(31);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setFirstName(LONG_FIRST_NAME);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(HttpStatus.BAD_REQUEST).isEqualTo(response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongLastName_returnBadRequest() throws IOException, JSONException {
        // given
        final String LONG_LAST_NAME = RandomStringUtils.random(31);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setLastName(LONG_LAST_NAME);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(HttpStatus.BAD_REQUEST).isEqualTo(response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongEmail_returnBadRequest() throws IOException, JSONException {
        // given
        final String LONG_EMAIL = RandomStringUtils.random(51);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setEmailAddress(LONG_EMAIL);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(HttpStatus.BAD_REQUEST).isEqualTo(response.getStatusCode());
    }
    
    @Test
    public void newAccount_givenAnAccountWithShortPassword_returnBadRequest() throws IOException, JSONException {
        // given
        final String SHORT_PASSWORD = RandomStringUtils.random(7);;
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setPassword(SHORT_PASSWORD);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(HttpStatus.BAD_REQUEST).isEqualTo(response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongPassword_returnBadRequest() throws IOException, JSONException {
        // given
        final String LONG_PASSWORD = RandomStringUtils.random(21);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setPassword(LONG_PASSWORD);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(HttpStatus.BAD_REQUEST).isEqualTo(response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongCompany_returnBadRequest() throws IOException, JSONException {
        // given
        final String LONG_COMPANY = RandomStringUtils.random(51);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setCompany(LONG_COMPANY);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(HttpStatus.BAD_REQUEST).isEqualTo(response.getStatusCode());
    }

    @Test
    public void newAccount_givenAnAccountWithLongCity_returnBadRequest() throws IOException, JSONException {
        // given
        final String LONG_CITY = RandomStringUtils.random(31);
        AccountInfoDTO accountDTO = AccountUtils.getAccountInfoDTO();
        accountDTO.setCity(LONG_CITY);

        // when
        ResponseEntity<CDCResponseData> response = accountsController.newAccount(accountDTO, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        Assertions.assertThat(HttpStatus.BAD_REQUEST).isEqualTo(response.getStatusCode());
    }

    @Test
    public void isAvailableLoginID_GivenIdIsAvailableInCDC_ItShouldReturnOk() throws Exception {
        // given
        String loginID = "test@mail.com";
        when(gigyaService.isAvailableLoginId(loginID)).thenReturn(true);

        // when
        ResponseEntity<AccountAvailabilityResponse> response = accountsController.isAvailableLoginID(loginID);

        // then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertTrue(response.getBody().getIsCDCAvailable());
    }

    @Test
    public void isAvailableLoginID_GivenIdIsNotAvailableInCDC_ItShouldReturnOk() throws Exception {
        // given
        String loginID = "test@mail.com";
        when(gigyaService.isAvailableLoginId(loginID)).thenReturn(false);

        // when
        ResponseEntity<AccountAvailabilityResponse> response = accountsController.isAvailableLoginID(loginID);

        // then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertFalse(response.getBody().getIsCDCAvailable());
    }

    @Test
    public void isAvailableLoginID_GivenAnExceptionOccursWhenCheckingCDC_ItShouldReturnInternalServerError() throws Exception {
        // given
        String loginID = "test@mail.com";
        when(gigyaService.isAvailableLoginId(loginID)).thenThrow(CustomGigyaErrorException.class);

        // when
        ResponseEntity<AccountAvailabilityResponse> response = accountsController.isAvailableLoginID(loginID);

        // then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void onAccountsMerge_GivenTheMethodIsCalled_WhenJWTIsNotValid_ThenNoAccountLinkingMethodsShouldBeCalled() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 1;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.MERGE, numberOfWebhookEvents);
        when(gigyaService.getJWTPublicKey()).thenReturn(null);
       // doNothing().when(accountsService).onAccountMerged(anyString());
       // doNothing().when(accountsService).onAccountUpdated(anyString());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(false);
            
            // when
            accountsController.onAccountsMerge(jwt, body);

            // then
            verify(accountsService, never()).onAccountMerged(anyString());
            verify(accountsService, never()).onAccountUpdated(anyString());
        }
    }

    @Test
    public void onAccountsMerge_GivenTheMethodIsCalled_WhenNotificationTypeIsNotMerge_ThenaccountsService_OnAccountMergedShouldNotBeCalled() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 1;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.REGISTRATION, numberOfWebhookEvents);
        when(gigyaService.getJWTPublicKey()).thenReturn(null);
//        doNothing().when(accountsService).onAccountMerged(anyString());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(false);
            
            // when
            accountsController.onAccountsMerge(jwt, body);

            // then
            verify(accountsService, never()).onAccountMerged(anyString());
        }
    }

    @Test
    public void onAccountsMerge_GivenTheMethodIsCalled_ThenAccountsRequestService_OnAccountMergedMethodShouldBeCalledSameTimesAsWebhookEventsAmount() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 2;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.MERGE, numberOfWebhookEvents);
        when(gigyaService.getJWTPublicKey()).thenReturn(null);
        doNothing().when(accountsService).onAccountMerged(anyString());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(true);
            
            // when
            accountsController.onAccountsMerge(jwt, body);

            // then
            verify(accountsService, times(numberOfWebhookEvents)).onAccountMerged(anyString());
        }
    }

    @Test
    public void onAccountsMerge_shouldNotCallOnAccountMerged_whenPublicKeyCantBeRetrieved() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 2;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.MERGE, numberOfWebhookEvents);
        when(gigyaService.getJWTPublicKey()).thenThrow(new CustomGigyaErrorException(""));
//        doNothing().when(accountsService).onAccountMerged(anyString());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(true);

            // when
            accountsController.onAccountsMerge(jwt, body);

            // then
            verify(accountsService, never()).onAccountMerged(anyString());
        }
    }

    @Test
    public void onAccountUpdated_GivenTheMethodIsCalled_WhenNotificationTypeIsNotUpdated_ThenOnAccountUpdatedShouldNotBeCalled() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 1;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.REGISTRATION, numberOfWebhookEvents);
        when(gigyaService.getJWTPublicKey()).thenReturn(null);
//        doNothing().when(accountsService).onAccountUpdated(anyString());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(false);
            
            // when
            accountsController.onAccountsMerge(jwt, body);

            // then
            verify(accountsService, never()).onAccountUpdated(anyString());
        }
    }

    @Test
    public void onAccountUpdated_GivenTheMethodIsCalled_ThenOnAccountUpdatedMethodShouldBeCalledSameTimesAsWebhookEventsAmount() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        int numberOfWebhookEvents = 2;
        String jwt = Utils.getAlphaNumericString(20);
        String body = CDCTestsUtils.getWebhookEventBody(WebhookEvent.UPDATE, numberOfWebhookEvents);
        when(gigyaService.getJWTPublicKey()).thenReturn(null);
        doNothing().when(accountsService).onAccountUpdated(anyString());

        try (MockedStatic<JWTValidator> jwtValidatorMock = Mockito.mockStatic(JWTValidator.class)) {
            jwtValidatorMock.when(() -> JWTValidator.isValidSignature(anyString(), any())).thenReturn(true);
            
            // when
            accountsController.onAccountsMerge(jwt, body);

            // then
            verify(accountsService, times(numberOfWebhookEvents)).onAccountUpdated(anyString());
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
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getProfileUserByUID_GivenAValidUID_whenProfileDoesntExist_ShouldReturnNotFound() throws IOException {
        // given
        String uid = uids.get(0);
        Mockito.when(usersHandler.getUserProfileByUID(uid)).thenReturn(null);

        // when
        ResponseEntity<ProfileInfoDTO> resp = accountsController.getUserProfileByUID(uid);

        // then
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getProfileUserByUID_GivenAnIOError_returnInternalServerError() throws IOException {
        // given
        String uid = uids.get(0);
        Mockito.when(usersHandler.getUserProfileByUID(uid)).thenThrow(IOException.class);

        // when
        ResponseEntity<ProfileInfoDTO> resp = accountsController.getUserProfileByUID(uid);

        // then
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void updateUserProfile_GivenNullProfileInfoDTO_WhenRequestUpdate_ThenShouldReturnBadRequest() throws Exception {
        // given
//        Mockito.when(updateAccountService.updateProfile(null)).thenReturn(HttpStatus.BAD_REQUEST);

        // when
        ResponseEntity<String> resp = accountsController.updateUserProfile(null);

        // then
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void updateUserProfile_GivenAValidProfileInfoDTO_WhenRequestUpdate_ThenShouldReturnOK() throws Exception {
        // given
        when(updateAccountService.updateProfile(profileInfoDTO)).thenReturn(HttpStatus.OK);
        when(gigyaService.getAccountInfo(any())).thenReturn(AccountUtils.getSiteAccount());
        doNothing().when(notificationService).sendPublicAccountUpdatedNotification(any());
        doNothing().when(notificationService).sendPrivateAccountUpdatedNotification(any());

        // when
        ResponseEntity<String> resp = accountsController.updateUserProfile(profileInfoDTO);

        // then
        verify(notificationService).sendPublicAccountUpdatedNotification(any());
        verify(notificationService).sendPrivateAccountUpdatedNotification(any());
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void updateUserProfile_GivenAnInvalidProfileInfoDTO_WhenRequestUpdate_ThenShouldReturnBadRequest() throws Exception {
        // given
        when(updateAccountService.updateProfile(profileInfoDTO)).thenReturn(HttpStatus.BAD_REQUEST);
        when(gigyaService.getAccountInfo(any())).thenReturn(AccountUtils.getSiteAccount());

        // when
        ResponseEntity<String> resp = accountsController.updateUserProfile(profileInfoDTO);

        // then
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void changePassword_whenNoExceptionIsThrown_returnOkNoContent() throws CustomGigyaErrorException {
        // given
        String uid = Long.toString(1L);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setNewPassword("Hello");
        changePasswordDTO.setPassword("World");
        doNothing().when(gigyaService).changePassword(anyString(), anyString(), anyString());

        try (MockedStatic<PasswordUtils> passwordUtilsMock = Mockito.mockStatic(PasswordUtils.class)) {
            passwordUtilsMock.when(() -> PasswordUtils.isPasswordValid(anyString())).thenReturn(true);

            // when
            ResponseEntity<?> response = accountsController.changePassword(uid, changePasswordDTO);

            // then
            Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }

    @Test
    public void changePassword_whenPasswordIsInvalid_returnBadRequest() throws CustomGigyaErrorException {
        // given
        String uid = Long.toString(1L);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setNewPassword("Hello");
        changePasswordDTO.setPassword("World");
//        doNothing().when(gigyaService).changePassword(anyString(), anyString(), anyString());

        // when
        ResponseEntity<?> response = accountsController.changePassword(uid, changePasswordDTO);

        // then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void changePassword_whenPasswordChangeIsSuccess_ThenPasswordShouldBeHashed() throws CustomGigyaErrorException {
        // given
        String uid = Long.toString(1L);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setNewPassword("P@ssw0rd");
        changePasswordDTO.setPassword("World");
        doNothing().when(gigyaService).changePassword(anyString(), anyString(), anyString());
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
        doNothing().when(gigyaService).changePassword(anyString(), anyString(), anyString());
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
//        doThrow(new CustomGigyaErrorException("")).when(gigyaService).changePassword(anyString(), anyString(), anyString());

        // when
        ResponseEntity<?> response = accountsController.changePassword(uid, changePasswordDTO);

        // then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void changePassword_whenIllegalArgumentExceptionIsThrown_returnBadRequest() throws CustomGigyaErrorException {
        // given
        String uid = Long.toString(1L);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setNewPassword("Hello");
        changePasswordDTO.setPassword("World");
//        doThrow(new IllegalArgumentException()).when(gigyaService).changePassword(anyString(), anyString(), anyString());

        // when
        ResponseEntity<?> response = accountsController.changePassword(uid, changePasswordDTO);

        // then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
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
            Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void updateConsent_shouldTriggerConsentUpdateAndNotification_andReturn200() throws CustomGigyaErrorException, JSONException {
        // Given.
        ConsentDTO mockRequest = ConsentDTO.builder()
                .uid("abc123")
                .build();

        doNothing().when(accountsService).updateConsent(mockRequest);
        doNothing().when(accountsService).notifyUpdatedConsent(mockRequest.getUid());

        // When.
        ResponseEntity<?> response = accountsController.updateConsent(mockRequest);

        // Then.
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(accountsService).updateConsent(mockRequest);
        verify(accountsService).notifyUpdatedConsent(mockRequest.getUid());
    }

    @Test
    public void updateConsent_givenException_shouldReturn500() throws CustomGigyaErrorException, JSONException {
        // Given.
        ConsentDTO mockRequest = ConsentDTO.builder()
                .uid("abc123")
                .build();

        doThrow(NullPointerException.class).when(accountsService).updateConsent(mockRequest);

        // When.
        ResponseEntity<?> response = accountsController.updateConsent(mockRequest);

        // Then.
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void updateConsent_givenCustomGigyaErrorException_shouldReturn424() throws CustomGigyaErrorException, JSONException {
        // Given.
        ConsentDTO mockRequest = ConsentDTO.builder()
                .uid("abc123")
                .build();

        doThrow(CustomGigyaErrorException.class).when(accountsService).updateConsent(mockRequest);

        // When.
        ResponseEntity<?> response = accountsController.updateConsent(mockRequest);

        // Then.
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FAILED_DEPENDENCY);
    }
}
