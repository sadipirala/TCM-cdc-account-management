package com.thermofisher.cdcam;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.model.ResetPasswordResponse;
import com.thermofisher.cdcam.model.ResetPasswordSubmit;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.services.CDCAccountsService;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.CustomGigyaErrorException;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;

import org.apache.logging.log4j.LogManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    private final String department = "dep";
    private final String company = "company";
    private String obj = "{\"socialProviders\":\"site,oidc-fedspikegidp\",\"lastLogin\":\"2019-08-21T23:13:38.284Z\",\"userInfo\":{\"country\":\"United States\",\"isTempUser\":false,\"oldestDataAge\":-2147483648,\"capabilities\":\"None\",\"isSiteUID\":true,\"loginProviderUID\":\"ef632aa3f52140aa836673469378d0ac\",\"city\":\"" + city + "\",\"isConnected\":true,\"errorCode\":0,\"isSiteUser\":true,\"loginProvider\":\"oidc-fedspikegidp\",\"oldestDataUpdatedTimestamp\":0,\"UID\":\"ffb10070d8174a518f2e8b403c1efe5d\",\"identities\":[{\"country\":\"United States\",\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"lastUpdatedTimestamp\":1566429217356,\"isExpiredSession\":false,\"allowsLogin\":false,\"city\":\"" + city + "\",\"provider\":\"site\",\"isLoginIdentity\":false,\"oldestDataUpdated\":\"0001-01-01T00:00:00Z\",\"oldestDataUpdatedTimestamp\":0,\"providerUID\":\"ffb10070d8174a518f2e8b403c1efe5d\"},{\"lastUpdated\":\"2019-08-21T23:13:38.284Z\",\"lastUpdatedTimestamp\":1566429218284,\"isExpiredSession\":false,\"allowsLogin\":true,\"provider\":\"oidc-fedspikegidp\",\"isLoginIdentity\":true,\"nickname\":\"federatedUser\",\"oldestDataUpdated\":\"2019-08-21T23:01:23.988Z\",\"oidcData\":{},\"oldestDataUpdatedTimestamp\":1566428483988,\"email\":\"test@gmail.com\",\"providerUID\":\"ef632aa3f52140aa836673469378d0ac\"}],\"statusReason\":\"OK\",\"nickname\":\"federatedUser\",\"isLoggedIn\":true,\"time\":\"2019-08-23T23:50:35.918Z\",\"email\":\"test@gmail.com\",\"providers\":\"site,oidc-fedspikegidp\",\"statusCode\":200},\"data\":{\"terms\":true},\"isVerified\":true,\"errorCode\":0,\"registered\":\"2019-08-19T21:11:52.372Z\",\"isActive\":true,\"oldestDataUpdatedTimestamp\":1566248846440,\"emails\":{\"verified\":[\"test@gmail.com\"],\"unverified\":[]},\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"apiVersion\":2,\"statusReason\":\"OK\",\"verifiedTimestamp\":1566248848104,\"oldestDataUpdated\":\"2019-08-19T21:07:26.440Z\",\"callId\":\"52317e98c0a849438f432669c5d198f0\",\"lastUpdatedTimestamp\":1566429217356,\"created\":\"2019-08-19T21:07:26.440Z\",\"createdTimestamp\":1566248846000,\"profile\":{\"firstName\":\"" + firstName + "\",\"lastName\":\"" + lastName + "\",\"work\":{\"company\":\"" + company + "\",\"location\":\"" + department + "\"},\"country\":\"" + country + "\",\"city\":\"" + city + "\",\"nickname\":\"federatedUser\",\"email\":\"" + emailAddress + "\"},\"regSource\":\"http://dev2.apps.thermofisher.com/apps/fedspike/enterpriselogin\",\"verified\":\"2019-08-19T21:07:28.104Z\",\"registeredTimestamp\":1566249112000,\"loginProvider\":\"oidc-fedspikegidp\",\"lastLoginTimestamp\":1566429218000,\"UID\":\"" + uid + "\",\"isRegistered\":true,\"time\":\"2019-08-23T23:50:35.919Z\",\"statusCode\":200}";

    @InjectMocks
    CDCResponseHandler cdcResponseHandler;

    @Mock
    CDCAccountsService cdcAccountsService;

    @Mock
    AccountBuilder accountBuilder;

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

    @Test
    public void getAccountInfo_WhenAGetAccountRequestInfoRequestIsResolvedWithError_ShouldReturnNull() throws Exception {
        // given
        final int ERROR_CODE = new Random().nextInt(10) + 1;
        GSResponse gsResponse = Mockito.mock(GSResponse.class);
        when(cdcAccountsService.getAccount(anyString())).thenReturn(gsResponse);
        when(gsResponse.getErrorCode()).thenReturn(ERROR_CODE);

        // when
        AccountInfo account = cdcResponseHandler.getAccountInfo(uid);

        // then
        assertNull(account);
    }

    @Test
    public void update_WhenGSResponseCodeIsZero_AnObjectNodeWith200ErrorCodeShouldBeReturned()
            throws JSONException {
        // given
        String message = "Success";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        Mockito.when(cdcAccountsService.setUserInfo(anyString(), anyString(), anyString())).thenReturn(mockCdcResponse);
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
        Mockito.when(cdcAccountsService.setUserInfo(anyString(), anyString(), anyString())).thenReturn(mockCdcResponse);
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
    public void searchDuplicatedAccountUid_WhenAValidUidAndFederatedEmailAreProvided_ThenReturnUidMatchInCDC() throws IOException {
        String uid = "0001";
        String fedEmail = "test@mail.com";
        int successCode = 0;
        String duplicatedAccountUid = "decb11cd6ed2442c99b380f56c4b47aa";
        String message = "Found matching UID in CDC.";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        String mockResponseText = "{\n" +
                "  \"totalCount\": 1,\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\",\n" +
                "  \"results\": [\n" +
                "{ \"UID\": \"decb11cd6ed2442c99b380f56c4b47aa\",\n" +
                "  \"loginIDs\": {" +
                "  \"emails\": ["+
                "  \"mariaguadalupe.chacon@thermofisher.com\"" +
                "]," +
                "  \"unverifiedEmails\": []" +
                "} " +
                "} " +
                " ]\n" +
                "}";


        Mockito.when(cdcAccountsService.search(anyString(),anyString())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(successCode);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);
        Mockito.when(mockCdcResponse.getResponseText()).thenReturn(mockResponseText);

        // when
        String responseUid = cdcResponseHandler.searchDuplicatedAccountUid(uid,fedEmail);

        // then
        Assert.assertEquals(responseUid,duplicatedAccountUid);
    }

    @Test
    public void searchDuplicatedAccountUid_WhenAValidUidAndFederatedEmailAreProvidedAndNoEmailWasFoundInCDC_ThenReturnEmptyString() throws IOException {
        String uid = "0001";
        String fedEmail = "test@mail.com";
        int successCode = 0;
        String noResultsFound = "";
        String message = "Could not match an account with that email on CDC.";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        String mockResponseText = "{\n" +
                "  \"totalCount\": 0,\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\",\n" +
                "  \"results\": [\n" +
                "  ]\n" +
                "}";


        Mockito.when(cdcAccountsService.search(anyString(),anyString())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(successCode);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);
        Mockito.when(mockCdcResponse.getResponseText()).thenReturn(mockResponseText);

        // when
        String responseUid = cdcResponseHandler.searchDuplicatedAccountUid(uid,fedEmail);

        // then
        Assert.assertEquals(responseUid, noResultsFound);
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

        Mockito.when(cdcAccountsService.search(anyString(), anyString())).thenReturn(mockCdcResponse);
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

        String mockResponseText = "{\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\"" +
                "}";

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
    public void resetPasswordRequest_whenAValidUsername_returnTrue() {
        // given
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        String username = "armvalidtest@mail.com";
        when(cdcAccountsService.resetPassword(any())).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getErrorCode()).thenReturn(0);

        // when
        boolean response = cdcResponseHandler.resetPasswordRequest(username);

        // then
        Assert.assertTrue(response);
    }

    @Test
    public void resetPasswordRequest_whenAnInValidUsername_returnFalse() {
        // given
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        String username = "arminvalidtest@mail.com";
        when(cdcAccountsService.resetPassword(any())).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getErrorCode()).thenReturn(40016);

        // when
        boolean response = cdcResponseHandler.resetPasswordRequest(username);

        // then
        Assert.assertFalse(response);
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
    public void getEmailByUsername_whenNoResultsAreFound_returnEmptyUsername() throws Exception {
        // given
        GSObject jsonResponse = new GSObject("{\n" +
                "  \"callId\": \"8ba37e7693594a7da17a134e79dfb950\",\n" +
                "  \"errorCode\": 0,\n" +
                "  \"apiVersion\": 2,\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\",\n" +
                "  \"time\": \"2020-08-26T18:13:18.021Z\",\n" +
                "  \"results\": [],\n" +
                "  \"objectsCount\": 0,\n" +
                "  \"totalCount\": 0\n" +
                "}");
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        when(cdcAccountsService.search(any(),any())).thenReturn(mockResponse);
        when(mockResponse.getErrorCode()).thenReturn(0);
        when(mockResponse.getData()).thenReturn(jsonResponse);

        // when
        String username = cdcResponseHandler.getUsernameByEmail("test");

        // then
        assertTrue(username.isEmpty());
    }

    @Test
    public void getEmailByUsername_whenResultsAreFound_returnFirstyUsername() throws Exception {
        // given
        String testEmail = "this-is-a-test-88@mail.com";
        GSObject jsonResponse = new GSObject("{\n" +
                "  \"callId\": \"8ba37e7693594a7da17a134e79dfb950\",\n" +
                "  \"errorCode\": 0,\n" +
                "  \"apiVersion\": 2,\n" +
                "  \"statusCode\": 200,\n" +
                "  \"statusReason\": \"OK\",\n" +
                "  \"time\": \"2020-08-26T18:13:18.021Z\",\n" +
                "  \"results\": [{\n" +
                "      \"profile\": {\n" +
                "        \"username\":\""+testEmail+"\"\n" +
                "      }\n" +
                "    }],\n" +
                "  \"objectsCount\": 0,\n" +
                "  \"totalCount\": 0\n" +
                "}");
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        when(cdcAccountsService.search(any(),any())).thenReturn(mockResponse);
        when(mockResponse.getErrorCode()).thenReturn(0);
        when(mockResponse.getData()).thenReturn(jsonResponse);

        // when
        String username = cdcResponseHandler.getUsernameByEmail("test");

        // then
        assertEquals(username, testEmail);
    }

    @Test
    public void getEmailByUsername_whenAnExceptionIsThrown_returnEmptyUsername() throws Exception {
        // given
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        when(cdcAccountsService.search(any(),any())).thenReturn(mockResponse);
        when(mockResponse.getErrorCode()).thenThrow(Exception.class);

        // when
        String username = cdcResponseHandler.getUsernameByEmail("test");

        // then
        assertTrue(username.isEmpty());
    }

    @Test
    public void isAvailableLoginID_ShouldReturnIsAvailableResponse() throws Exception {
        // given
        boolean mockResponse = true;
        String data = String.format("{\"isAvailable\": \"%b\"}", mockResponse);
        GSObject gsObject = new GSObject(data);
        GSResponse mockGSResponse = Mockito.mock(GSResponse.class);
        when(cdcAccountsService.isAvailableLoginID(any())).thenReturn(mockGSResponse);
        when(mockGSResponse.getErrorCode()).thenReturn(0);
        when(mockGSResponse.getData()).thenReturn(gsObject);

        // when
        boolean response = cdcResponseHandler.isAvailableLoginID("test");

        // then
        assertEquals(response, mockResponse);
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void isAvailableLoginID_GivenAnErrorOccurs_ShouldThrowCustomGigyaErrorException() throws Exception {
        // given
        int errorCode = 1;
        GSResponse mockGSResponse = Mockito.mock(GSResponse.class);
        when(cdcAccountsService.isAvailableLoginID(any())).thenReturn(mockGSResponse);
        when(mockGSResponse.getErrorCode()).thenReturn(errorCode);

        // when
        cdcResponseHandler.isAvailableLoginID("test");
    }
}
