package com.thermofisher.cdcam.services;

import com.gigya.socialize.GSKeyNotFoundException;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.builders.GSRequestFactory;
import com.thermofisher.cdcam.enums.cdc.AccountType;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccountV2;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.dto.LiteAccountDTO;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.cdc.CDCUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GigyaApiTests {

    @Value("${cdc.main.datacenter}")
    private String mainApiDomain;

    @InjectMocks
    GigyaApi gigyaApi;

    @Mock
    SecretsService secretsService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

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
            gigyaApi.setAccountInfo(account);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void setAccountInfo_ShouldSendSetAccountInfoRequest() throws JSONException {
        // given
        GSResponse gsResponseMock = mock(GSResponse.class);
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSObject gsObjectMock = mock(GSObject.class);
        doNothing().when(gsRequestMock).setParams(eq(gsObjectMock));
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestFactoryMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestFactoryMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.setAccountInfo(gsObjectMock);

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
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            gigyaApi.changePassword(uid, newPassword, oldPassword);

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
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.getAccount(uid);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void getAccount_givenParametersToMakeGetAccountRequest_whenNullPointerExceptionIsThrown_thenMethodSendFromGSRequestShouldNotBeCalled() {
        // given
        final String uid = "uid";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
//        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
//        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenThrow(new NullPointerException(""));

            // when
            gigyaApi.getAccount(uid);

            // then
            verify(gsRequestMock, times(0)).send();
        }
    }

    @Test
    public void getAccount_v2_givenParametersToMakeGetAccountRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled() {
        // given
        final String uid = "uid";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.getAccountV2(uid);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void getAccount_v2_givenParametersToMakeGetAccountRequest_whenNullPointerExceptionIsThrown_thenMethodSendFromGSRequestShouldNotBeCalled() {
        // given
        final String uid = "uid";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
//        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
//        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenThrow(new NullPointerException(""));

            // when
            gigyaApi.getAccountV2(uid);

            // then
            verify(gsRequestMock, times(0)).send();
        }
    }

    @Test
    public void getJWTPublicKey_givenParametersToMakeGetJWTPublicKeyRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled() {
        // given
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
//        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.getJWTPublicKey();

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void setUserInfo_givenParametersToMakeSetUserInfoRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled() {
        // given
        String uid = "uid";
        String data = "data";
        String profile = "profile";
        String removeLoginEmails = "removeLoginEmails";
        String username = "username";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.setUserInfo(uid, data, profile, removeLoginEmails, username);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void setUserInfo_givenParametersToMakeSetUserInfoRequest_whenMethodIsCalledAndRemoveLoginEmailsIsNull_thenMethodSendFromGSRequestShouldBeCalled() {
        // given
        String uid = "uid";
        String data = "data";
        String profile = "profile";
        String removeLoginEmails = null;
        String username = "username";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.setUserInfo(uid, data, profile, removeLoginEmails, username);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void setUserInfo_givenParametersToMakeSetUserInfoRequest_whenMethodIsCalledAndUsernameIsNull_thenMethodSendFromGSRequestShouldBeCalled() {
        // given
        String uid = "uid";
        String data = "data";
        String profile = "profile";
        String removeLoginEmails = "removeLoginEmails";
        String username = null;
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.setUserInfo(uid, data, profile, removeLoginEmails, username);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void changeAccountStatus_givenParametersToMakeChangeAccountStatusRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled() {
        // given
        String uid = "uid";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.changeAccountStatus(uid, true);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void changeAccountStatus_givenParametersToMakeChangeAccountStatusRequest_whenNullPointerExceptionIsThrown_thenMethodSendFromGSRequestShouldNotBeCalled() {
        // given
        String uid = "uid";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
//        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
//        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenThrow(NullPointerException.class);

            // when
            gigyaApi.changeAccountStatus(uid, true);

            // then
            verify(gsRequestMock, times(0)).send();
        }
    }

    @Test
    public void registerLiteAccount_ShouldMakeTwoRequests_AndOneShouldBeSentWithTheRegTokenAndProfileParams() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        String email = "ivan.quintana@thermofisher.com";
        String regToken = RandomStringUtils.random(10);

        GSObject data = mock(GSObject.class);
        data.put("regToken", regToken);
        GSResponse initRegResponse = mock(GSResponse.class);
        when(initRegResponse.getData()).thenReturn(data);

        GSResponse gsResponseMock = mock(GSResponse.class);
        GSRequest gsRequestMock = mock(GSRequest.class);
//        doNothing().when(gsRequestMock).setParam(eq("regToken"), eq(regToken));
//        doNothing().when(gsRequestMock).setParam(eq("profile"), eq(String.format("{\"email\":\"%s\"}", email)));
        when(gsRequestMock.send()).thenReturn(initRegResponse, gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.registerLiteAccount(email);

            // then
            verify(gsRequestMock, times(2)).send();
        }
    }

    @Test
    public void registerLiteAccount_V3_ShouldMakeTwoRequests_AndTheyShouldHaveRegTokenAndProfileAndDataParams() throws GSKeyNotFoundException, CustomGigyaErrorException, JSONException {
        // given
        LiteAccountDTO liteAccountDTO = LiteAccountDTO.builder()
                .email("john.doe@mail.com")
                .clientId("eZc3CGSFO2-phATVvTvL_4tf")
                .build();
        String regToken = RandomStringUtils.random(10);

        GSObject data = mock(GSObject.class);
        data.put("regToken", regToken);
        GSResponse initResponse = mock(GSResponse.class);
        when(initResponse.getData()).thenReturn(data);

        GSResponse gsResponseMock = mock(GSResponse.class);
        GSRequest gsRequestMock = mock(GSRequest.class);
//        doNothing().when(gsRequestMock).setParam((eq("regToken")), eq(regToken));
        when(gsRequestMock.send()).thenReturn(initResponse, gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.registerLiteAccount(liteAccountDTO);

            // then
            verify(gsRequestMock, times(2)).send();
        }
    }

    @Test
    public void registerLiteAccount_ShouldThrowCustomGigyaErrorException_WhenCDCReturnsAnError() throws GSKeyNotFoundException, CustomGigyaErrorException {
        // given
        String email = "ivan.quintana@thermofisher.com";
        String regToken = RandomStringUtils.random(10);

        GSObject data = mock(GSObject.class);
        data.put("regToken", regToken);
        GSResponse initRegResponse = mock(GSResponse.class);
//        when(initRegResponse.getData()).thenReturn(data);

        GSResponse gsResponseMock = mock(GSResponse.class);
        GSRequest gsRequestMock = mock(GSRequest.class);
//        doNothing().when(gsRequestMock).setParam(eq("regToken"), eq(regToken));
//        doNothing().when(gsRequestMock).setParam(eq("profile"), eq(String.format("{\"email\":\"%s\"}", email)));
//        when(gsRequestMock.send()).thenReturn(initRegResponse, gsResponseMock);

        try (MockedStatic<CDCUtils> cdcUtils = Mockito.mockStatic(CDCUtils.class)) {
            cdcUtils.when(() -> CDCUtils.isErrorResponse(any())).thenReturn(true);

            // when
            Assertions.assertThrows(CustomGigyaErrorException.class, () -> {
                gigyaApi.registerLiteAccount(email);
            });

            // then
            verify(gsRequestMock, times(0)).send();
        }
    }

    @Test
    public void registerLiteAccount_V3_ShouldThrowCustomGigyaError_WhenCDCReturnsAnError() throws GSKeyNotFoundException, CustomGigyaErrorException, JSONException {
        // given
        LiteAccountDTO liteAccountDTO = LiteAccountDTO.builder()
                .email("john.doe@mail.com")
                .clientId("eZc3CGSFO2-phATVvTvL_4tf")
                .build();
        String regToken = RandomStringUtils.random(10);

        GSObject data = mock(GSObject.class);
        data.put("regToken", regToken);
        GSResponse initRegResponse = mock(GSResponse.class);
//        when(initRegResponse.getData()).thenReturn(data);

        GSResponse gsResponseMock = mock(GSResponse.class);
        GSRequest gsRequestMock = mock(GSRequest.class);
//        doNothing().when(gsRequestMock).setParam(eq("regToken"), eq(regToken));
//        when(gsRequestMock.send()).thenReturn(initRegResponse, gsResponseMock);

        try (MockedStatic<CDCUtils> cdcUtils = Mockito.mockStatic(CDCUtils.class)) {
            cdcUtils.when(() -> CDCUtils.isErrorResponse(any())).thenReturn(true);

            // when
            Assertions.assertThrows(CustomGigyaErrorException.class, () -> {
                gigyaApi.registerLiteAccount(liteAccountDTO);
            });

            // then
            verify(gsRequestMock, times(0)).send();
        }
    }

    @Test
    public void search_givenParametersToMakeSearchRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled() {
        // given
        String query = "query";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.search(query, AccountType.FULL, mainApiDomain);

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
            gigyaApi.register(cdcNewAccount);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void register_shouldReturnNull_whenNullPointerExceptionIsThrown() {
        // given
        CDCNewAccount cdcNewAccount = CDCNewAccount.builder().build();
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
//        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
//        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenThrow(NullPointerException.class);

            // when
            GSResponse response = gigyaApi.register(cdcNewAccount);

            // then
            assertNull(response);
        }
    }

    @Test
    public void register_v2_givenParametersToMakeRegisterRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled() {
        // given
        CDCNewAccountV2 cdcNewAccount = CDCNewAccountV2.builder().build();
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.register(cdcNewAccount);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void register_v2_shouldReturnNull_whenNullPointerExceptionIsThrown() {
        // given
        CDCNewAccountV2 cdcNewAccount = CDCNewAccountV2.builder().build();
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
//        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
//        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenThrow(NullPointerException.class);

            // when
            GSResponse response = gigyaApi.register(cdcNewAccount);

            // then
            assertNull(response);
        }
    }

    @Test
    public void sendVerificationEmail_givenParametersToMakesendVerificationEmailRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled() throws JSONException {
        // given
        String uid = "uid";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.sendVerificationEmail(uid);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void sendVerificationEmail_shouldReturnNull_whenNullPointerExceptionIsThrown() {
        // given
        String uid = "uid";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
//        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
//        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenThrow(NullPointerException.class);

            // when
            GSResponse response = gigyaApi.sendVerificationEmail(uid);

            // then
            assertNull(response);
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
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.isAvailableLoginId(loginId, apiDomain);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void resetPassword_givenParametersToMakeResetPasswordRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled() {
        // given
        GSObject gsObject = new GSObject();
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
//        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.createWithParams(any(), any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.resetPassword(gsObject);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void resetPassword_shouldReturnNull_whenNullPointerExceptionIsThrown() {
        // given
        GSObject gsObject = new GSObject();
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
//        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
//        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenThrow(NullPointerException.class);

            // when
            GSResponse response = gigyaApi.resetPassword(gsObject);

            // then
            assertNull(response);
        }
    }

    @Test
    public void getRP_givenParametersToMakeGetRPRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled() {
        // given
        String clientID = "clientID";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.getRP(clientID);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void updateRequirePasswordCheck_givenParametersToMakeUpdateRequirePasswordCheckRequest_whenMethodIsCalled_thenMethodSendFromGSRequestShouldBeCalled() {
        // given
        String uid = "uid";
        GSRequest gsRequestMock = mock(GSRequest.class);
        GSResponse gsResponseMock = mock(GSResponse.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
//        doNothing().when(gsRequestMock).setAPIDomain(anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestStaticMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestStaticMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.updateRequirePasswordCheck(uid);

            // then
            verify(gsRequestMock).send();
        }
    }

    @Test
    public void setCredentials_shouldSetCredentialsForMainDataCenter() throws JSONException {
        // given
        ReflectionTestUtils.setField(gigyaApi, "env", "dev");

        // when
        gigyaApi.setCredentials();

        // then
        verify(secretsService).get(any());
    }

    @Test
    public void setCredentials_shouldSetCredentialsForSecondaryDataCenter() throws JSONException {
        // given
        ReflectionTestUtils.setField(gigyaApi, "env", "dev");

        try (MockedStatic<CDCUtils> cdcUtils = Mockito.mockStatic(CDCUtils.class)) {
            cdcUtils.when(() -> CDCUtils.isSecondaryDCSupported(any())).thenReturn(true);

            // when
            gigyaApi.setCredentials();

            // then
            verify(secretsService, times(2)).get(any());
        }
    }

    @Test
    public void setCredentials_shouldNotSetCredentials_whenEnvIsTest() throws JSONException {
        // given
        ReflectionTestUtils.setField(gigyaApi, "env", "test");

        try (MockedStatic<CDCUtils> cdcUtils = Mockito.mockStatic(CDCUtils.class)) {
            cdcUtils.when(() -> CDCUtils.isSecondaryDCSupported(any())).thenReturn(true);

            // when
            gigyaApi.setCredentials();

            // then
            verify(secretsService, times(0)).get(any());
        }
    }

    @Test
    public void setCredentials_shouldNotSetCredentials_whenEnvIsLocal() throws JSONException {
        // given
        ReflectionTestUtils.setField(gigyaApi, "env", "local");

        try (MockedStatic<CDCUtils> cdcUtils = Mockito.mockStatic(CDCUtils.class)) {
            cdcUtils.when(() -> CDCUtils.isSecondaryDCSupported(any())).thenReturn(true);

            // when
            gigyaApi.setCredentials();

            // then
            verify(secretsService, times(0)).get(any());
        }
    }

    @Test
    public void setCredentials_shouldThrowASONException() throws JSONException {
        // given
        ReflectionTestUtils.setField(gigyaApi, "env", "dev");
        when(secretsService.get(any())).thenThrow(JSONException.class);

        // when
        gigyaApi.setCredentials();

        // then
        verify(secretsService, times(1)).get(any());
    }

    @Test
    public void finalizeRegistration_ShouldFinalizeAccountRegistration() {
        // given
        String regToken = "regTokenTest";
        GSResponse gsResponseMock = mock(GSResponse.class);
        GSRequest gsRequestMock = mock(GSRequest.class);
        doNothing().when(gsRequestMock).setParam(anyString(), anyString());
        when(gsRequestMock.send()).thenReturn(gsResponseMock);

        try (MockedStatic<GSRequestFactory> gsRequestFactoryMock = Mockito.mockStatic(GSRequestFactory.class)) {
            gsRequestFactoryMock.when(() -> GSRequestFactory.create(any(), any(), any(), any())).thenReturn(gsRequestMock);

            // when
            gigyaApi.finalizeRegistration(regToken);

            // then
            verify(gsRequestMock).send();
        }
    }
}
