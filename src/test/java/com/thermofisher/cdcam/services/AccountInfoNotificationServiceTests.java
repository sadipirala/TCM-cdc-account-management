package com.thermofisher.cdcam.services;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class AccountInfoNotificationServiceTests {

    @InjectMocks
    AccountInfoNotificationService accountInfoNotificationService;

    @Mock
    AccountInfoHandler accountInfoHandler;

    @Mock
    SNSHandler snsHandler;

    @Test
    public void sendAspireRegistrationSNS_IfGivenAccountInfoObject_ThenSendAspireNotification() throws IOException {
        //setup
        ReflectionTestUtils.setField(accountInfoNotificationService,"snsAspireRegistration","aspireSNS");
        AccountInfo accountInfo = AccountInfo.builder()
                .uid("55885")
                .username("test")
                .emailAddress("email")
                .firstName("first")
                .lastName("last")
                .password("1")
                .acceptsAspireTermsAndConditions(true)
                .isHealthcareProfessional(true)
                .isGovernmentEmployee(true)
                .isProhibitedFromAcceptingGifts(true)
                .acceptsAspireEnrollmentConsent(true)
                .build();

        String mockAccountToNotify = "Test Account";
        Mockito.when(accountInfoHandler.prepareForAspireNotification(any())).thenReturn(mockAccountToNotify);
        Mockito.when(snsHandler.sendSNSNotification(anyString(),anyString())).thenReturn(true);

        //execution
        accountInfoNotificationService.sendAspireRegistrationSNS(accountInfo);

        //validation
        Mockito.verify(snsHandler,atLeastOnce()).sendSNSNotification(any(), any());
    }
}
