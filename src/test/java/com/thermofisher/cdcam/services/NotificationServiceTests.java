package com.thermofisher.cdcam.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.notifications.AccountUpdatedNotification;
import com.thermofisher.cdcam.model.notifications.MergedAccountNotification;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import com.thermofisher.cdcam.utils.AccountUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { NotificationService.class, AccountInfoHandler.class, SNSHandler.class })
public class NotificationServiceTests {

    @InjectMocks
    NotificationService notificationService;

    @Mock
    AccountInfoHandler accountInfoHandler;

    @Mock
    SNSHandler snsHandler;

    @Test(expected = NullPointerException.class)
    public void sendAccountMergedNotification_GivenParameterIsNull_ThenThrowNullPointerException() throws JsonProcessingException {
        // when
        notificationService.sendAccountMergedNotification(null);
    }

    @Test
    public void sendAccountMergedNotification_ShouldSendNotificationToRegistrationSNSTopicWithMergedAccountNotification() throws IOException {
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
    public void sendAccountUpdatedNotification_GivenParameterIsNull_ThenThrowNullPointerException() throws JsonProcessingException {
        // when
        notificationService.sendAccountUpdatedNotification(null);
    }

    @Test
    public void sendAccountUpdatedNotification_ShouldSendNotificationToRegistrationSNSTopicWithMergedAccountNotification() throws IOException {
        // given
        ReflectionTestUtils.setField(notificationService, "registrationSNSTopic", "registrationSNS");
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        AccountUpdatedNotification accountUpdatedNotification = AccountUpdatedNotification.build(accountInfo);
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        notificationService.sendAccountUpdatedNotification(accountUpdatedNotification);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString());
    }

    @Test(expected = NullPointerException.class)
    public void sendAspireRegistrationNotification_GivenParameterIsNull_ThenThrowNullPointerException() throws JsonProcessingException {
        // when
        notificationService.sendAspireRegistrationNotification(null);
    }

    @Test
    public void sendAspireRegistrationNotification_IfGivenAccountInfoObject_ThenSendAspireNotification() throws IOException {
        // given
        ReflectionTestUtils.setField(notificationService, "aspireRegistrationSNSTopic", "aspireSNS");
        AccountInfo accountInfo = AccountUtils.getAspireAccount();
        when(accountInfoHandler.prepareForAspireNotification(any())).thenCallRealMethod();
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        notificationService.sendAspireRegistrationNotification(accountInfo);

        // then
        verify(snsHandler).sendNotification(anyString(), anyString());
    }
}
