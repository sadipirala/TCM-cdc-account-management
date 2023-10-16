package com.thermofisher.cdcam.services;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NotificationServiceTests {

    @InjectMocks
    NotificationService notificationService;

    @Mock
    SNSHandler snsHandler;

    @BeforeEach
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

    @Test
    public void sendAccountRegisteredNotification_GivenAccountIsNull_ThenThrowNullPointerException() throws JsonProcessingException {
        Assertions.assertThrows(NullPointerException.class, () -> {
            notificationService.sendAccountRegisteredNotification(null, null);
        });
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

    @Test
    public void sendNotifyAccountInfoNotification_GivenAccountIsNull_ThenThrowNullPointerException() throws JsonProcessingException {
        Assertions.assertThrows(NullPointerException.class, () -> {
            notificationService.sendNotifyAccountInfoNotification(null, null);
        });
    }

    @Test
    public void sendAccountMergedNotification_GivenParameterIsNull_ThenThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            notificationService.sendAccountMergedNotification(null);
        });
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

    @Test
    public void sendPublicAccountUpdatedNotification_GivenParameterIsNull_ThenThrowNullPointerException() throws JsonProcessingException {
        Assertions.assertThrows(NullPointerException.class, () -> {
            notificationService.sendPublicAccountUpdatedNotification(null);
        });
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

    @Test
    public void sendPublicEmailUpdatedNotification_GivenParameterIsNull_ThenThrowNullPointerException() throws JsonProcessingException {
        Assertions.assertThrows(NullPointerException.class, () -> {
            notificationService.sendPublicEmailUpdatedNotification(null);
        });
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

    @Test
    public void sendPublicMarketingConsentUpdatedNotification_GivenParameterIsNull_ThenThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            notificationService.sendPublicMarketingConsentUpdatedNotification(null);
        });
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

    @Test
    public void sendAspireRegistrationNotification_GivenParameterIsNull_ThenThrowNullPointerException() throws JsonProcessingException {
        Assertions.assertThrows(NullPointerException.class, () -> {
            notificationService.sendAspireRegistrationNotification(null);
        });
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

    @Test
    public void sendPasswordUpdateNotification_GivenParameterIsNull_ThenThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            notificationService.sendPasswordUpdateNotification(null);
        });
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

    @Test
    public void sendRecoveryUsernameEmail_GivenANullAccountAndAValidUsernameRecoveryDTO_whenTheMethodIsCalled_ThenShouldThrowNullPointerException() {
        // given
        UsernameRecoveryDTO usernameRecoveryDTO = UsernameRecoveryDTO.builder()
                .locale("en_US")
                .userInfo(UsernameRecoveryUserInfoDTO.builder()
                        .email("test@test.com")
                        .redirectUrl("http://www.test.com")
                        .build()
                ).build();

        Assertions.assertThrows(NullPointerException.class, () -> {
            notificationService.sendRecoveryUsernameEmailNotification(usernameRecoveryDTO, null);
        });
    }

    @Test
    public void sendRecoveryUsernameEmail_GivenAValidAccountAndANullUsernameRecoveryDTO_whenTheMethodIsCalled_ThenShouldThrowNullPointerException() {
        // given
        AccountInfo account = AccountUtils.getSiteAccount();
        Assertions.assertThrows(NullPointerException.class, () -> {
            notificationService.sendRecoveryUsernameEmailNotification(null, account);
        });
    }

    @Test
    public void sendRequestResetPasswordEmailNotification_GivenAValidAccountAndANullRequestResetPasswordDTO_whenTheMethodIsCalled_ThenShouldThrowNullPointerException() {
        // given
        AccountInfo account = AccountUtils.getSiteAccount();
        Assertions.assertThrows(NullPointerException.class, () -> {
        notificationService.sendRequestResetPasswordEmailNotification(account, null);
        });
    }

    @Test
    public void sendRequestResetPasswordEmailNotification_GivenANullAccountAndAValidRequestResetPasswordDTO_whenTheMethodIsCalled_ThenShouldThrowNullPointerException() {
        // given
        RequestResetPasswordDTO requestResetPasswordDTO = RequestResetPasswordDTO.builder()
                .passwordToken("token")
                .authData("authData")
                .build();
        Assertions.assertThrows(NullPointerException.class, () -> {
            notificationService.sendRequestResetPasswordEmailNotification(null, requestResetPasswordDTO);
        });
    }

    @Test
    public void sendResetPasswordConfirmationEmailNotification_GivenANullAccount_whenTheMethodIsCalled_ThenShouldThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            notificationService.sendResetPasswordConfirmationEmailNotification(null);
        });
    }



    @Test
    public void sendConfirmationEmailNotification_GivenANullAccount_whenTheMethodIsCalled_ThenShouldThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            notificationService.sendConfirmationEmailNotification(null);
        });
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
