package com.thermofisher.cdcam.services;
import static org.mockito.Mockito.*;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.builders.GSRequestFactory;
import com.thermofisher.cdcam.enums.cdc.AccountType;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.utils.AccountUtils;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class CDCAccountsServiceTests {

    @Value("${cdc.main.datacenter}")
    private String mainApiDomain;

    @InjectMocks
    CDCAccountsService cdcAccountsService;

    @Mock
    SecretsService secretsService;

    @Test
    public void setAccountInfo_ShouldSetAccountInfoSendRequest() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        CDCAccount account = AccountUtils.getCDCAccount(accountInfo);

        GSResponse gsResponseMock = mock(GSResponse.class);
        GSRequest gsRequestMock = mock(GSRequest.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestFactoryMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestFactoryMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            cdcAccountsService.setAccountInfo(account);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void changePassword_GivenThereIsAValidRequest_ThenGSRequestSendShouldBeCalled() throws JSONException {
        // given
        final String uid = "uid";
        final String newPassword = "newPassword";
        final String oldPassword = "oldPassword";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            cdcAccountsService.changePassword(uid, newPassword, oldPassword);

            verify(gsRequestMock).send();
        }
    }

    @Test
    public void getAccount_givenParametersToMakeGetAccountRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled() throws JSONException {
        // given
        final String uid = "uid";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            cdcAccountsService.getAccount(uid);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void getJWTPublicKey_givenParametersToMakeGetJWTPublicKeyRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled(){
        // given
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            cdcAccountsService.getJWTPublicKey();

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void setUserInfo_givenParametersToMakeSetUserInfoRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled(){
        // given
        String uid = "uid";
        String data = "data";
        String profile = "profile";
        String removeLoginEmails = "removeLoginEmails";
        String username = "username";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            cdcAccountsService.setUserInfo(uid, data, profile, removeLoginEmails, username);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void setUserInfo_givenParametersToMakeSetUserInfoRequest_whenMethodIsCalledAndRemoveLoginEmailsIsNull_thenMethodSendFromGSRequestShouldBeCalled(){
        // given
        String uid = "uid";
        String data = "data";
        String profile = "profile";
        String removeLoginEmails = null;
        String username = "username";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            cdcAccountsService.setUserInfo(uid, data, profile, removeLoginEmails, username);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void setUserInfo_givenParametersToMakeSetUserInfoRequest_whenMethodIsCalledAndUsernameIsNull_thenMethodSendFromGSRequestShouldBeCalled(){
        // given
        String uid = "uid";
        String data = "data";
        String profile = "profile";
        String removeLoginEmails = "removeLoginEmails";
        String username = null;
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            cdcAccountsService.setUserInfo(uid, data, profile, removeLoginEmails, username);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void changeAccountStatus_givenParametersToMakeChangeAccountStatusRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled(){
        // given
        String uid = "uid";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            cdcAccountsService.changeAccountStatus(uid, true);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void setLiteReg_givenParametersToMakeSetLiteRegRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled(){
        // given
        String email = "email";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            cdcAccountsService.setLiteReg(email);

            // then
            verify(gsRequestMock, times(2)).send();
        }
    }

    @Test
    public void search_givenParametersToMakeSearchRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled(){
        // given
        String query = "query";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            cdcAccountsService.search(query, AccountType.FULL, mainApiDomain);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void register_givenParametersToMakeRegisterRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled() throws JSONException {
        // given
        CDCNewAccount cdcNewAccount = CDCNewAccount.builder().build();
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            cdcAccountsService.register(cdcNewAccount);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void sendVerificationEmail_givenParametersToMakeSendVerificationEmailRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled() throws JSONException {
        // given
        String uid = "uid";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            cdcAccountsService.sendVerificationEmail(uid);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void isAvailableLoginId_givenParametersToMakeIsAvailableLoginIdRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled() throws JSONException {
        // given
        String loginId = "loginId";
        String apiDomain = "apiDomain";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            cdcAccountsService.isAvailableLoginId(loginId, apiDomain);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void resetPassword_givenParametersToMakeResetPasswordRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled(){
        // given
        GSObject gsObject = new GSObject();
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.createWithParams(any(), any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            cdcAccountsService.resetPassword(gsObject);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void getRP_givenParametersToMakeGetRPRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled(){
        // given
        String clientID = "clientID";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            cdcAccountsService.getRP(clientID);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void updateRequirePasswordCheck_givenParametersToMakeUpdateRequirePasswordCheckRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled(){
        // given
        String uid = "uid";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            cdcAccountsService.updateRequirePasswordCheck(uid);

            // then
            verify(gsRequestMock).send();
        }
    }
}
