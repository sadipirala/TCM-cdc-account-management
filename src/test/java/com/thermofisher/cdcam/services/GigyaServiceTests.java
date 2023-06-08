package com.thermofisher.cdcam.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.thermofisher.cdcam.model.cdc.*;
import com.thermofisher.cdcam.model.dto.LiteAccountDTO;
import com.thermofisher.cdcam.model.identityProvider.IdentityProviderResponse;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.IdentityProviderUtils;
import com.thermofisher.cdcam.utils.cdc.CDCUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class GigyaServiceTests {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String uid = "c1c691f4-556b-4ad1-ab75-841fc4e94dcd";
    private final String emailAddress = "federatedUser@OIDC.com";
    private final String firstName = "first";
    private final String lastName = "first";
    private final String country = "United States";
    private final String city = "testCity";
    private final String company = "company";
    private final String mainDataCenter = "us";
    private final String secondaryDataCenter = "cn";
    private final String mainApiDomain = "us1.gigya.com";
    private final String secondaryApiDomain = "cn1.sapcdm.com";
    private String obj = "{\"socialProviders\":\"site,oidc-fedspikegidp\",\"lastLogin\":\"2019-08-21T23:13:38.284Z\",\"userInfo\":{\"country\":\"United States\",\"isTempUser\":false,\"oldestDataAge\":-2147483648,\"capabilities\":\"None\",\"isSiteUID\":true,\"loginProviderUID\":\"ef632aa3f52140aa836673469378d0ac\",\"city\":\"" + city + "\",\"isConnected\":true,\"errorCode\":0,\"isSiteUser\":true,\"loginProvider\":\"oidc-fedspikegidp\",\"oldestDataUpdatedTimestamp\":0,\"UID\":\"ffb10070d8174a518f2e8b403c1efe5d\",\"identities\":[{\"country\":\"United States\",\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"lastUpdatedTimestamp\":1566429217356,\"isExpiredSession\":false,\"allowsLogin\":false,\"city\":\"" + city + "\",\"provider\":\"site\",\"isLoginIdentity\":false,\"oldestDataUpdated\":\"0001-01-01T00:00:00Z\",\"oldestDataUpdatedTimestamp\":0,\"providerUID\":\"ffb10070d8174a518f2e8b403c1efe5d\"},{\"lastUpdated\":\"2019-08-21T23:13:38.284Z\",\"lastUpdatedTimestamp\":1566429218284,\"isExpiredSession\":false,\"allowsLogin\":true,\"provider\":\"oidc-fedspikegidp\",\"isLoginIdentity\":true,\"nickname\":\"federatedUser\",\"oldestDataUpdated\":\"2019-08-21T23:01:23.988Z\",\"oidcData\":{},\"oldestDataUpdatedTimestamp\":1566428483988,\"email\":\"test@gmail.com\",\"providerUID\":\"ef632aa3f52140aa836673469378d0ac\"}],\"statusReason\":\"OK\",\"nickname\":\"federatedUser\",\"isLoggedIn\":true,\"time\":\"2019-08-23T23:50:35.918Z\",\"email\":\"test@gmail.com\",\"providers\":\"site,oidc-fedspikegidp\",\"statusCode\":200},\"data\":{\"terms\":true},\"isVerified\":true,\"errorCode\":0,\"registered\":\"2019-08-19T21:11:52.372Z\",\"isActive\":true,\"oldestDataUpdatedTimestamp\":1566248846440,\"emails\":{\"verified\":[\"test@gmail.com\"],\"unverified\":[]},\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"apiVersion\":2,\"statusReason\":\"OK\",\"verifiedTimestamp\":1566248848104,\"oldestDataUpdated\":\"2019-08-19T21:07:26.440Z\",\"callId\":\"52317e98c0a849438f432669c5d198f0\",\"lastUpdatedTimestamp\":1566429217356,\"created\":\"2019-08-19T21:07:26.440Z\",\"createdTimestamp\":1566248846000,\"profile\":{\"firstName\":\"" + firstName + "\",\"lastName\":\"" + lastName + "\",\"work\":{\"company\":\"" + company + "\"},\"country\":\"" + country + "\",\"city\":\"" + city + "\",\"nickname\":\"federatedUser\",\"email\":\"" + emailAddress + "\"},\"regSource\":\"http://dev2.apps.thermofisher.com/apps/fedspike/enterpriselogin\",\"verified\":\"2019-08-19T21:07:28.104Z\",\"registeredTimestamp\":1566249112000,\"loginProvider\":\"oidc-fedspikegidp\",\"lastLoginTimestamp\":1566429218000,\"UID\":\"" + uid + "\",\"isRegistered\":true,\"time\":\"2019-08-23T23:50:35.919Z\",\"statusCode\":200}";
    private String objV2 = "{\"socialProviders\":\"site,oidc-fedspikegidp\",\"lastLogin\":\"2019-08-21T23:13:38.284Z\",\"userInfo\":{\"country\":\"United States\",\"isTempUser\":false,\"oldestDataAge\":-2147483648,\"capabilities\":\"None\",\"isSiteUID\":true,\"loginProviderUID\":\"ef632aa3f52140aa836673469378d0ac\",\"city\":\"city\",\"isConnected\":true,\"errorCode\":0,\"isSiteUser\":true,\"loginProvider\":\"oidc-fedspikegidp\",\"oldestDataUpdatedTimestamp\":0,\"UID\":\"ffb10070d8174a518f2e8b403c1efe5d\",\"identities\":[{\"country\":\"United States\",\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"lastUpdatedTimestamp\":1566429217356,\"isExpiredSession\":false,\"allowsLogin\":false,\"city\":\"city\",\"provider\":\"site\",\"isLoginIdentity\":false,\"oldestDataUpdated\":\"0001-01-01T00:00:00Z\",\"oldestDataUpdatedTimestamp\":0,\"providerUID\":\"ffb10070d8174a518f2e8b403c1efe5d\"},{\"lastUpdated\":\"2019-08-21T23:13:38.284Z\",\"lastUpdatedTimestamp\":1566429218284,\"isExpiredSession\":false,\"allowsLogin\":true,\"provider\":\"oidc-fedspikegidp\",\"isLoginIdentity\":true,\"nickname\":\"federatedUser\",\"oldestDataUpdated\":\"2019-08-21T23:01:23.988Z\",\"oidcData\":{},\"oldestDataUpdatedTimestamp\":1566428483988,\"email\":\"test@gmail.com\",\"providerUID\":\"ef632aa3f52140aa836673469378d0ac\"}],\"statusReason\":\"OK\",\"nickname\":\"federatedUser\",\"isLoggedIn\":true,\"time\":\"2019-08-23T23:50:35.918Z\",\"email\":\"test@gmail.com\",\"providers\":\"site,oidc-fedspikegidp\",\"statusCode\":200},\"data\":{\"terms\":true},\"isVerified\":true,\"errorCode\":0,\"registered\":\"2019-08-19T21:11:52.372Z\",\"isActive\":true,\"oldestDataUpdatedTimestamp\":1566248846440,\"emails\":{\"verified\":[\"test@gmail.com\"],\"unverified\":[]},\"preferences\":{\"marketing\":{\"consent\":{\"isConsentGranted\":true}},\"korea\":{\"receiveMarketingInformation\":{\"isConsentGranted\":false},\"thirdPartyTransferPersonalInfoMandatory\":{\"isConsentGranted\":false},\"thirdPartyTransferPersonalInfoOptional\":{\"isConsentGranted\":false},\"collectionAndUsePersonalInfoMandatory\":{\"isConsentGranted\":false},\"collectionAndUsePersonalInfoOptional\":{\"isConsentGranted\":false},\"collectionAndUsePersonalInfoMarketing\":{\"isConsentGranted\":false},\"overseasTransferPersonalInfoMandatory\":{\"isConsentGranted\":false},\"overseasTransferPersonalInfoOptional\":{\"isConsentGranted\":false}}},\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"apiVersion\":2,\"statusReason\":\"OK\",\"verifiedTimestamp\":1566248848104,\"oldestDataUpdated\":\"2019-08-19T21:07:26.440Z\",\"callId\":\"52317e98c0a849438f432669c5d198f0\",\"lastUpdatedTimestamp\":1566429217356,\"created\":\"2019-08-19T21:07:26.440Z\",\"createdTimestamp\":1566248846000,\"profile\":{\"firstName\":\"firstName\",\"lastName\":\"lastName\",\"work\":{\"company\":\"company\"},\"country\":\"country\",\"city\":\"city\",\"nickname\":\"federatedUser\",\"email\":\"arm@test.com\"},\"regSource\":\"http://dev2.apps.thermofisher.com/apps/fedspike/enterpriselogin\",\"verified\":\"2019-08-19T21:07:28.104Z\",\"registeredTimestamp\":1566249112000,\"loginProvider\":\"oidc-fedspikegidp\",\"lastLoginTimestamp\":1566429218000,\"UID\":\"10293847\",\"isRegistered\":true,\"time\":\"2019-08-23T23:50:35.919Z\",\"statusCode\":200}";
    private JSONObject loginIdAvailable = new JSONObject();
    private JSONObject loginIdNotAvailable = new JSONObject();

    
    @InjectMocks
    GigyaService gigyaService;

    @Mock
    GigyaApi gigyaApi;

    @Mock
    AccountBuilder accountBuilder;

    @Mock
    CDCIdentityProviderService cdcIdentityProviderService;

    @Mock
    IdentityProviderBuilder identityProviderBuilder;

    @Captor
    ArgumentCaptor<GSObject> gsObjectCaptor;

    @Before
    public void setup() throws JSONException {
        ReflectionTestUtils.setField(accountBuilder, "logger", LogManager.getLogger(AccountBuilder.class));
        loginIdAvailable.put("totalCount", 0);
        loginIdNotAvailable.put("totalCount", 1);
    }

    private void setProperties() {
        ReflectionTestUtils.setField(gigyaService, "mainApiDomain", mainApiDomain);
        ReflectionTestUtils.setField(gigyaService, "mainDataCenter", "us");
        ReflectionTestUtils.setField(gigyaService, "secondaryApiDomain", secondaryApiDomain);
        ReflectionTestUtils.setField(gigyaService, "secondaryDataCenter", "cn");
    }

    @Test
    public void getAccount_WhenAGetAccountRequestInfoIsMade_ShouldReturnAnAccountInfoObjectWithResponseData() throws Exception {
        // given
        ReflectionTestUtils.setField(gigyaService, "isNewMarketingConsentEnabled", false);
        GSResponse gsResponse = Mockito.mock(GSResponse.class);
        GSObject mockedGSObject = new GSObject(obj);
        when(gigyaApi.getAccount(anyString())).thenReturn(gsResponse);
        when(gsResponse.getData()).thenReturn(mockedGSObject);
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        AccountInfo accountInfo = accountBuilder.getAccountInfo(new GSObject(obj));

        // when
        AccountInfo account = gigyaService.getAccountInfo(uid);

        // then
        String _accountInfo = mapper.writeValueAsString(accountInfo);
        String _account = mapper.writeValueAsString(account);
        assertTrue(_accountInfo.equals(_account));
    }
    
    @Test
    public void getAccount_WhenAGetAccountRequestInfoIsMade_ShouldReturnAnAccountInfoObjectWithResponseData_V2() throws Exception {
        // given
        ReflectionTestUtils.setField(gigyaService, "isNewMarketingConsentEnabled", true);
        GSResponse gsResponse = Mockito.mock(GSResponse.class);
        GSObject mockedGSObject = new GSObject(obj);
        when(gigyaApi.getAccountV2(anyString())).thenReturn(gsResponse);
        when(gsResponse.getData()).thenReturn(mockedGSObject);
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();
        AccountInfo accountInfo = accountBuilder.getAccountInfoV2(new GSObject(obj));

        // when
        AccountInfo account = gigyaService.getAccountInfo(uid);

        // then
        String _accountInfo = mapper.writeValueAsString(accountInfo);
        String _account = mapper.writeValueAsString(account);
        assertTrue(_accountInfo.equals(_account));
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void getAccountInfo_WhenAGetAccountRequestInfoRequestIsResolvedWithError_ShouldThrowCustomGigyaErrorException() throws Exception {
        // given
        ReflectionTestUtils.setField(gigyaService, "isNewMarketingConsentEnabled", false);
        final int ERROR_CODE = new Random().nextInt(10) + 1;
        GSResponse gsResponse = Mockito.mock(GSResponse.class);
        when(gigyaApi.getAccount(anyString())).thenReturn(gsResponse);
        when(gsResponse.getErrorCode()).thenReturn(ERROR_CODE);

        // when
        gigyaService.getAccountInfo(uid);
    }
    
    @Test(expected = CustomGigyaErrorException.class)
    public void getAccountInfo_WhenAGetAccountRequestInfoRequestIsResolvedWithError_ShouldThrowCustomGigyaErrorException_V2() throws Exception {
        // given
        ReflectionTestUtils.setField(gigyaService, "isNewMarketingConsentEnabled", true);
        final int ERROR_CODE = new Random().nextInt(10) + 1;
        GSResponse gsResponse = Mockito.mock(GSResponse.class);
        when(gigyaApi.getAccountV2(anyString())).thenReturn(gsResponse);
        when(gsResponse.getErrorCode()).thenReturn(ERROR_CODE);

        // when
        gigyaService.getAccountInfo(uid);
    }

    @Test
    public void update_WhenGSResponseCodeIsZero_AnObjectNodeWith200ErrorCodeShouldBeReturned() throws JSONException {
        // given
        String message = "Success";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        Mockito.when(gigyaApi.setUserInfo(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(0);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        JSONObject user = new JSONObject("{\"data\":{\"regStatus\":true},\"profile\":{\"username\":\"test@test.com\"}}");

        // when
        ObjectNode updateResponse = gigyaService.update(user);

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
        Mockito.when(gigyaApi.setUserInfo(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(errorCode);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        JSONObject user = new JSONObject("{\"data\":{\"regStatus\":true},\"profile\":{\"username\":\"test@test.com\"}}");

        // when
        ObjectNode updateResponse = gigyaService.update(user);

        // then
        Assert.assertEquals(errorCode, updateResponse.get("code").asInt());
        Assert.assertEquals(message, updateResponse.get("error").asText());
    }

    @Test
    public void update_shouldReturnNull_whenAccountIsNull() {
        // given
        String message = "Something went bad.";
        int errorCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        Mockito.when(gigyaApi.setUserInfo(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(errorCode);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        // when
        ObjectNode updateResponse = gigyaService.update(null);

        // then
        assertNull(updateResponse);
    }

    @Test
    public void update_shouldSetDefaultDataAndProfile_whenValuesFromJSONAreNull() throws JSONException {
        // given
        String message = "Success";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        Mockito.when(gigyaApi.setUserInfo(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(0);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        JSONObject user = new JSONObject("{}");

        // when
        ObjectNode updateResponse = gigyaService.update(user);

        // then
        assertEquals(HttpStatus.OK.value(), updateResponse.get("code").asInt());
        assertEquals(message, updateResponse.get("error").asText());
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

        Mockito.when(gigyaApi.changeAccountStatus(anyString(),anyBoolean())).thenReturn(mockChangeStatusResponse);
        Mockito.when(mockChangeStatusResponse.getErrorCode()).thenReturn(successCode);

        // when
        boolean updateResponse = gigyaService.disableAccount(uid);

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

        Mockito.when(gigyaApi.search(anyString(), any(), any())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(errorCode);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        Mockito.when(gigyaApi.changeAccountStatus(anyString(), anyBoolean())).thenReturn(mockChangeStatusResponse);
        Mockito.when(mockChangeStatusResponse.getErrorCode()).thenReturn(errorCode);

        // when
        boolean updateResponse = gigyaService.disableAccount(uid);

        // then
        Assert.assertFalse(updateResponse);
    }

    @Test
    public void sendVerificationEmail_whenGSResponseIsNotNull_returnResponseAsItIs() throws IOException {
        // given
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        String mockResponseText = "{\"statusCode\": 200,\"statusReason\": \"OK\"}";
        when(mockCdcResponse.getResponseText()).thenReturn(mockResponseText);
        when(gigyaApi.sendVerificationEmail(any())).thenReturn(mockCdcResponse);

        // when
        CDCResponseData response = gigyaService.sendVerificationEmail("test");

        // then
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK.value());
    }

    @Test
    public void sendVerificationEmail_whenGSResponseIsNull_returnResponseAsWithInternalServerErrorStatus() throws IOException {
        // given
        when(gigyaApi.sendVerificationEmail(any())).thenReturn(null);

        // when
        CDCResponseData response = gigyaService.sendVerificationEmail("test");

        // then
        Assert.assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void resetPasswordRequest_shouldRequestPasswordReset()
            throws CustomGigyaErrorException, LoginIdDoesNotExistException, GSKeyNotFoundException, JsonProcessingException {
        // given
        GSObject gsObject = new GSObject();
        gsObject.put("passwordResetToken","");
        gsObject.put("requirePasswordCheck", false);
        ArrayList<CDCAccount> mockAccount = new ArrayList<>();
        LoginIDs loginIDs = LoginIDs.builder().username("armvalidtest@mail.com").build();
        CDCAccount cdcAccount = CDCAccount.builder().loginIDs(loginIDs).build();
        mockAccount.add(cdcAccount);
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        CDCSearchResponse mockCdcSearchResponse = CDCSearchResponse.builder()
                .errorCode(0)
                .statusCode(0)
                .totalCount(0)
                .statusReason("mock")
                .results(mockAccount)
                .build();

        when(mockSearchResponse.getResponseText()).thenReturn(mapper.writeValueAsString(mockCdcSearchResponse));
        when(gigyaApi.search(any(),any(),any())).thenReturn(mockSearchResponse);
        when(gigyaApi.resetPassword(any())).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getErrorCode()).thenReturn(0);
        when(mockCdcResponse.getData()).thenReturn(gsObject);

        // when
        gigyaService.resetPasswordRequest("armvalidtest@mail.com");

        // then
        verify(gigyaApi).resetPassword(any());
    }

    @Test(expected = LoginIdDoesNotExistException.class)
    public void resetPasswordRequest_whenAnInValidUsername_throwLoginIdDoesNotExistException()
            throws CustomGigyaErrorException, LoginIdDoesNotExistException, GSKeyNotFoundException, JsonProcessingException {
        // given
        ArrayList<CDCAccount> mockAccount = new ArrayList<>();
        LoginIDs loginIDs = LoginIDs.builder().username("arminvalidtest@mail.com").build();
        CDCAccount cdcAccount = CDCAccount.builder().loginIDs(loginIDs).build();
        mockAccount.add(cdcAccount);
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        CDCSearchResponse mockCdcSearchResponse = CDCSearchResponse.builder()
                .errorCode(0)
                .statusCode(0)
                .totalCount(0)
                .statusReason("mock")
                .results(mockAccount)
                .build();

        when(mockSearchResponse.getResponseText()).thenReturn(mapper.writeValueAsString(mockCdcSearchResponse));
        when(gigyaApi.search(any(),any(),any())).thenReturn(mockSearchResponse);
        when(gigyaApi.resetPassword(any())).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getErrorCode()).thenReturn(GigyaCodes.LOGIN_ID_DOES_NOT_EXIST.getValue());

        // when
        gigyaService.resetPasswordRequest("arminvalidtest@mail.com");
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void resetPasswordRequest_whenAnUnhandledErrorOccurs_throwCustomGigyaErrorException()
            throws CustomGigyaErrorException, LoginIdDoesNotExistException, GSKeyNotFoundException, JsonProcessingException {
        // given
        ArrayList<CDCAccount> mockAccount = new ArrayList<>();
        LoginIDs loginIDs = LoginIDs.builder().username("arminvalidtest@mail.com").build();
        CDCAccount cdcAccount = CDCAccount.builder().loginIDs(loginIDs).build();
        mockAccount.add(cdcAccount);
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        CDCSearchResponse mockCdcSearchResponse = CDCSearchResponse.builder()
                .errorCode(0)
                .statusCode(0)
                .totalCount(0)
                .statusReason("mock")
                .results(mockAccount)
                .build();

        when(mockSearchResponse.getResponseText()).thenReturn(mapper.writeValueAsString(mockCdcSearchResponse));
        when(gigyaApi.search(any(),any(),any())).thenReturn(mockSearchResponse);
        when(gigyaApi.resetPassword(any())).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getErrorCode()).thenReturn(1);

        // when
        gigyaService.resetPasswordRequest("arminvalidtest@mail.com");
    }

    @Test(expected = LoginIdDoesNotExistException.class)
    public void resetPasswordRequest_whenNoResultsFromSearch_throwLoginIdDoesNotExistException() throws GSKeyNotFoundException, CustomGigyaErrorException, JsonProcessingException, LoginIdDoesNotExistException {
        // given
        ArrayList<CDCAccount> mockAccount = new ArrayList<>();
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        CDCSearchResponse mockCdcSearchResponse = CDCSearchResponse.builder()
                .errorCode(0)
                .statusCode(0)
                .totalCount(0)
                .statusReason("mock")
                .results(mockAccount)
                .build();

        when(mockSearchResponse.getResponseText()).thenReturn(mapper.writeValueAsString(mockCdcSearchResponse));
        when(gigyaApi.search(any(),any(),any())).thenReturn(mockSearchResponse);
        when(gigyaApi.resetPassword(any())).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getErrorCode()).thenReturn(1);

        // when
        gigyaService.resetPasswordRequest("arminvalidtest@mail.com");
    }

    @Test
    public void resetPasswordRequest_whenAUserNameLoginIDisReturned_searchWithUsername()
            throws CustomGigyaErrorException, LoginIdDoesNotExistException, GSKeyNotFoundException, JsonProcessingException {
        // given
        GSObject gsObject = new GSObject();
        gsObject.put("passwordResetToken","");
        gsObject.put("requirePasswordCheck", false);
        ArrayList<CDCAccount> mockAccount = new ArrayList<>();
        LoginIDs loginIDs = LoginIDs.builder().username("arminvalidtest@mail.com").build();
        CDCAccount cdcAccount = CDCAccount.builder().loginIDs(loginIDs).build();
        mockAccount.add(cdcAccount);
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        CDCSearchResponse mockCdcSearchResponse = CDCSearchResponse.builder()
                .errorCode(0)
                .statusCode(0)
                .totalCount(0)
                .statusReason("mock")
                .results(mockAccount)
                .build();

        when(mockSearchResponse.getResponseText()).thenReturn(mapper.writeValueAsString(mockCdcSearchResponse));
        when(gigyaApi.search(any(),any(),any())).thenReturn(mockSearchResponse);
        when(gigyaApi.resetPassword(any())).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getErrorCode()).thenReturn(0);
        when(mockCdcResponse.getData()).thenReturn(gsObject);

        GSObject requestParams = new GSObject();
        requestParams.put("loginID", "arminvalidtest@mail.com");
        requestParams.put("sendEmail", false);

        // when
        gigyaService.resetPasswordRequest("arminvalidtest@mail.com");

        // then
        verify(gigyaApi).resetPassword(refEq(requestParams));


    }


    @Test
    public void resetPasswordRequest_whenAVerifiedEmailisReturned_searchWithUsername()
            throws CustomGigyaErrorException, LoginIdDoesNotExistException, GSKeyNotFoundException, JsonProcessingException {
        // given
        GSObject gsObject = new GSObject();
        gsObject.put("passwordResetToken","");
        gsObject.put("requirePasswordCheck", false);
        ArrayList<CDCAccount> mockAccount = new ArrayList<>();
        String[] emails = {"blrVerEmail@mail.com"};
        LoginIDs loginIDs = LoginIDs.builder().username("arminvalidtest@mail.com").emails(emails).build();
        CDCAccount cdcAccount = CDCAccount.builder().loginIDs(loginIDs).build();
        mockAccount.add(cdcAccount);
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        CDCSearchResponse mockCdcSearchResponse = CDCSearchResponse.builder()
                .errorCode(0)
                .statusCode(0)
                .totalCount(0)
                .statusReason("mock")
                .results(mockAccount)
                .build();

        when(mockSearchResponse.getResponseText()).thenReturn(mapper.writeValueAsString(mockCdcSearchResponse));
        when(gigyaApi.search(any(),any(),any())).thenReturn(mockSearchResponse);
        when(gigyaApi.resetPassword(any())).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getErrorCode()).thenReturn(0);
        when(mockCdcResponse.getData()).thenReturn(gsObject);

        GSObject requestParams = new GSObject();
        requestParams.put("loginID", "blrVerEmail@mail.com");
        requestParams.put("sendEmail", false);

        // when
        gigyaService.resetPasswordRequest("arminvalidtest@mail.com");

        // then
        verify(gigyaApi).resetPassword(refEq(requestParams));


    }

    @Test
    public void resetPasswordRequest_whenAUnverifiedEmailisReturned_searchWithUsername()
            throws CustomGigyaErrorException, LoginIdDoesNotExistException, GSKeyNotFoundException, JsonProcessingException {
        // given
        GSObject gsObject = new GSObject();
        gsObject.put("passwordResetToken","");
        gsObject.put("requirePasswordCheck", false);
        ArrayList<CDCAccount> mockAccount = new ArrayList<>();
        String[] emails = {"blrUnVerEmail@mail.com"};
        LoginIDs loginIDs = LoginIDs.builder().username("arminvalidtest@mail.com").unverifiedEmails(emails).build();
        CDCAccount cdcAccount = CDCAccount.builder().loginIDs(loginIDs).build();
        mockAccount.add(cdcAccount);
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        CDCSearchResponse mockCdcSearchResponse = CDCSearchResponse.builder()
                .errorCode(0)
                .statusCode(0)
                .totalCount(0)
                .statusReason("mock")
                .results(mockAccount)
                .build();

        when(mockSearchResponse.getResponseText()).thenReturn(mapper.writeValueAsString(mockCdcSearchResponse));
        when(gigyaApi.search(any(),any(),any())).thenReturn(mockSearchResponse);
        when(gigyaApi.resetPassword(any())).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getErrorCode()).thenReturn(0);
        when(mockCdcResponse.getData()).thenReturn(gsObject);

        GSObject requestParams = new GSObject();
        requestParams.put("loginID", "blrUnVerEmail@mail.com");
        requestParams.put("sendEmail", false);

        // when
        gigyaService.resetPasswordRequest("arminvalidtest@mail.com");

        // then
        verify(gigyaApi).resetPassword(refEq(requestParams));


    }

    @Test
    public void resetPasswordSubmit_whenANotValidToken_returnResponseWithError() {
        // given
        GSResponse mockErrorCdcResponse = Mockito.mock(GSResponse.class);
        String token = "testerrortoken";
        String newPassword = "testPassword1";
        int errorResponseCode = 40016;
        ResetPasswordSubmit request = ResetPasswordSubmit.builder().resetPasswordToken(token).newPassword(newPassword).build();
        when(gigyaApi.resetPassword(any())).thenReturn(mockErrorCdcResponse);
        when(mockErrorCdcResponse.getErrorCode()).thenReturn(errorResponseCode);

        // when
        ResetPasswordResponse response = gigyaService.resetPasswordSubmit(request);

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

        when(gigyaApi.resetPassword(any())).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getErrorCode()).thenReturn(successResponseCode);

        // when
        ResetPasswordResponse response = gigyaService.resetPasswordSubmit(request);

        // then
        Assert.assertEquals(response.getResponseCode(),successResponseCode);
    }

    @Test
    public void getUsernameByEmail_whenNoResultsAreFound_returnEmptyUsername() throws Exception {
        // given
        GSObject jsonResponse = new GSObject("{\"callId\": \"8ba37e7693594a7da17a134e79dfb950\",\"errorCode\": 0,\"apiVersion\": 2,\"statusCode\": 200,\"statusReason\": \"OK\",\"time\": \"2020-08-26T18:13:18.021Z\",\"results\": [],\"objectsCount\": 0,\"totalCount\": 0}");
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        when(gigyaApi.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getErrorCode()).thenReturn(0);
        when(mockResponse.getData()).thenReturn(jsonResponse);

        // when
        String username = gigyaService.getUsernameByEmail("test");

        // then
        assertTrue(username.isEmpty());
    }

    @Test
    public void getUsernameByEmail_whenResultsAreFound_returnFirstyUsername() throws Exception {
        // given
        String testEmail = "this-is-a-test-88@mail.com";
        GSObject jsonResponse = new GSObject("{\"callId\": \"8ba37e7693594a7da17a134e79dfb950\",\"errorCode\": 0,\"apiVersion\": 2,\"statusCode\": 200,\"statusReason\": \"OK\",\"time\": \"2020-08-26T18:13:18.021Z\",\"results\": [{\"profile\": {\"username\":\"" + testEmail + "\"}}], \"objectsCount\": 0,\"totalCount\": 0}");
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        when(gigyaApi.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getErrorCode()).thenReturn(0);
        when(mockResponse.getData()).thenReturn(jsonResponse);

        // when
        String username = gigyaService.getUsernameByEmail("test");

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
        when(gigyaApi.search(any(),any(), any())).thenReturn(mockResponse);

        // when
        String username = gigyaService.getUsernameByEmail("test");

        // then
        assertTrue(username.isEmpty());
    }

    @Test
    public void isAvailableLoginId_ShouldReturnIsAvailableResponse() throws Exception {
        // given
        boolean mockResponse = false;
        String data = loginIdNotAvailable.toString();
        GSObject gsObject = new GSObject(data);
        GSResponse mockGSResponse = Mockito.mock(GSResponse.class);
        when(mockGSResponse.getErrorCode()).thenReturn(0);
        when(mockGSResponse.getData()).thenReturn(gsObject);
        when(gigyaApi.search(any(), any(), any())).thenReturn(mockGSResponse);

        // when
        boolean response = gigyaService.isAvailableLoginId("test");

        // then
        assertEquals(response, mockResponse);
    }

    @Test
    public void isAvailableLoginId_ShouldMakeCallToSecondaryDCIfFirstResponseIsTrue_ThenReturnIsAvailableResponse() throws Exception {
        // given
        String mainDCResponse = loginIdAvailable.toString();
        GSObject mainGsObject = new GSObject(mainDCResponse);
        GSResponse mainGsResponse = Mockito.mock(GSResponse.class);
        when(mainGsResponse.getErrorCode()).thenReturn(0);
        when(mainGsResponse.getData()).thenReturn(mainGsObject);

        boolean expectedResponse = true;
        String secondaryDCResponse = loginIdAvailable.toString();
        GSObject secondaryGsObject = new GSObject(secondaryDCResponse);
        GSResponse secondaryGsResponse = Mockito.mock(GSResponse.class);
        when(secondaryGsResponse.getErrorCode()).thenReturn(0);
        when(secondaryGsResponse.getData()).thenReturn(secondaryGsObject);
        
        when(gigyaApi.search(any(), any(), any())).thenReturn(mainGsResponse, secondaryGsResponse);

        try (MockedStatic<CDCUtils> cdcUtilsMock = Mockito.mockStatic(CDCUtils.class)) {
            cdcUtilsMock.when(() -> { CDCUtils.isSecondaryDCSupported(anyString()); }).thenReturn(true);

            // when
            boolean response = gigyaService.isAvailableLoginId("test");

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
        when(gigyaApi.search(any(), any(), any())).thenReturn(mockGSResponse);

        // when
        gigyaService.isAvailableLoginId("test");
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
        IdentityProviderResponse result = gigyaService.getIdPInformation(IDP_NAME);

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
        IdentityProviderResponse result = gigyaService.getIdPInformation(IDP_NAME);

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
        when(gigyaApi.getJWTPublicKey()).thenReturn(mockGSResponse);
        when(mockGSResponse.getData()).thenReturn(new GSObject(responseData));

        // when
        JWTPublicKey result = gigyaService.getJWTPublicKey();

        // then
        assertEquals(n, result.getN());
        assertEquals(e, result.getE());
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void getJWTPublicKey_shouldThrowAnError() throws Exception {
        // given
        int errorCode = 400404;
        GSResponse mockGSResponse = Mockito.mock(GSResponse.class);
        when(gigyaApi.getJWTPublicKey()).thenReturn(mockGSResponse);
        when(mockGSResponse.getErrorCode()).thenReturn(errorCode);

        // when
        gigyaService.getJWTPublicKey();
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void search_GivenTheresAResponseCodeDifferentThanZero_ThenItShouldThrowCustomGigyaErrorException() throws CustomGigyaErrorException, IOException {
        // given
        String query = "";
        String message = "Error";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        Mockito.when(gigyaApi.search(anyString(), any(), any())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(10040);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        // when
        gigyaService.search(query, AccountType.FULL_LITE, mainApiDomain);
    }

    @Test
    public void search_GivenTheresAValidResponse_ItShouldReturnTheSameNumberOfResultsAsCDCAccounts() throws CustomGigyaErrorException, IOException {
        // given
        String query = "";
        String uid = "18f6f06762725175ab3fa121d32d8992";
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{\"totalCount\": 1,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[{\"UID\":\"" + uid + "\",\"isRegistered\":true,\"profile\":{\"username\":\"armatest\",\"country\":\"MX\"}}]}";
        when(mockSearchResponse.getResponseText()).thenReturn(searchResponseJson);
        when(gigyaApi.search(anyString(), any(), any())).thenReturn(mockSearchResponse);

        // when
        CDCSearchResponse searchResponse = gigyaService.search(query, AccountType.FULL_LITE, mainApiDomain);
        List<CDCAccount> accounts = searchResponse.getResults();

        // then
        CDCAccount account = accounts.get(0);
        assertTrue(account.getUID().equals(uid));
        assertTrue(accounts.size() == 1);
    }

    @Test
    public void liteRegisterUser_GivenTheresAValidResponse_ItShouldReturnTheSameNumberOfResultsAsCDCAccounts() throws CustomGigyaErrorException, IOException, GSKeyNotFoundException {
        // given
        String uid = "9f6f2133e57144d787574d49c0b9908e";
        GSResponse cdcMockResponse = Mockito.mock(GSResponse.class);
        String liteRegResponse = "{\"callId\": \"5c62541d1ce341eba0faf1d14642c191\",\"UID\": \"" + uid + "\",\"apiVersion\": 2,\"statusReason\": \"OK\",\"errorCode\": 0,\"time\": \"2019-09-19T16:14:24.983Z\",\"statusCode\": 200}";
        when(cdcMockResponse.getResponseText()).thenReturn(liteRegResponse);
        when(gigyaApi.registerLiteAccount(anyString())).thenReturn(cdcMockResponse);

        // when
        CDCResponseData cdcResponse = gigyaService.registerLiteAccount("");

        // then
        assertEquals(uid, cdcResponse.getUID());
    }

    @Test
    public void liteRegisterUser_V3_GivenTheresAValidResponse_ItShouldReturnTheSameNumberOfResultsAsCDCAccounts() throws GSKeyNotFoundException, CustomGigyaErrorException, IOException, JSONException, ParseException {
        // given
        String uid = "9f6f2133e57144d787574d49c0b9908e";
        LiteAccountDTO liteAccountDTO = LiteAccountDTO.builder().build();
        GSResponse cdcMockResponse = Mockito.mock(GSResponse.class);
        when(cdcMockResponse.getResponseText()).thenReturn(AccountUtils.getLiteRegistrationResponseJsonString());
        when(gigyaApi.registerLiteAccount(any(LiteAccountDTO.class))).thenReturn(cdcMockResponse);

        // when
        CDCResponseData cdcResponse = gigyaService.registerLiteAccount(liteAccountDTO);

        // then
        assertEquals(uid, cdcResponse.getUID());
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void liteRegisterUser_V3_GivenTheresAnError_ItShouldThrowCustomGigyaErrorException() throws CustomGigyaErrorException, GSKeyNotFoundException, IOException, ParseException, JSONException {
        // given
        int errorCode = 400;
        LiteAccountDTO liteAccountDTO = LiteAccountDTO.builder().build();
        GSResponse cdcMockResponse = Mockito.mock(GSResponse.class);
        when(cdcMockResponse.getErrorCode()).thenReturn(errorCode);
        when(cdcMockResponse.getResponseText()).thenReturn(AccountUtils.getLiteRegistrationErrorJsonString());
        when(gigyaApi.registerLiteAccount(any(LiteAccountDTO.class))).thenReturn(cdcMockResponse);

        // when
        gigyaService.registerLiteAccount(liteAccountDTO);
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void liteRegisterUser_GivenTheresAnError_ItShouldThrowCustomGigyaErrorException() throws CustomGigyaErrorException, IOException, GSKeyNotFoundException {
        // given
        int errorCode = 400;
        GSResponse cdcMockResponse = Mockito.mock(GSResponse.class);
        String liteRegError = "{\"callId\": \"349272dd0ec242d89e2be84c6692d0d2\",\"apiVersion\": 2,\"statusReason\": \"Bad Request\",\"errorMessage\": \"Invalid parameter value\", \"errorCode\": " + errorCode + ", \"validationErrors\": [{\"fieldName\": \"profile.email\",\"errorCode\": 400006,\"message\": \"Unallowed value for field: email\"}], \"time\": \"2019-09-19T16:15:20.508Z\",\"errorDetails\": \"Schema validation failed\", \"statusCode\":" + errorCode + "}";
        when(cdcMockResponse.getErrorCode()).thenReturn(errorCode);
        when(cdcMockResponse.getResponseText()).thenReturn(liteRegError);
        when(gigyaApi.registerLiteAccount(anyString())).thenReturn(cdcMockResponse);

        // when
        gigyaService.registerLiteAccount("");
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
        when(gigyaApi.changePassword(anyString(), anyString(), anyString())).thenReturn(cdcMockResponse);

        // when
        gigyaService.changePassword(uid, newPassword, oldPassword);
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
        when(gigyaApi.getRP(clientId)).thenReturn(mockGSResponse);

        //when
        OpenIdRelyingParty result = gigyaService.getRP(clientId);

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
        when(gigyaApi.getRP(clientId)).thenReturn(mockGSResponse);

        //when
        gigyaService.getRP(clientId);
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void updateRequirePasswordCheck_givenTheresAnError_ThenCustomGigyaErrorExceptionShouldBeThrown() throws CustomGigyaErrorException {
        // given
        String uid = Long.toString(1L);
        int errorCode = 400;
        GSResponse cdcMockResponse = Mockito.mock(GSResponse.class);
        when(cdcMockResponse.getErrorCode()).thenReturn(errorCode);
        when(gigyaApi.updateRequirePasswordCheck(anyString())).thenReturn(cdcMockResponse);

        // when
        gigyaService.updateRequirePasswordCheck(uid);
    }

    @Test
    public void updateRequirePasswordCheck_ShouldUpdateRequirePasswordCheck() throws CustomGigyaErrorException, JsonParseException {
        // given
        String uid = "9f6f2133e57144d787574d49c0b9908e";
        GSResponse cdcMockResponse = Mockito.mock(GSResponse.class);
        String requirePasswordCheckResponse = "{\"callId\": \"5c62541d1ce341eba0faf1d14642c191\",\"UID\": \"" + uid + "\",\"apiVersion\": 2,\"statusReason\": \"OK\",\"errorCode\": 0,\"time\": \"2019-09-19T16:14:24.983Z\",\"statusCode\": 200}";
        when(cdcMockResponse.getResponseText()).thenReturn(requirePasswordCheckResponse);
        when(gigyaApi.updateRequirePasswordCheck(anyString())).thenReturn(cdcMockResponse);

        // when
        gigyaService.updateRequirePasswordCheck(uid);

        // then
        verify(gigyaApi).updateRequirePasswordCheck(uid);
    }

    @Test
    public void setAccountInfo_ShouldSetCDCAccountData() throws CustomGigyaErrorException {
        // given
        CDCAccount cdcAccount = CDCAccount.builder().build();
        GSResponse gsResponseMock = mock(GSResponse.class);
        when(gigyaApi.setAccountInfo(any(CDCAccount.class))).thenReturn(gsResponseMock);

        // when
        gigyaService.setAccountInfo(cdcAccount);

        // then
        verify(gigyaApi).setAccountInfo(cdcAccount);
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void setAccountInfo_GivenTheresAnErrorFromCDC_ShouldThrowCustomGigyaErrorException() throws CustomGigyaErrorException {
        // given
        CDCAccount cdcAccount = CDCAccount.builder().build();
        GSResponse gsResponseMock = mock(GSResponse.class);
        when(gsResponseMock.getErrorCode()).thenReturn(400001);
        when(gigyaApi.setAccountInfo(any(CDCAccount.class))).thenReturn(gsResponseMock);

        // when
        gigyaService.setAccountInfo(cdcAccount);
    }

    @Test
    public void setAccountInfo_WhenParamsArePassed_ShouldSetCDCAccountData() throws CustomGigyaErrorException, GSKeyNotFoundException {
        // given
        String mockValue = "GeneralKenobi";
        Map<String, String> params = new HashMap<>();
        params.put("HelloThere", mockValue);
        GSResponse gsResponseMock = mock(GSResponse.class);
        when(gigyaApi.setAccountInfo(any(GSObject.class))).thenReturn(gsResponseMock);

        // when
        gigyaService.setAccountInfo(params);

        // then
        verify(gigyaApi).setAccountInfo(gsObjectCaptor.capture());
        GSObject gsObject = gsObjectCaptor.getValue();
        assertEquals(gsObject.get("HelloThere"), mockValue);
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void setAccountInfo_WhenParamsArePassed_AndTheresAnError_ShouldThrowCustomGigyaErrorException() throws CustomGigyaErrorException {
        // given
        Map<String, String> params = new HashMap<>();
        GSResponse gsResponseMock = mock(GSResponse.class);
        when(gsResponseMock.getErrorCode()).thenReturn(400001);
        when(gigyaApi.setAccountInfo(any(GSObject.class))).thenReturn(gsResponseMock);

        // when
        gigyaService.setAccountInfo(params);
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
        when(gigyaApi.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getResponseText()).thenReturn(searchResponse);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        String email = gigyaService.getEmailByUsername("test");

        // then
        assertTrue(email.isEmpty());
    }

    @Test
    public void getEmailByUsername_givenAValidUsername_whenMethodIsCalledAndResultContainsAVerifiedEmail_thenReturnTheFirstVerifiedEmail() throws Exception {
        // given
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String emailMock = "test@mail.com";
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 200, \"statusReason\": \"OK\", \"results\": [{ \"UID\": \"c1c691f4-556b-4ad1-ab75-841fc4e94dcd\", \"isRegistered\": true, \"profile\": { \"username\": \"armatest\", \"country\": \"MX\" }, \"emails\": { \"verified\": [\"" + emailMock + "\", \"test-two@mail.com\"] } }] }";
        when(gigyaApi.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        String email = gigyaService.getEmailByUsername("test");

        // then
        assertEquals(email, emailMock);
    }

    @Test
    public void getEmailByUsername_givenAValidUsername_whenMethodIsCalledAndResultContainsAnUnverifiedEmail_thenReturnTheFirstUnverifiedEmail() throws Exception {
        // given
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String emailMock = "test@mail.com";
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 200, \"statusReason\": \"OK\", \"results\": [{ \"UID\": \"c1c691f4-556b-4ad1-ab75-841fc4e94dcd\", \"isRegistered\": true, \"profile\": { \"username\": \"armatest\", \"country\": \"MX\" }, \"emails\": { \"verified\": [], \"unverified\": [\"" + emailMock + "\", \"test-two@mail.com\"] } }] }";
        when(gigyaApi.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        String email = gigyaService.getEmailByUsername("test");

        // then
        assertEquals(email, emailMock);
    }

    @Test
    public void getEmailByUsername_givenAValidUsername_whenMethodIsCalledAndTheUserDoesntHaveEmails_thenReturnAnEmptyString() throws Exception {
        // given
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 200, \"statusReason\": \"OK\", \"results\": [{ \"UID\": \"c1c691f4-556b-4ad1-ab75-841fc4e94dcd\", \"isRegistered\": true, \"profile\": { \"username\": \"armatest\", \"country\": \"MX\" }, \"emails\": { \"verified\": [], \"unverified\": [] } }] }";
        when(gigyaApi.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        String email = gigyaService.getEmailByUsername("test");

        // then
        assertEquals("", email);
    }

    @Test
    public void getUIDByEmail_givenAValidEmail_whenMethodIsCalled_thenShouldReturnAValidUID() throws Exception {
        // given
        String uidMock = "c1c691f4-556b-4ad1-ab75-841fc4e94dcd";
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 200, \"statusReason\": \"OK\", \"results\": [{ \"UID\": \"c1c691f4-556b-4ad1-ab75-841fc4e94dcd\", \"isRegistered\": true, \"profile\": { \"username\": \"armatest\", \"country\": \"MX\" }, \"emails\": { \"verified\": [], \"unverified\": [] } }] }";
        when(gigyaApi.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        String uid = gigyaService.getUIDByEmail("email@mail.com");

        // then
        assertEquals(uidMock, uid);
    }

    @Test
    public void getUIDByEmail_givenAnInvalidEmail_whenMethodIsCalled_thenShouldReturnAnEmptyString() throws Exception {
        // given
        String uidMock = "";
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 200, \"statusReason\": \"OK\", \"results\": [] }";
        when(gigyaApi.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        String uid = gigyaService.getUIDByEmail("email@mail.com");

        // then
        assertEquals(uidMock, uid);
    }

    @Test
    public void getAccountInfoByEmail_givenAnEmailThatDoesntExist_whenMethodIsCalled_thenShouldReturnNull() throws Exception {
        // given
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 200, \"statusReason\": \"OK\", \"results\": [{ \"UID\": \"\", \"isRegistered\": true, \"profile\": { \"username\": \"armatest\", \"country\": \"MX\" }, \"emails\": { \"verified\": [], \"unverified\": [] } }] }";
        when(gigyaApi.search(any(),any(), any())).thenReturn(mockResponse);
        when(gigyaApi.getAccount(any())).thenReturn(mockResponse);
        when(mockResponse.getData()).thenReturn(new GSObject(obj));
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        AccountInfo accountInfo = gigyaService.getAccountInfoByEmail("email@mail.com");

        // then
        assertNull(accountInfo);
    }

    @Test
    public void getAccountInfoByEmail_givenAnInvalidEmail_whenMethodIsCalled_thenShouldReturnAccountInfoAsNull() throws Exception {
        // given
        ReflectionTestUtils.setField(gigyaService, "isNewMarketingConsentEnabled", false);
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 404, \"statusReason\": \"NOT_FOUND\", \"results\": [{ \"UID\": \"\", \"isRegistered\": true, \"profile\": { \"username\": \"armatest\", \"country\": \"MX\" }, \"emails\": { \"verified\": [], \"unverified\": [] } }] }";
        when(gigyaApi.search(any(),any(), any())).thenReturn(mockResponse);
        when(gigyaApi.getAccount(any())).thenReturn(mockResponse);
        when(mockResponse.getData()).thenReturn(new GSObject(obj));
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        AccountInfo accountInfo = gigyaService.getAccountInfoByEmail("email@mail.com");

        // then
        assertNull(accountInfo);
    }

    @Test
    public void getAccountInfoByEmail_givenAnInvalidEmail_whenMethodIsCalled_thenShouldReturnAccountInfoAsNull_V2() throws Exception {
        // given
        ReflectionTestUtils.setField(gigyaService, "isNewMarketingConsentEnabled", true);
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 401, \"statusReason\": \"NOT_FOUND\", \"results\": [{ \"UID\": \"\", \"isRegistered\": true, \"profile\": { \"username\": \"armatest\", \"country\": \"MX\" }, \"emails\": { \"verified\": [], \"unverified\": [] } }] }";
        when(gigyaApi.search(any(),any(), any())).thenReturn(mockResponse);
        when(gigyaApi.getAccountV2(any())).thenReturn(mockResponse);
        when(mockResponse.getData()).thenReturn(new GSObject(objV2));
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        AccountInfo accountInfo = gigyaService.getAccountInfoByEmail("email@mail.com");

        // then
        assertNull(accountInfo);
    }

    @Test
    public void getAccountInfoByEmail_givenAValidEmail_whenMethodIsCalled_thenShouldReturnAccountInfol_V2() throws Exception {
        // given
        ReflectionTestUtils.setField(gigyaService, "isNewMarketingConsentEnabled", true);
        GSResponse mockResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{ \"totalCount\": 1, \"statusCode\": 200, \"statusReason\": \"OK\", \"results\": [{ \"UID\": \"10293847\", \"isRegistered\": true, \"profile\": { \"username\": \"armatest\", \"country\": \"MX\" }, \"emails\": { \"verified\": [], \"unverified\": [] } }] }";
        when(gigyaApi.search(any(),any(), any())).thenReturn(mockResponse);
        when(mockResponse.getResponseText()).thenReturn(searchResponseJson);

        when(gigyaApi.getAccountV2(any())).thenReturn(mockResponse);

        when(mockResponse.getData()).thenReturn(new GSObject(objV2));
        when(mockResponse.getErrorCode()).thenReturn(0);

        // when
        AccountInfo accountInfo = gigyaService.getAccountInfoByEmail("email@mail.com");

        // then
        assertEquals(accountInfo.getUid(), "10293847");
        assertEquals(accountInfo.getUsername(), "arm@test.com");
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void searchInBothDC_GivenTheresAResponseCodeDifferentThanZero_ThenItShouldThrowCustomGigyaErrorException() throws CustomGigyaErrorException, IOException {
        // given
        String username = "armatest@test.com";
        String message = "Error";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        Mockito.when(gigyaApi.search(anyString(), any(), any())).thenReturn(mockCdcResponse);
        Mockito.when(mockCdcResponse.getErrorCode()).thenReturn(10040);
        Mockito.when(mockCdcResponse.getErrorMessage()).thenReturn(message);

        // when
        gigyaService.searchInBothDC(username);
    }

    @Test
    public void searchInBothDC_GivenTheEmailExistsInTheMainApiDomain_ItShouldReturnTheAccountFound() throws CustomGigyaErrorException, IOException {
        // given
        setProperties();
        String username = "armatest@test.com";
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String searchResponseJson = "{\"totalCount\": 1,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[{\"UID\":\"" + uid + "\",\"isRegistered\":true,\"profile\":{\"username\":\"" + username + "\",\"country\":\"MX\"}}]}";
        when(mockSearchResponse.getResponseText()).thenReturn(searchResponseJson);
        when(gigyaApi.search(anyString(), any(), any())).thenReturn(mockSearchResponse);

        try (MockedStatic<CDCUtils> cdcUtilsMock = Mockito.mockStatic(CDCUtils.class)) {
            cdcUtilsMock.when(() -> CDCUtils.isSecondaryDCSupported(any())).thenReturn(true);

            // when
            SearchResponse searchResponse = gigyaService.searchInBothDC(username);
            List<CDCAccount> accounts = searchResponse.getCdcSearchResponse().getResults();

            // then
            CDCAccount account = accounts.get(0);
            assertEquals(mainDataCenter, searchResponse.getDataCenter().getValue());
            assertEquals(uid, account.getUID());
            assertEquals(1, accounts.size());
        }
    }

    @Test
    public void searchInBothDC_GivenTheUserNameDoesNotExistInTheMainAPIDomain_ItShouldSearchInTheSecondaryDomain_AndReturnTheAccountFound() throws CustomGigyaErrorException, IOException {
        // given
        setProperties();
        String username = "armatest@test.com";
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String mainDCSearchResponseJson = "{\"totalCount\": 0,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[]}";
        String secondaryDCResponseJson = "{\"totalCount\": 1,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[{\"UID\":\"" + uid + "\",\"isRegistered\":true,\"profile\":{\"username\":\"" + username + "\",\"country\":\"MX\"}}]}";

        when(mockSearchResponse.getResponseText()).thenReturn(mainDCSearchResponseJson, secondaryDCResponseJson);
        when(gigyaApi.search(anyString(), any(), any())).thenReturn(mockSearchResponse);


        try (MockedStatic<CDCUtils> cdcUtilsMock = Mockito.mockStatic(CDCUtils.class)) {
            cdcUtilsMock.when(() -> CDCUtils.isSecondaryDCSupported(any())).thenReturn(true);

            // when
            SearchResponse searchResponse = gigyaService.searchInBothDC(username);
            List<CDCAccount> accounts = searchResponse.getCdcSearchResponse().getResults();

            // then
            CDCAccount account = accounts.get(0);
            assertEquals(secondaryDataCenter, searchResponse.getDataCenter().getValue());
            assertEquals(uid, account.getUID());
            assertEquals(1, accounts.size());
        }
    }

    @Test
    public void searchInBothDC_GivenTheAccountDoesNotExistInAnyDataCenter_ItShouldReturnAResponseWithZeroAccounts() throws CustomGigyaErrorException, IOException {
        // given
        setProperties();
        String username = "armatest@test.com";
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String mainDCSearchResponseJson = "{\"totalCount\": 0,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[]}";
        String secondaryDCResponseJson = "{\"totalCount\": 0,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[]}";

        when(mockSearchResponse.getResponseText()).thenReturn(mainDCSearchResponseJson, secondaryDCResponseJson);
        when(gigyaApi.search(anyString(), any(), any())).thenReturn(mockSearchResponse);


        try (MockedStatic<CDCUtils> cdcUtilsMock = Mockito.mockStatic(CDCUtils.class)) {
            cdcUtilsMock.when(() -> CDCUtils.isSecondaryDCSupported(any())).thenReturn(true);

            // when
            SearchResponse searchResponse = gigyaService.searchInBothDC(username);
            List<CDCAccount> accounts = searchResponse.getCdcSearchResponse().getResults();

            // then
            assertEquals(0, accounts.size());
            assertNull(searchResponse.getDataCenter());
        }
    }

    @Test
    public void searchInBothDC_GivenTheSecondaryDataCenterIsNotSupported_ThenItShouldOnlyCheckTheMainDataCenter() throws CustomGigyaErrorException, IOException {
        // given
        setProperties();
        String username = "armatest@test.com";
        GSResponse mockSearchResponse = Mockito.mock(GSResponse.class);
        String mainDCSearchResponseJson = "{\"totalCount\": 0,\"errorCode\":0,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[]}";
        String secondaryDCResponseJson = "{\"totalCount\": 0,\"errorCode\":0,\"statusCode\":200,\"statusReason\":\"OK\",\"results\":[]}";

        when(mockSearchResponse.getResponseText()).thenReturn(mainDCSearchResponseJson, secondaryDCResponseJson);
        when(gigyaApi.search(anyString(), eq(AccountType.FULL_LITE), eq(mainApiDomain))).thenReturn(mockSearchResponse);
        when(gigyaApi.search(anyString(), eq(AccountType.FULL_LITE), eq(secondaryApiDomain))).thenCallRealMethod();


        try (MockedStatic<CDCUtils> cdcUtilsMock = Mockito.mockStatic(CDCUtils.class)) {
            cdcUtilsMock.when(() -> CDCUtils.isSecondaryDCSupported(any())).thenReturn(false);

            // when
            SearchResponse searchResponse = gigyaService.searchInBothDC(username);
            searchResponse.getCdcSearchResponse().getResults();

            // then
            verify(gigyaApi).search(anyString(), eq(AccountType.FULL_LITE), eq(mainApiDomain));
            verify(gigyaApi, times(0)).search(anyString(), eq(AccountType.FULL_LITE), eq(secondaryApiDomain));
        }
    }

    @Test
    public void register_shouldRegisterAnAccount() throws IOException {
        // given
        CDCNewAccount cdcNewAccount = CDCNewAccount.builder().build();
        String response = "{\"statusCode\": 200,\"statusReason\": \"OK\"}";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);

        when(gigyaApi.register(cdcNewAccount)).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getResponseText()).thenReturn(response);

        // when
        CDCResponseData cdcResponse = gigyaService.register(cdcNewAccount);

        // then
        verify(gigyaApi).register(any(CDCNewAccount.class));
        assertEquals(cdcResponse.getStatusCode(), 200);
    }

    @Test
    public void register_v2_shouldRegisterAnAccount() throws IOException {
        // given
        CDCNewAccountV2 cdcNewAccount = CDCNewAccountV2.builder().build();
        String response = "{\"statusCode\": 200,\"statusReason\": \"OK\"}";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);

        when(gigyaApi.register(cdcNewAccount)).thenReturn(mockCdcResponse);
        when(mockCdcResponse.getResponseText()).thenReturn(response);

        // when
        CDCResponseData cdcResponse = gigyaService.register(cdcNewAccount);

        // then
        verify(gigyaApi).register(any(CDCNewAccountV2.class));
        assertEquals(cdcResponse.getStatusCode(), 200);
    }

    @Test
    public void finalizeRegistration_ShouldFinalizeRegistrationInCDC() throws CustomGigyaErrorException {
        // given
        String regToken = "regTokenTest";
        GSResponse gsResponseMock = mock(GSResponse.class);
        when(gigyaApi.finalizeRegistration(anyString())).thenReturn(gsResponseMock);

        // when
        gigyaService.finalizeRegistration(regToken);

        // then
        verify(gigyaApi).finalizeRegistration(regToken);
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void finalizeRegistration_GivenTheresAnErrorFromCDC_ShouldThrowCustomGigyaErrorException() throws CustomGigyaErrorException {
        // given
        String regToken = "regTokenTest";
        GSResponse gsResponseMock = mock(GSResponse.class);
        when(gsResponseMock.getErrorCode()).thenReturn(400001);
        when(gigyaApi.finalizeRegistration(anyString())).thenReturn(gsResponseMock);

        // when
        gigyaService.finalizeRegistration(regToken);
    }
}
