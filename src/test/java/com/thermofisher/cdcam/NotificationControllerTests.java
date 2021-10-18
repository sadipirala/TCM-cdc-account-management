package com.thermofisher.cdcam;

import com.google.gson.Gson;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.controller.NotificationController;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.dto.EmailVerificationDTO;
import com.thermofisher.cdcam.services.NotificationService;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class NotificationControllerTests {

    @InjectMocks
    NotificationController notificationController;

    @Mock
    CDCResponseHandler cdcResponseHandler;

    @Mock
    NotificationService notificationService;

    @Test
    public void sendEmailVerificationSNS_GivenAValidUid_WhenCallSNS_ThenShouldReturnOK() throws Exception {
        // given
        EmailVerificationDTO emailVerification = new Gson().fromJson("{ \"uid\": \"496264a07789452b8fb331906bbf86ee\"}", EmailVerificationDTO.class);
        when(cdcResponseHandler.getAccountInfo(any())).thenReturn(AccountUtils.getSiteAccount());
        doNothing().when(notificationService).sendPublicAccountUpdatedNotification(any());
        doNothing().when(notificationService).sendPrivateAccountUpdatedNotification(any());

        // when
        ResponseEntity<String> resp = notificationController.sendEmailVerificationSNS(emailVerification);

        // then
        verify(notificationService).sendPublicAccountUpdatedNotification(any());
        verify(notificationService).sendPrivateAccountUpdatedNotification(any());
        assertEquals(resp.getStatusCode(),HttpStatus.OK);
    }

    @Test
    public void sendEmailVerificationSNS_GivenAInvalidUid_WhenCallSNS_ThenShouldReturnABadRequest() throws CustomGigyaErrorException {
        // given
        EmailVerificationDTO emailVerification = new Gson().fromJson("{ \"uid\": \"496264a07789452b8fb331906bbf86ee\"}", EmailVerificationDTO.class);
        when(cdcResponseHandler.getAccountInfo(any())).thenThrow(CustomGigyaErrorException.class);
        doNothing().when(notificationService).sendPublicAccountUpdatedNotification(any());
        doNothing().when(notificationService).sendPrivateAccountUpdatedNotification(any());

        // when
        ResponseEntity<String> resp = notificationController.sendEmailVerificationSNS(emailVerification);

        // then
        assertEquals(resp.getStatusCode(),HttpStatus.BAD_REQUEST);
    }
}
