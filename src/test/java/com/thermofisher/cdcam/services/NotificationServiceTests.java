package com.thermofisher.cdcam.services;

import static org.mockito.Mockito.*;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.EmailUpdatedNotification;
import com.thermofisher.cdcam.model.MarketingConsentUpdatedNotification;
import com.thermofisher.cdcam.model.dto.RequestResetPasswordDTO;
import com.thermofisher.cdcam.model.dto.UsernameRecoveryDTO;
import com.thermofisher.cdcam.model.dto.UsernameRecoveryUserInfoDTO;
import com.thermofisher.cdcam.model.notifications.AccountUpdatedNotification;
import com.thermofisher.cdcam.model.notifications.MergedAccountNotification;
import com.thermofisher.cdcam.model.notifications.PasswordUpdateNotification;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import com.thermofisher.cdcam.utils.AccountUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
@SpringBootTest//(classes = { NotificationService.class, SNSHandler.class })
public class NotificationServiceTests {

    @InjectMocks
    NotificationService notificationService;

    @Mock
    SNSHandler snsHandler;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void sendAccountRegisteredNotification_ShouldSendNotificationToRegistrationSNSTopicWithAccountData() throws JsonProcessingException {
        // given
        ReflectionTestUtils.setField(notificationService, "registrationSNSTopic", "registrationSNS");
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        notificationService.sendAccountRegisteredNotification(accountInfo, AccountUtils.cipdc);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString());
    }

    @Test(expected = NullPointerException.class)
    public void sendAccountRegisteredNotification_GivenAccountIsNull_ThenThrowNullPointerException() throws JsonProcessingException {
        // when
        notificationService.sendAccountRegisteredNotification(null, null);
    }

    @Test
    public void sendNotifyAccountInfoNotification_ShouldSendNotificationToAccountInfoSNSTopicWithAccountData() throws JsonProcessingException {
        // given
        ReflectionTestUtils.setField(notificationService, "accountInfoSNSTopic", "accountInfoTopicValue");
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        doNothing().when(snsHandler).sendNotification(anyString(), anyString(), any());

        // when
        notificationService.sendNotifyAccountInfoNotification(accountInfo, AccountUtils.cipdc);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString(), any());
    }

    @Test(expected = NullPointerException.class)
    public void sendNotifyAccountInfoNotification_GivenAccountIsNull_ThenThrowNullPointerException() throws JsonProcessingException {
        // when
        notificationService.sendNotifyAccountInfoNotification(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void sendAccountMergedNotification_GivenParameterIsNull_ThenThrowNullPointerException() {
        // when
        notificationService.sendAccountMergedNotification(null);
    }

    @Test
    public void sendAccountMergedNotification_ShouldSendNotificationToRegistrationSNSTopicWithMergedAccountNotification() {
        // given
        ReflectionTestUtils.setField(notificationService, "registrationSNSTopic", "registrationSNS");
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        MergedAccountNotification mergedAccountNotification = MergedAccountNotification.build(accountInfo);
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        notificationService.sendAccountMergedNotification(mergedAccountNotification);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString());
    }

    @Test(expected = NullPointerException.class)
    public void sendPublicAccountUpdatedNotification_GivenParameterIsNull_ThenThrowNullPointerException() throws JsonProcessingException {
        // when
        notificationService.sendPublicAccountUpdatedNotification(null);
    }

    @Test
    public void sendPublicAccountUpdatedNotification_GivenParameterAValidParameter_ThenShouldSend_AccountUpdatedNotification() throws JsonProcessingException {
        // given
        ReflectionTestUtils.setField(notificationService, "accountUpdatedSNSTopic", "accountUpdatedSNSTopic");
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        AccountUpdatedNotification accountUpdatedNotification = AccountUpdatedNotification.build(accountInfo);
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        notificationService.sendPublicAccountUpdatedNotification(accountUpdatedNotification);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString());
    }

    @Test
    public void sendPrivateAccountUpdatedNotification_ShouldSendNotificationToRegistrationSNSTopicWithMergedAccountNotification() throws IOException {
        // given
        ReflectionTestUtils.setField(notificationService, "registrationSNSTopic", "registrationSNS");
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        AccountUpdatedNotification accountUpdatedNotification = AccountUpdatedNotification.build(accountInfo);
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        notificationService.sendPrivateAccountUpdatedNotification(accountUpdatedNotification);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString());
    }

    @Test(expected = NullPointerException.class)
    public void sendPublicEmailUpdatedNotification_GivenParameterIsNull_ThenThrowNullPointerException() throws JsonProcessingException {
        // when
        notificationService.sendPublicEmailUpdatedNotification(null);
    }

    @Test
    public void sendPublicEmailUpdatedNotification_GivenParameterAValidParameter_ThenShouldSend_AccountUpdatedNotification() throws JsonProcessingException {
        // given
        ReflectionTestUtils.setField(notificationService, "accountUpdatedSNSTopic", "accountUpdatedSNSTopic");
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        EmailUpdatedNotification emailUpdatedNotification = EmailUpdatedNotification.build(accountInfo);
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        notificationService.sendPublicEmailUpdatedNotification(emailUpdatedNotification);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString());
    }

    @Test
    public void sendPrivateEmailUpdatedNotification_ShouldSendNotificationToRegistrationSNSTopicWithMergedAccountNotification() throws IOException {
        // given
        ReflectionTestUtils.setField(notificationService, "registrationSNSTopic", "registrationSNS");
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        EmailUpdatedNotification emailUpdatedNotification = EmailUpdatedNotification.build(accountInfo);
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        notificationService.sendPrivateEmailUpdatedNotification(emailUpdatedNotification);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString());
    }

    @Test(expected = NullPointerException.class)
    public void sendPublicMarketingConsentUpdatedNotification_GivenParameterIsNull_ThenThrowNullPointerException() {
        // when
        notificationService.sendPublicMarketingConsentUpdatedNotification(null);
    }

    @Test
    public void sendPublicMarketingConsentUpdatedNotification_GivenParameterAValidParameter_ThenShouldSend_AccountUpdatedNotification() throws JsonProcessingException {
        // given
        ReflectionTestUtils.setField(notificationService, "accountUpdatedSNSTopic", "accountUpdatedSNSTopic");
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        MarketingConsentUpdatedNotification marketingConsentUpdatedNotification = MarketingConsentUpdatedNotification.build(accountInfo);
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        notificationService.sendPublicMarketingConsentUpdatedNotification(marketingConsentUpdatedNotification);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString());
    }

    @Test
    public void sendPrivateMarketingConsentUpdatedNotification_ShouldSendNotificationToRegistrationSNSTopicWithMergedAccountNotification() throws IOException {
        // given
        ReflectionTestUtils.setField(notificationService, "registrationSNSTopic", "registrationSNS");
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        MarketingConsentUpdatedNotification marketingConsentUpdatedNotification = MarketingConsentUpdatedNotification.build(accountInfo);
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        notificationService.sendPrivateMarketingConsentUpdatedNotification(marketingConsentUpdatedNotification);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString());
    }

    @Test(expected = NullPointerException.class)
    public void sendAspireRegistrationNotification_GivenParameterIsNull_ThenThrowNullPointerException() throws JsonProcessingException {
        // when
        notificationService.sendAspireRegistrationNotification(null);
    }

    @Test
    public void sendAspireRegistrationNotification_ShouldPrepareAspirePayload_AndSendAspireNotification() throws IOException {
        // given
        ReflectionTestUtils.setField(notificationService, "aspireRegistrationSNSTopic", "aspireSNS");
        AccountInfo accountInfo = AccountUtils.getAspireAccount();
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        try (MockedStatic<AccountInfoHandler> accountInfoHandlerMock = Mockito.mockStatic(AccountInfoHandler.class)) {
            accountInfoHandlerMock.when(() -> AccountInfoHandler.prepareForAspireNotification(any(AccountInfo.class))).thenCallRealMethod();
            // when
            notificationService.sendAspireRegistrationNotification(accountInfo);
    
            // then
            accountInfoHandlerMock.verify(() -> AccountInfoHandler.prepareForAspireNotification(accountInfo));
            verify(snsHandler).sendNotification(anyString(), anyString());
        }
    }

    @Test(expected = NullPointerException.class)
    public void sendPasswordUpdateNotification_GivenParameterIsNull_ThenThrowNullPointerException() {
        // when
        notificationService.sendPasswordUpdateNotification(null);
    }

    @Test
    public void sendPasswordUpdateNotification_ShouldSendPasswordUpdateNotification() {
        // given
        ReflectionTestUtils.setField(notificationService, "passwordUpdateSNSTopic", "passwordUpdateSNS");
        PasswordUpdateNotification passwordUpdateNotification = PasswordUpdateNotification.builder().build();
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        notificationService.sendPasswordUpdateNotification(passwordUpdateNotification);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString());
    }

    @Test(expected = NullPointerException.class)
    public void sendRecoveryUsernameEmail_GivenANullAccountAndAValidUsernameRecoveryDTO_whenTheMethodIsCalled_ThenShouldThrowNullPointerException() {
        // given
        UsernameRecoveryDTO usernameRecoveryDTO = UsernameRecoveryDTO.builder()
                .locale("en_US")
                .userInfo(UsernameRecoveryUserInfoDTO.builder()
                        .email("test@test.com")
                        .redirectUrl("http://www.test.com")
                        .build()
                ).build();

        // when
        notificationService.sendRecoveryUsernameEmailNotification(usernameRecoveryDTO,null);
    }

    @Test(expected = NullPointerException.class)
    public void sendRecoveryUsernameEmail_GivenAValidAccountAndANullUsernameRecoveryDTO_whenTheMethodIsCalled_ThenShouldThrowNullPointerException() {
        // given
        AccountInfo account = AccountUtils.getSiteAccount();
        // when
        notificationService.sendRecoveryUsernameEmailNotification(null,account);
    }

    @Test(expected = NullPointerException.class)
    public void sendRequestResetPasswordEmailNotification_GivenAValidAccountAndANullRequestResetPasswordDTO_whenTheMethodIsCalled_ThenShouldThrowNullPointerException() {
        // given
        AccountInfo account = AccountUtils.getSiteAccount();
        // when
        notificationService.sendRequestResetPasswordEmailNotification(account, null);
    }

    @Test(expected = NullPointerException.class)
    public void sendRequestResetPasswordEmailNotification_GivenANullAccountAndAValidRequestResetPasswordDTO_whenTheMethodIsCalled_ThenShouldThrowNullPointerException() {
        // given
        RequestResetPasswordDTO requestResetPasswordDTO = RequestResetPasswordDTO.builder()
                .passwordToken("token")
                .authData("authData")
                .build();
        // when
        notificationService.sendRequestResetPasswordEmailNotification(null, requestResetPasswordDTO);
    }

    @Test(expected = NullPointerException.class)
    public void sendResetPasswordConfirmationEmailNotification_GivenANullAccount_whenTheMethodIsCalled_ThenShouldThrowNullPointerException() {
        // when
        notificationService.sendResetPasswordConfirmationEmailNotification(null);
    }



    @Test(expected = NullPointerException.class)
    public void sendConfirmationEmailNotification_GivenANullAccount_whenTheMethodIsCalled_ThenShouldThrowNullPointerException() {
        // when
        notificationService.sendConfirmationEmailNotification(null);
    }

    @Test
    public void sendResetPasswordConfirmationEmailNotification_GivenParameterAValidParameter_ThenShouldSend_ResetPasswordConfirmationEmailNotification(){
        // given
        ReflectionTestUtils.setField(notificationService, "redirectUrl", "redirectUrl");
        ReflectionTestUtils.setField(notificationService, "emailServiceSNSTopic", "emailServiceSNSTopic");
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        notificationService.sendResetPasswordConfirmationEmailNotification(accountInfo);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString());
    }

    @Test
    public void sendConfirmationEmailNotification_GivenParameterAValidParameter_ThenShouldSend_ConfirmationEmailNotification(){
        // given
        ReflectionTestUtils.setField(notificationService, "emailServiceSNSTopic", "emailServiceSNSTopic");
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        notificationService.sendConfirmationEmailNotification(accountInfo);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString());
    }

    @Test
    public void sendRecoveryUsernameEmailNotification_GivenParameterAValidParameters_ThenShouldSend_RecoveryUsernameEmailNotification(){
        // given
        ReflectionTestUtils.setField(notificationService, "emailServiceSNSTopic", "emailServiceSNSTopic");
        UsernameRecoveryDTO usernameRecoveryDTO = UsernameRecoveryDTO.builder()
                .locale("locale")
                .userInfo(UsernameRecoveryUserInfoDTO.builder().email("email").redirectUrl("redirect").build())
                .build();
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        notificationService.sendRecoveryUsernameEmailNotification(usernameRecoveryDTO, accountInfo);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString());
    }

    @Test
    public void sendRequestResetPasswordEmailNotification_GivenParameterAValidParameters_ThenShouldSend_RequestResetPasswordEmailNotification(){
        // given
        ReflectionTestUtils.setField(notificationService, "emailServiceSNSTopic", "emailServiceSNSTopic");
        RequestResetPasswordDTO requestResetPasswordDTO = RequestResetPasswordDTO.builder()
                .passwordToken("token")
                .authData("authData")
                .build();
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        notificationService.sendRequestResetPasswordEmailNotification(accountInfo, requestResetPasswordDTO);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString());
    }
}
