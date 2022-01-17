package com.thermofisher.cdcam.handlers;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSKeyNotFoundException;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.google.gson.JsonParseException;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.builders.IdentityProviderBuilder;
import com.thermofisher.cdcam.enums.cdc.AccountType;
import com.thermofisher.cdcam.enums.cdc.GigyaCodes;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.ResetPasswordResponse;
import com.thermofisher.cdcam.model.ResetPasswordSubmit;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CDCSearchResponse;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.JWTPublicKey;
import com.thermofisher.cdcam.model.cdc.LoginIdDoesNotExistException;
import com.thermofisher.cdcam.model.cdc.OpenIdRelyingParty;
import com.thermofisher.cdcam.model.identityProvider.IdentityProviderResponse;
import com.thermofisher.cdcam.services.CDCAccountsService;
import com.thermofisher.cdcam.services.CDCIdentityProviderService;
import com.thermofisher.cdcam.utils.IdentityProviderUtils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
import com.thermofisher.cdcam.utils.cdc.CDCUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;


/**
 * CDCAccountsServiceTests
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class CDCResponseHandlerTests {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String uid = "c1c691f4-556b-4ad1-ab75-841fc4e94dcd";
    private final String emailAddress = "federatedUser@OIDC.com";
    private final String firstName = "first";
    private final String lastName = "first";
    private final String country = "United States";
    private final String city = "testCity";
    private final String company = "company";
    private String obj = "{\"socialProviders\":\"site,oidc-fedspikegidp\",\"lastLogin\":\"2019-08-21T23:13:38.284Z\",\"userInfo\":{\"country\":\"United States\",\"isTempUser\":false,\"oldestDataAge\":-2147483648,\"capabilities\":\"None\",\"isSiteUID\":true,\"loginProviderUID\":\"ef632aa3f52140aa836673469378d0ac\",\"city\":\"" + city + "\",\"isConnected\":true,\"errorCode\":0,\"isSiteUser\":true,\"loginProvider\":\"oidc-fedspikegidp\",\"oldestDataUpdatedTimestamp\":0,\"UID\":\"ffb10070d8174a518f2e8b403c1efe5d\",\"identities\":[{\"country\":\"United States\",\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"lastUpdatedTimestamp\":1566429217356,\"isExpiredSession\":false,\"allowsLogin\":false,\"city\":\"" + city + "\",\"provider\":\"site\",\"isLoginIdentity\":false,\"oldestDataUpdated\":\"0001-01-01T00:00:00Z\",\"oldestDataUpdatedTimestamp\":0,\"providerUID\":\"ffb10070d8174a518f2e8b403c1efe5d\"},{\"lastUpdated\":\"2019-08-21T23:13:38.284Z\",\"lastUpdatedTimestamp\":1566429218284,\"isExpiredSession\":false,\"allowsLogin\":true,\"provider\":\"oidc-fedspikegidp\",\"isLoginIdentity\":true,\"nickname\":\"federatedUser\",\"oldestDataUpdated\":\"2019-08-21T23:01:23.988Z\",\"oidcData\":{},\"oldestDataUpdatedTimestamp\":1566428483988,\"email\":\"test@gmail.com\",\"providerUID\":\"ef632aa3f52140aa836673469378d0ac\"}],\"statusReason\":\"OK\",\"nickname\":\"federatedUser\",\"isLoggedIn\":true,\"time\":\"2019-08-23T23:50:35.918Z\",\"email\":\"test@gmail.com\",\"providers\":\"site,oidc-fedspikegidp\",\"statusCode\":200},\"data\":{\"terms\":true},\"isVerified\":true,\"errorCode\":0,\"registered\":\"2019-08-19T21:11:52.372Z\",\"isActive\":true,\"oldestDataUpdatedTimestamp\":1566248846440,\"emails\":{\"verified\":[\"test@gmail.com\"],\"unverified\":[]},\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"apiVersion\":2,\"statusReason\":\"OK\",\"verifiedTimestamp\":1566248848104,\"oldestDataUpdated\":\"2019-08-19T21:07:26.440Z\",\"callId\":\"52317e98c0a849438f432669c5d198f0\",\"lastUpdatedTimestamp\":1566429217356,\"created\":\"2019-08-19T21:07:26.440Z\",\"createdTimestamp\":1566248846000,\"profile\":{\"firstName\":\"" + firstName + "\",\"lastName\":\"" + lastName + "\",\"work\":{\"company\":\"" + company + "\"},\"country\":\"" + country + "\",\"city\":\"" + city + "\",\"nickname\":\"federatedUser\",\"email\":\"" + emailAddress + "\"},\"regSource\":\"http://dev2.apps.thermofisher.com/apps/fedspike/enterpriselogin\",\"verified\":\"2019-08-19T21:07:28.104Z\",\"registeredTimestamp\":1566249112000,\"loginProvider\":\"oidc-fedspikegidp\",\"lastLoginTimestamp\":1566429218000,\"UID\":\"" + uid + "\",\"isRegistered\":true,\"time\":\"2019-08-23T23:50:35.919Z\",\"statusCode\":200}";

    @Value("${cdc.main.datacenter}")
    private String mainApiDomain;

    @InjectMocks
    CDCResponseHandler cdcResponseHandler;

    @Mock
    CDCAccountsService cdcAccountsService;

    @Mock
    AccountBuilder accountBuilder;

    @Mock
    CDCIdentityProviderService cdcIdentityProviderService;

    @Mock
    IdentityProviderBuilder identityProviderBuilder;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(accountBuilder, "logger", LogManager.getLogger(AccountBuilder.class));
    }

    @Test
    public void getAccount_WhenAGetAccountRequestInfoIsMade_ShouldReturnAnAccountInfoObjectWithResponseData() throws Exception {
        // given
        GSResponse gsResponse = Mockito.mock(GSResponse.class);
        GSObject mockedGSObject = new GSObject(obj);
        when(cdcAccountsService.getAccount(anyString())).thenReturn(gsResponse);
        when(gsResponse.getData()).thenReturn(mockedGSObject);
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        AccountInfo accountInfo = accountBuilder.getAccountInfo(new GSObject(obj));

        // when
        AccountInfo account = cdcResponseHandler.getAccountInfo(uid);

        // then
        String _accountInfo = mapper.writeValueAsString(accountInfo);
        String _account = mapper.writeValueAsString(account);
        assertTrue(_accountInfo.equals(_account));
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void getAccountInfo_WhenAGetAccountRequestInfoRequestIsResolvedWithError_ShouldThrowCustomGigyaErrorException() throws Exception {
        // given
        final int ERROR_CODE = new Random().nextInt(10) + 1;
        GSResponse gsResponse = Mockito.mock(GSResponse.class);
        when(cdcAccountsService.getAccount(anyString())).thenReturn(gsResponse);
        when(gsResponse.getErrorCode()).thenReturn(ERROR_CODE);

        // when
        cdcResponseHandler.getAccountInfo(uid);
    }

    @Test
    public void update_WhenGSResponseCodeIsZero_AnObjectNodeWith200ErrorCodeShouldBeReturned() throws JSONException {
        // given
        String message = "Success";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        Mockito.when(cdcAccountsService.setUserInfo(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(0);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        JSONObject user = new JSONObject("{\"data\":{\"regStatus\":true},\"profile\":{\"username\":\"test@test.com\"}}");

        // when
        ObjectNode updateResponse = cdcResponseHandler.update(user);

        // then
        Assert.assertEquals(HttpStatus.OK.value(), updateResponse.get("code").asInt());
        Assert.assertEquals(message, updateResponse.get("error").asText());
    }

    @Test
    public void update_WhenGSResponseCodeIsZero_AnObjectNodeWith599999ErrorCodeShouldBeReturned()
            throws JSONException {
        // given
        String message = "Something went bad.";
        int errorCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        Mockito.when(cdcAccountsService.setUserInfo(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(errorCode);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        JSONObject user = new JSONObject("{\"data\":{\"regStatus\":true},\"profile\":{\"username\":\"test@test.com\"}}");

        // when
        ObjectNode updateResponse = cdcResponseHandler.update(user);

        // then
        Assert.assertEquals(errorCode, updateResponse.get("code").asInt());
        Assert.assertEquals(message, updateResponse.get("error").asText());
    }

    @Test
    public void disableAccounts_WhenAValidUidIsReceived_TheAccountStatusIsChanged() {
        // given
        String uid = "0001";
        int successCode = 0;
        String message = "Account status successfully changed.";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        GSResponse mockChangeStatusResponse = Mockito.mock(GSResponse.class);

        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(successCode);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        Mockito.when(cdcAccountsService.changeAccountStatus(anyString(),anyBoolean())).thenReturn(mockChangeStatusResponse);
        Mockito.when(mockChangeStatusResponse.getErrorCode()).thenReturn(successCode);

        // when
        boolean updateResponse = cdcResponseHandler.disableAccount(uid);

        // then
        Assert.assertTrue(updateResponse);
    }

    @Test
    public void disableAccounts_WhenGSResponseErrorCodeIsNotZero_ThenReturnUnsuccessfulUpdate() {
        // given
        String uid = "0001";
        int errorCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String message = "An error occurred while updating an account status.";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        GSResponse mockChangeStatusResponse = Mockito.mock(GSResponse.class);

        Mockito.when(cdcAccountsService.search(anyString(), any(), any())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(errorCode);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        Mockito.when(cdcAccountsService.changeAccountStatus(anyString(), anyBoolean())).thenReturn(mockChangeStatusResponse);
        Mockito.when(mockChangeStatusResponse.getErrorCode()).thenReturn(errorCode);

        // when
        boolean updateResponse = cdcResponseHandler.disableAccount(uid);

        // then
        Assert.assertFalse(updateResponse);
    }

    @Test
    public void sendVerificationEmail_whenGSResponseIsNotNull_returnResponseAsItIs() throws IOException {
        // given
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        String mockResponseText = "{\"statusCode\": 200,\"statusReason\": \"OK\"}";
        when(mockCdcResponse.getResponseText()).thenReturn(mockResponseText);
        when(cdcAccountsService.sendVerificationEmail(any())).thenReturn(mockCdcResponse);

        // when
        CDCResponseData response = cdcResponseHandler.sendVerificationEmail("test");

        // then
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK.value());
    }

    @Test
    public void sendVerificationEmail_whenGSResponseIsNull_returnResponseAsWithInternalServerErrorStatus() throws IOException {
        // given
        when(cdcAccountsService.sendVerificationEmail(any())).thenReturn(null);

        // when
        CDCResponseData response = cdcResponseHandler.sendVerificationEmail("test");

        // then
        Assert.assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void resetPasswordRequest_shouldRequestPasswordReset()
            throws CustomGigyaErrorException, LoginIdDoesNotExistException, GSKeyNotFoundException {
        // given
        GSObject gsObject = new GSObject();
        gsObject.put("passwordResetToken","");
        gsObject.put("requirePasswordCheck", false);
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        when(cdcAccountsService.resetPassword(any())).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getErrorCode()).thenReturn(0);
        when(mockCdcResponse.getData()).thenReturn(gsObject);

        // when
        cdcResponseHandler.resetPasswordRequest("armvalidtest@mail.com");

        // then
        verify(cdcAccountsService).resetPassword(any());
    }

    @Test(expected = LoginIdDoesNotExistException.class)
    public void resetPasswordRequest_whenAnInValidUsername_throwLoginIdDoesNotExistException()
            throws CustomGigyaErrorException, LoginIdDoesNotExistException, GSKeyNotFoundException {
        // given
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        when(cdcAccountsService.resetPassword(any())).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getErrorCode()).thenReturn(GigyaCodes.LOGIN_ID_DOES_NOT_EXIST.getValue());

        // when
        cdcResponseHandler.resetPasswordRequest("arminvalidtest@mail.com");
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void resetPasswordRequest_whenAnUnhandledErrorOccurs_throwCustomGigyaErrorException()
            throws CustomGigyaErrorException, LoginIdDoesNotExistException, GSKeyNotFoundException {
        // given
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        when(cdcAccountsService.resetPassword(any())).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getErrorCode()).thenReturn(1);

        // when
        cdcResponseHandler.resetPasswordRequest("arminvalidtest@mail.com");
    }

    @Test
    public void resetPasswordSubmit_whenANotValidToken_returnResponseWithError() {
        // given
        GSResponse mockErrorCdcResponse = Mockito.mock(GSResponse.class);
        String token = "testerrortoken";
        String newPassword = "testPassword1";
        int errorResponseCode = 40016;
        ResetPasswordSubmit request = ResetPasswordSubmit.builder().resetPasswordToken(token).newPassword(newPassword).build();
        when(cdcAccountsService.resetPassword(any())).thenReturn(mockErrorCdcResponse);
        when(mockErrorCdcResponse.getErrorCode()).thenReturn(errorResponseCode);

        // when
        ResetPasswordResponse response = cdcResponseHandler.resetPasswordSubmit(request);

        // then
        Assert.assertEquals(response.getResponseCode(),errorResponseCode);
    }

    @Test
    public void resetPasswordSubmit_whenAnValidToken_returnResponseWithResponseCode_0() {
        // given
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        String token = "testValidtoken";
        String newPassword = "testPassword1";
        int successResponseCode = 0;
        ResetPasswordSubmit request = ResetPasswordSubmit.builder().resetPasswordToken(token).newPassword(newPassword).build();

        when(cdcAccountsService.resetPassword(any())).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getErrorCode()).thenReturn(successResponseCode);

        // when
        ResetPasswordResponse response = cdcResponseHandler.resetPasswordSubmit(request);

        // then
        Assert.assertEquals(response.getResponseCode(),successResponseCode);
    }

    @Test
    public void getUsernameByEmail_whenNoResultsAreFound_returnEmptyUsername() throws Exception {
        // given
        GSObject jsonResponse = new GSObject("{\"callId\": \"8ba37e7693594a7da17a134e79dfb950\",\"errorCode\": 0,\"apiVersion\": 2,\"statusCode\": 200,\"statusReason\": \"OK\",\"time\": \"2020-08-26T18:13:18.021Z\",\"results\": [],\"objectsCount\": 0,\"totalCount\": 0}");
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        when(cdcAccountsService.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getErrorCode()).thenReturn(0);
        when(mockResponse.getData()).thenReturn(jsonResponse);

        // when
        String username = cdcResponseHandler.getUsernameByEmail("test");

        // then
        assertTrue(username.isEmpty());
    }

    @Test
    public void getUsernameByEmail_whenResultsAreFound_returnFirstyUsername() throws Exception {
        // given
        String testEmail = "this-is-a-test-88@mail.com";
        GSObject jsonResponse = new GSObject("{\"callId\": \"8ba37e7693594a7da17a134e79dfb950\",\"errorCode\": 0,\"apiVersion\": 2,\"statusCode\": 200,\"statusReason\": \"OK\",\"time\": \"2020-08-26T18:13:18.021Z\",\"results\": [{\"profile\": {\"username\":\"" + testEmail + "\"}}], \"objectsCount\": 0,\"totalCount\": 0}");
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        when(cdcAccountsService.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getErrorCode()).thenReturn(0);
        when(mockResponse.getData()).thenReturn(jsonResponse);

        // when
        String username = cdcResponseHandler.getUsernameByEmail("test");

        // then
        assertEquals(username, testEmail);
    }

    @Test
    public void getUsernameByEmail_whenAnExceptionIsThrown_returnEmptyUsername() throws Exception {
        // given
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        GSObject gsObject = Mockito.mock(GSObject.class);
        when(gsObject.getArray(any())).thenThrow(GSKeyNotFoundException.class);
        when(mockResponse.getData()).thenReturn(gsObject);
        when(cdcAccountsService.search(any(),any(), any())).thenReturn(mockResponse);

        // when
        String username = cdcResponseHandler.getUsernameByEmail("test");

        // then
        assertTrue(username.isEmpty());
    }

    @Test
    public void isAvailableLoginId_ShouldReturnIsAvailableResponse() throws Exception {
        // given
        boolean mockResponse = false;
        String data = String.format("{\"isAvailable\": \"%b\"}", mockResponse);
        GSObject gsObject = new GSObject(data);
        GSResponse mockGSResponse = Mockito.mock(GSResponse.class);
        when(mockGSResponse.getErrorCode()).thenReturn(0);
        when(mockGSResponse.getData()).thenReturn(gsObject);
        when(cdcAccountsService.isAvailableLoginId(any(), any())).thenReturn(mockGSResponse);

        // when
        boolean response = cdcResponseHandler.isAvailableLoginId("test");

        // then
        assertEquals(response, mockResponse);
    }

    @Test
    public void isAvailableLoginId_ShouldMakeCallToSecondaryDCIfFirstResponseIsTrue_ThenReturnIsAvailableResponse() throws Exception {
        // given
        String mainDCResponse = String.format("{\"isAvailable\": \"%b\"}", true);
        GSObject mainGsObject = new GSObject(mainDCResponse);
        GSResponse mainGsResponse = Mockito.mock(GSResponse.class);
        when(mainGsResponse.getErrorCode()).thenReturn(0);
        when(mainGsResponse.getData()).thenReturn(mainGsObject);

        boolean expectedResponse = true;
        String secondaryDCResponse = String.format("{\"isAvailable\": \"%b\"}", expectedResponse);
        GSObject secondaryGsObject = new GSObject(secondaryDCResponse);
        GSResponse secondaryGsResponse = Mockito.mock(GSResponse.class);
        when(secondaryGsResponse.getErrorCode()).thenReturn(0);
        when(secondaryGsResponse.getData()).thenReturn(secondaryGsObject);
        
        when(cdcAccountsService.isAvailableLoginId(any(), any())).thenReturn(mainGsResponse, secondaryGsResponse);

        try (MockedStatic<CDCUtils> cdcUtilsMock = Mockito.mockStatic(CDCUtils.class)) {
            cdcUtilsMock.when(() -> { CDCUtils.isSecondaryDCSupported(anyString()); }).thenReturn(true);

            // when
            boolean response = cdcResponseHandler.isAvailableLoginId("test");

            // then
            assertEquals(response, expectedResponse);
        }
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void isAvailableLoginID_GivenAnErrorOccurs_ShouldThrowCustomGigyaErrorException() throws Exception {
        // given
        int errorCode = 1;
        GSResponse mockGSResponse = Mockito.mock(GSResponse.class);
        when(mockGSResponse.getErrorCode()).thenReturn(errorCode);
        when(cdcAccountsService.isAvailableLoginId(any(), any())).thenReturn(mockGSResponse);

        // when
        cdcResponseHandler.isAvailableLoginId("test");
    }

    @Test
    public void getIdPInformation_ShouldReturnTheInformationForTheGivenIdP() throws Exception {
        // given
        final String IDP_NAME = "FID-NOVARTIS";
        String data = IdentityProviderUtils.getIdentityProviderJsonString();
        GSObject gsObject = new GSObject(data);
        GSResponse mockGSResponse = Mockito.mock(GSResponse.class);

        when(cdcIdentityProviderService.getIdPInformation(any())).thenReturn(mockGSResponse);
        when(mockGSResponse.getErrorCode()).thenReturn(0);
        when(mockGSResponse.getData()).thenReturn(gsObject);
        when(identityProviderBuilder.getIdPInformation(any())).thenCallRealMethod();

        IdentityProviderResponse expectedResponse = identityProviderBuilder.getIdPInformation(new GSObject(data));

        // when
        IdentityProviderResponse result = cdcResponseHandler.getIdPInformation(IDP_NAME);

        // then
        assertTrue(expectedResponse.getName() .equals(result.getName())
                && expectedResponse.getEntityID().equals(result.getEntityID()));
    }

    @Test
    public void getIdPInformation_ShouldNotReturnTheInformationIfTheIdPDoesNotExist() {
        // given
        final String IDP_NAME = "XX";
        final int ERROR_CODE = 1;
        GSResponse mockGSResponse = Mockito.mock(GSResponse.class);

        when(cdcIdentityProviderService.getIdPInformation(any())).thenReturn(mockGSResponse);
        when(mockGSResponse.getErrorCode()).thenReturn(ERROR_CODE);

        // when
        IdentityProviderResponse result = cdcResponseHandler.getIdPInformation(IDP_NAME);

        // then
        assertNull(result);
    }

    @Test
    public void getJWTPublicKey_Test() throws Exception {
        // given
        String n = "qoQa182GYedrbWwFc3UkC1hpZlnB2_E922yRJfHqpq2tTHL_NvjYmssVdJBgSKi36cptKqUJ0Phui9Z_kk8zMPrPfV16h0ZfBzKsvIy6_d7cWnn163BMz46kAHtZXqXhNuj19IZRCDfNoqVVxxCIYvbsgInbzZM82CB86iYPAS7piijYn1S6hueVHGAzQorOetZevKIAvbH3kJXZ4KdY6Ffz5SFDJBxC3bycN4q2JM1qnyD53vcc0MitxyIUF7a06iJb5_xXBiA-3xnTI0FU5hw_k6x-sdB5Rglx13_2aNzdWBSBAnxs1XXtZUt9_2RAUxP1XORkrBGlPg9D7cBtQ";
        String e = "SDAI";
        String responseData = String.format("{\"alg\":\"RS256\",\"apiVersion\":2,\"callId\":\"a98f09e409284cf788bd392e2aabd62c\",\"e\":\"%s\",\"errorCode\":0,\"kid\":\"REQ0MUQ5N0NCRTJEMzk3M0U1RkNDQ0U0Q0M1REFBRjhDMjdENUFBQg\",\"kty\":\"RSA\",\"n\":\"%s\",\"statusCode\":200,\"statusReason\":\"OK\",\"time\":\"2021-03-05T20:37:46.273Z\",\"use\":\"sig\"}", e, n);
        GSResponse mockGSResponse = Mockito.mock(GSResponse.class);
        when(cdcAccountsService.getJWTPublicKey()).thenReturn(mockGSResponse);
        when(mockGSResponse.getData()).thenReturn(new GSObject(responseData));

        // when
        JWTPublicKey result = cdcResponseHandler.getJWTPublicKey();

        // then
        assertTrue(n.equals(result.getN()));
        assertTrue(e.equals(result.getE()));
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void search_GivenTheresAResponseCodeDifferentThanZero_ThenItShouldThrowCustomGigyaErrorException() throws CustomGigyaErrorException, IOException {
        // given
        String query = "";
        String message = "Error";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        Mockito.when(cdcAccountsService.search(anyString(), any(), any())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(10040);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        // when
        cdcResponseHandler.search(query, AccountType.FULL_LITE, mainApiDomain);
    }

    @Test
    public void search_GivenTheresAValidResponse_ItShouldReturnTheSameNumberOfResultsAsCDCAccounts() throws CustomGigyaErrorException, IOException {
        // given
        String query = "";
        String uid = "18f6f06762725175ab3fa121d32d8992";
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{\"totalCount\": 1,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[{\"UID\":\"" + uid + "\",\"isRegistered\":true,\"profile\":{\"username\":\"armatest\",\"country\":\"MX\"}}]}";
        when(mockSearchResponse.getResponseText()).thenReturn(searchResponseJson);
        when(cdcAccountsService.search(anyString(), any(), any())).thenReturn(mockSearchResponse);

        // when
        CDCSearchResponse searchResponse = cdcResponseHandler.search(query, AccountType.FULL_LITE, mainApiDomain);
        List<CDCAccount> accounts = searchResponse.getResults();

        // then
        CDCAccount account = accounts.get(0);
        assertTrue(account.getUID().equals(uid));
        assertTrue(accounts.size() == 1);
    }

    @Test
    public void liteRegisterUser_GivenTheresAValidResponse_ItShouldReturnTheSameNumberOfResultsAsCDCAccounts() throws CustomGigyaErrorException, IOException {
        // given
        String uid = "9f6f2133e57144d787574d49c0b9908e";
        GSResponse cdcMockResponse = Mockito.mock(GSResponse.class);
        String liteRegResponse = "{\"callId\": \"5c62541d1ce341eba0faf1d14642c191\",\"UID\": \"" + uid + "\",\"apiVersion\": 2,\"statusReason\": \"OK\",\"errorCode\": 0,\"time\": \"2019-09-19T16:14:24.983Z\",\"statusCode\": 200}";
        when(cdcMockResponse.getResponseText()).thenReturn(liteRegResponse);
        when(cdcAccountsService.setLiteReg(anyString())).thenReturn(cdcMockResponse);

        // when
        CDCResponseData cdcResponse = cdcResponseHandler.liteRegisterUser("");

        // then
        assertEquals(uid, cdcResponse.getUID());
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void liteRegisterUser_GivenTheresAnError_ItShouldThrowCustomGigyaErrorException() throws CustomGigyaErrorException, IOException {
        // given
        int errorCode = 400;
        GSResponse cdcMockResponse = Mockito.mock(GSResponse.class);
        String liteRegError = "{\"callId\": \"349272dd0ec242d89e2be84c6692d0d2\",\"apiVersion\": 2,\"statusReason\": \"Bad Request\",\"errorMessage\": \"Invalid parameter value\", \"errorCode\": " + errorCode + ", \"validationErrors\": [{\"fieldName\": \"profile.email\",\"errorCode\": 400006,\"message\": \"Unallowed value for field: email\"}], \"time\": \"2019-09-19T16:15:20.508Z\",\"errorDetails\": \"Schema validation failed\", \"statusCode\":" + errorCode + "}";
        when(cdcMockResponse.getErrorCode()).thenReturn(errorCode);
        when(cdcMockResponse.getResponseText()).thenReturn(liteRegError);
        when(cdcAccountsService.setLiteReg(anyString())).thenReturn(cdcMockResponse);

        // when
        cdcResponseHandler.liteRegisterUser("");
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void changePassword_givenTheresAnError_ThenCustomGigyaErrorExceptionShouldBeThrown() throws CustomGigyaErrorException {
        // given
        String uid = Long.toString(1L);
        String oldPassword = "Hello there";
        String newPassword = "General Kenobi";
        int errorCode = 400;
        GSResponse cdcMockResponse = Mockito.mock(GSResponse.class);
        String gsResponse = "{\"callId\": \"349272dd0ec242d89e2be84c6692d0d2\",\"apiVersion\": 2,\"statusReason\": \"Bad Request\",\"errorMessage\": \"Invalid parameter value\", \"errorCode\": " + errorCode + ", \"validationErrors\": [{\"fieldName\": \"profile.email\",\"errorCode\": 400006,\"message\": \"Unallowed value for field: email\"}], \"time\": \"2019-09-19T16:15:20.508Z\",\"errorDetails\": \"Schema validation failed\", \"statusCode\":" + errorCode + "}";
        when(cdcMockResponse.getErrorCode()).thenReturn(errorCode);
        when(cdcMockResponse.getResponseText()).thenReturn(gsResponse);
        when(cdcAccountsService.changePassword(anyString(), anyString(), anyString())).thenReturn(cdcMockResponse);

        // when
        cdcResponseHandler.changePassword(uid, newPassword, oldPassword);
    }

    @Test
    public void getRP_givenMethodCalled_whenClientIdAndRe_returnAValidOpenIdConnectObject() throws Exception {
        //given
        String clientId = RandomStringUtils.random(10);
        List<String> uris = new ArrayList<>();
        uris.add("http://example.com");
        uris.add("http://example2.com");
        GSResponse mockGSResponse = mock(GSResponse.class);
        String responseData = String.format("{ \"statusCode\": 200, \"errorCode\": 0, \"statusReason\": \"OK\", \"callId\": \"0ac740a1d1444e\", \"time\": \"2016-03-22T10:20:18.732Z\", \"description\": \"This is a THIRD RP\", \"redirectUris\": [ \"http://example.com\", \"http://example2.com\" ], \"allowedScopes\": [ \"openid\", \"email\", \"profile\" ], \"clientID\": \"%s\", \"clientSecret\": \"7_CbWuu6UqokqyjAfhD9sz4xpHnBhW3r2KkI4\", \"supportedResponseTypes\": [ \"code\", \"id_token\", \"id_token token\", \"code id_token\", \"code token\", \"code id_token token\" ], \"subjectIdentifierType\": \"pairwise\" }", clientId);
        when(mockGSResponse.getData()).thenReturn(new GSObject(responseData));
        when(cdcAccountsService.getRP(clientId)).thenReturn(mockGSResponse);

        //when
        OpenIdRelyingParty result = cdcResponseHandler.getRP(clientId);

        //then
        assertTrue(clientId.equals(result.getClientId()));
        assertEquals(uris, result.getRedirectUris());
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void getRP_givenMethodCalled_whenClientIdNoExist_ThenCustomGigyaErrorExceptionShouldBeThrown() throws Exception {
        //given
        int errorCode = 404;
        String clientId = RandomStringUtils.random(10);
        GSResponse mockGSResponse = mock(GSResponse.class);
        String responseData = String.format("{ \"statusCode\": 404, \"errorCode\": 404000, \"statusReason\": \"NOT_FOUND\", \"callId\": \"0ac740a1d1444e\", \"time\": \"2016-03-22T10:20:18.732Z\", \"description\": \"This is a THIRD RP\", \"redirectUris\": [ \"http://example.com\", \"http://example2.com\" ], \"allowedScopes\": [ \"openid\", \"email\", \"profile\" ], \"clientID\": \"%s\", \"clientSecret\": \"7_CbWuu6UqokqyjAfhD9sz4xpHnBhW3r2KkI4\", \"supportedResponseTypes\": [ \"code\", \"id_token\", \"id_token token\", \"code id_token\", \"code token\", \"code id_token token\" ], \"subjectIdentifierType\": \"pairwise\" }", clientId);
        when(mockGSResponse.getData()).thenReturn(new GSObject(responseData));
        when(mockGSResponse.getErrorCode()).thenReturn(errorCode);
        when(cdcAccountsService.getRP(clientId)).thenReturn(mockGSResponse);

        //when
        cdcResponseHandler.getRP(clientId);
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void updateRequirePasswordCheck_givenTheresAnError_ThenCustomGigyaErrorExceptionShouldBeThrown() throws CustomGigyaErrorException {
        // given
        String uid = Long.toString(1L);
        int errorCode = 400;
        GSResponse cdcMockResponse = Mockito.mock(GSResponse.class);
        when(cdcMockResponse.getErrorCode()).thenReturn(errorCode);
        when(cdcAccountsService.updateRequirePasswordCheck(anyString())).thenReturn(cdcMockResponse);

        // when
        cdcResponseHandler.updateRequirePasswordCheck(uid);
    }

    @Test
    public void updateRequirePasswordCheck_ShouldUpdateRequirePasswordCheck() throws CustomGigyaErrorException, JsonParseException {
        // given
        String uid = "9f6f2133e57144d787574d49c0b9908e";
        GSResponse cdcMockResponse = Mockito.mock(GSResponse.class);
        String requirePasswordCheckResponse = "{\"callId\": \"5c62541d1ce341eba0faf1d14642c191\",\"UID\": \"" + uid + "\",\"apiVersion\": 2,\"statusReason\": \"OK\",\"errorCode\": 0,\"time\": \"2019-09-19T16:14:24.983Z\",\"statusCode\": 200}";
        when(cdcMockResponse.getResponseText()).thenReturn(requirePasswordCheckResponse);
        when(cdcAccountsService.updateRequirePasswordCheck(anyString())).thenReturn(cdcMockResponse);

        // when
        cdcResponseHandler.updateRequirePasswordCheck(uid);

        // then
        verify(cdcAccountsService).updateRequirePasswordCheck(uid);
    }

    @Test
    public void setAccountInfo_ShouldSetCDCAccountData() throws CustomGigyaErrorException {
        // given
        CDCAccount cdcAccount = CDCAccount.builder().build();
        GSResponse gsResponseMock = mock(GSResponse.class);
        when(cdcAccountsService.setAccountInfo(any())).thenReturn(gsResponseMock);

        // when
        cdcResponseHandler.setAccountInfo(cdcAccount);

        // then
        verify(cdcAccountsService).setAccountInfo(cdcAccount);
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void setAccountInfo_GivenTheresAnErrorFromCDC_ShouldThrowCustomGigyaErrorException() throws CustomGigyaErrorException {
        // given
        CDCAccount cdcAccount = CDCAccount.builder().build();
        GSResponse gsResponseMock = mock(GSResponse.class);
        when(gsResponseMock.getErrorCode()).thenReturn(400001);
        when(cdcAccountsService.setAccountInfo(any())).thenReturn(gsResponseMock);

        // when
        cdcResponseHandler.setAccountInfo(cdcAccount);
    }

    @Test
    public void getEmailByUsername_givenAnInvalidUsername_whenMethodIsCalled_thenReturnEmptyString() throws Exception {
        // given
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String searchResponse = "{\n" +
                "  \"totalCount\": 0,\n" +
                "  \"errorCode\": 400,\n" +
                "  \"results\": [\n" +
                "  ]\n" +
                "}";
        when(cdcAccountsService.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getResponseText()).thenReturn(searchResponse);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        String email = cdcResponseHandler.getEmailByUsername("test");

        // then
        assertTrue(email.isEmpty());
    }

    @Test
    public void getEmailByUsername_givenAValidUsername_whenMethodIsCalledAndResultContainsAVerifiedEmail_thenReturnTheFirstVerifiedEmail() throws Exception {
        // given
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String emailMock = "test@mail.com";
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 200, \"statusReason\": \"OK\", \"results\": [{ \"UID\": \"c1c691f4-556b-4ad1-ab75-841fc4e94dcd\", \"isRegistered\": true, \"profile\": { \"username\": \"armatest\", \"country\": \"MX\" }, \"emails\": { \"verified\": [\"" + emailMock + "\", \"test-two@mail.com\"] } }] }";
        when(cdcAccountsService.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        String email = cdcResponseHandler.getEmailByUsername("test");

        // then
        assertEquals(email, emailMock);
    }

    @Test
    public void getEmailByUsername_givenAValidUsername_whenMethodIsCalledAndResultContainsAnUnverifiedEmail_thenReturnTheFirstUnverifiedEmail() throws Exception {
        // given
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String emailMock = "test@mail.com";
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 200, \"statusReason\": \"OK\", \"results\": [{ \"UID\": \"c1c691f4-556b-4ad1-ab75-841fc4e94dcd\", \"isRegistered\": true, \"profile\": { \"username\": \"armatest\", \"country\": \"MX\" }, \"emails\": { \"verified\": [], \"unverified\": [\"" + emailMock + "\", \"test-two@mail.com\"] } }] }";
        when(cdcAccountsService.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        String email = cdcResponseHandler.getEmailByUsername("test");

        // then
        assertEquals(email, emailMock);
    }

    @Test
    public void getEmailByUsername_givenAValidUsername_whenMethodIsCalledAndTheUserDoesntHaveEmails_thenReturnAnEmptyString() throws Exception {
        // given
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 200, \"statusReason\": \"OK\", \"results\": [{ \"UID\": \"c1c691f4-556b-4ad1-ab75-841fc4e94dcd\", \"isRegistered\": true, \"profile\": { \"username\": \"armatest\", \"country\": \"MX\" }, \"emails\": { \"verified\": [], \"unverified\": [] } }] }";
        when(cdcAccountsService.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        String email = cdcResponseHandler.getEmailByUsername("test");

        // then
        assertEquals("", email);
    }

    @Test
    public void getUIDByEmail_givenAValidEmail_whenMethodIsCalled_thenShouldReturnAValidUID() throws Exception {
        // given
        String uidMock = "c1c691f4-556b-4ad1-ab75-841fc4e94dcd";
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 200, \"statusReason\": \"OK\", \"results\": [{ \"UID\": \"c1c691f4-556b-4ad1-ab75-841fc4e94dcd\", \"isRegistered\": true, \"profile\": { \"username\": \"armatest\", \"country\": \"MX\" }, \"emails\": { \"verified\": [], \"unverified\": [] } }] }";
        when(cdcAccountsService.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        String uid = cdcResponseHandler.getUIDByEmail("email@mail.com");

        // then
        assertEquals(uidMock, uid);
    }

    @Test
    public void getUIDByEmail_givenAnInvalidEmail_whenMethodIsCalled_thenShouldReturnAnEmptyString() throws Exception {
        // given
        String uidMock = "";
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 200, \"statusReason\": \"OK\", \"results\": [] }";
        when(cdcAccountsService.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        String uid = cdcResponseHandler.getUIDByEmail("email@mail.com");

        // then
        assertEquals(uidMock, uid);
    }

    @Test
    public void getAccountInfoByEmail_givenAnEmailThatDoesntExist_whenMethodIsCalled_thenShouldReturnNull() throws Exception {
        // given
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 200, \"statusReason\": \"OK\", \"results\": [{ \"UID\": \"\", \"isRegistered\": true, \"profile\": { \"username\": \"armatest\", \"country\": \"MX\" }, \"emails\": { \"verified\": [], \"unverified\": [] } }] }";
        when(cdcAccountsService.search(any(),any(), any())).thenReturn(mockResponse);
        when(cdcAccountsService.getAccount(any())).thenReturn(mockResponse);
        when(mockResponse.getData()).thenReturn(new GSObject(obj));
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        AccountInfo accountInfo = cdcResponseHandler.getAccountInfoByEmail("email@mail.com");

        // then
        assertNull(accountInfo);
    }

    @Test
    public void getAccountInfoByEmail_givenAnInvalidEmail_whenMethodIsCalled_thenShouldReturnAccountInfoAsNull() throws Exception {
        // given
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 200, \"statusReason\": \"OK\", \"results\": [{ \"UID\": \"c1c691f4-556b-4ad1-ab75-841fc4e94dcd\", \"isRegistered\": true, \"profile\": { \"username\": \"armatest\", \"country\": \"MX\" }, \"emails\": { \"verified\": [], \"unverified\": [] } }] }";
        when(cdcAccountsService.search(any(),any(), any())).thenReturn(mockResponse);
        when(cdcAccountsService.getAccount(any())).thenReturn(mockResponse);
        when(mockResponse.getData()).thenReturn(new GSObject(obj));
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        AccountInfo accountInfo = cdcResponseHandler.getAccountInfoByEmail("email@mail.com");

        // then
        assertNotNull(accountInfo);
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void searchInBothDC_GivenTheresAResponseCodeDifferentThanZero_ThenItShouldThrowCustomGigyaErrorException() throws CustomGigyaErrorException, IOException {
        // given
        String username = "armatest@test.com";
        String message = "Error";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        Mockito.when(cdcAccountsService.search(anyString(), any(), any())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(10040);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        // when
        cdcResponseHandler.searchInBothDC(username);
    }

    @Test
    public void searchInBothDC_GivenTheEmailExistsInTheMainApiDomain_ItShouldReturnTheAccountFound() throws CustomGigyaErrorException, IOException {
        // given
        String username = "armatest@test.com";
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{\"totalCount\": 1,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[{\"UID\":\"" + uid + "\",\"isRegistered\":true,\"profile\":{\"username\":\"" + username + "\",\"country\":\"MX\"}}]}";
        when(mockSearchResponse.getResponseText()).thenReturn(searchResponseJson);
        when(cdcAccountsService.search(anyString(), any(), any())).thenReturn(mockSearchResponse);

        try (MockedStatic<CDCUtils> cdcUtilsMock = Mockito.mockStatic(CDCUtils.class)) {
            cdcUtilsMock.when(() -> CDCUtils.isSecondaryDCSupported(any())).thenReturn(true);

            // when
            CDCSearchResponse searchResponse = cdcResponseHandler.searchInBothDC(username);
            List<CDCAccount> accounts = searchResponse.getResults();

            // then
            CDCAccount account = accounts.get(0);
            assertEquals(uid, account.getUID());
            assertEquals(1, accounts.size());
        }
    }

    @Test
    public void searchInBothDC_GivenTheUserNameDoesNotExistInTheMainAPIDomain_ItShouldSearchInTheSecondaryDomain_AndReturnTheAccountFound() throws CustomGigyaErrorException, IOException {
        // given
        String username = "armatest@test.com";
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String mainDCSearchResponseJson = "{\"totalCount\": 0,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[]}";
        String secondaryDCResponseJson = "{\"totalCount\": 1,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[{\"UID\":\"" + uid + "\",\"isRegistered\":true,\"profile\":{\"username\":\"" + username + "\",\"country\":\"MX\"}}]}";

        when(mockSearchResponse.getResponseText()).thenReturn(mainDCSearchResponseJson, secondaryDCResponseJson);
        when(cdcAccountsService.search(anyString(), any(), any())).thenReturn(mockSearchResponse);


        try (MockedStatic<CDCUtils> cdcUtilsMock = Mockito.mockStatic(CDCUtils.class)) {
            cdcUtilsMock.when(() -> CDCUtils.isSecondaryDCSupported(any())).thenReturn(true);
            // when
            CDCSearchResponse searchResponse = cdcResponseHandler.searchInBothDC(username);

            List<CDCAccount> accounts = searchResponse.getResults();

            // then
            CDCAccount account = accounts.get(0);
            assertEquals(uid, account.getUID());
            assertEquals(1, accounts.size());
        }
    }

    @Test
    public void searchInBothDC_GivenTheUserNameDoesNotExistInTheMainAPIDomain_ItShouldSearchInTheSecondaryDomain_AndReturnThatAnyAccountWasFound() throws CustomGigyaErrorException, IOException {
        // given
        String username = "armatest@test.com";
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String mainDCSearchResponseJson = "{\"totalCount\": 0,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[]}";
        String secondaryDCResponseJson = "{\"totalCount\": 0,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[]}";

        when(mockSearchResponse.getResponseText()).thenReturn(mainDCSearchResponseJson, secondaryDCResponseJson);
        when(cdcAccountsService.search(anyString(), any(), any())).thenReturn(mockSearchResponse);


        try (MockedStatic<CDCUtils> cdcUtilsMock = Mockito.mockStatic(CDCUtils.class)) {
            cdcUtilsMock.when(() -> CDCUtils.isSecondaryDCSupported(any())).thenReturn(true);
            // when
            CDCSearchResponse searchResponse = cdcResponseHandler.searchInBothDC(username);

            List<CDCAccount> accounts = searchResponse.getResults();

            // then
            assertEquals(0, accounts.size());
        }
    }

    @Test
    public void searchInBothDC_GivenTheUserNameDoesNotExistInTheMainAPIDomain_AndThereIsNotASecondaryDomain_ThenShouldReturnAnyAccountsFound() throws CustomGigyaErrorException, IOException {
        // given
        String username = "armatest@test.com";
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String mainDCSearchResponseJson = "{\"totalCount\": 0,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[]}";
        String secondaryDCResponseJson = "{\"totalCount\": 0,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[]}";

        when(mockSearchResponse.getResponseText()).thenReturn(mainDCSearchResponseJson, secondaryDCResponseJson);
        when(cdcAccountsService.search(anyString(), any(), any())).thenReturn(mockSearchResponse);


        try (MockedStatic<CDCUtils> cdcUtilsMock = Mockito.mockStatic(CDCUtils.class)) {
            cdcUtilsMock.when(() -> CDCUtils.isSecondaryDCSupported(any())).thenReturn(false);
            // when
            CDCSearchResponse searchResponse = cdcResponseHandler.searchInBothDC(username);

            List<CDCAccount> accounts = searchResponse.getResults();

            // then
            assertEquals(0, accounts.size());
        }
    }
}
