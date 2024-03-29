package com.thermofisher.cdcam;

import com.google.gson.Gson;
import com.thermofisher.cdcam.controller.NotificationController;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.dto.EmailVerificationDTO;
import com.thermofisher.cdcam.model.dto.UpdateMarketingConsentDTO;
import com.thermofisher.cdcam.services.GigyaService;
import com.thermofisher.cdcam.services.NotificationService;
import com.thermofisher.cdcam.utils.AccountUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationControllerTests {

    @InjectMocks
    NotificationController notificationController;

    @Mock
    GigyaService gigyaService;

    @Mock
    NotificationService notificationService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void sendEmailVerificationSNS_GivenAValidUid_WhenCallSNS_ThenShouldReturnOK() throws Exception {
        // given
        EmailVerificationDTO emailVerification = new Gson().fromJson("{ \"uid\": \"496264a07789452b8fb331906bbf86ee\"}", EmailVerificationDTO.class);
        when(gigyaService.getAccountInfo(any())).thenReturn(AccountUtils.getSiteAccount());
        doNothing().when(notificationService).sendPublicAccountUpdatedNotification(any());
        doNothing().when(notificationService).sendPrivateAccountUpdatedNotification(any());

        // when
        ResponseEntity<String> resp = notificationController.sendEmailVerificationSNS(emailVerification);

        // then
        verify(notificationService).sendPublicAccountUpdatedNotification(any());
        verify(notificationService).sendPrivateAccountUpdatedNotification(any());
        assertEquals(resp.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void sendEmailVerificationSNS_GivenAValidUid_PreviousEmail_exists_WhenCallSNS_ThenShouldReturnOK() throws Exception {
        // given
        EmailVerificationDTO emailVerification = new EmailVerificationDTO();
        emailVerification.setUid(AccountUtils.getSiteAccount().getUid());
        emailVerification.setPreviousEmail(AccountUtils.getSiteAccount().getEmailAddress());

        when(gigyaService.getAccountInfo(any())).thenReturn(AccountUtils.getSiteAccount());
        doNothing().when(notificationService).sendPublicAccountUpdatedNotification(any());
        doNothing().when(notificationService).sendPrivateAccountUpdatedNotification(any());

        // when
        ResponseEntity<String> resp = notificationController.sendEmailVerificationSNS(emailVerification);

        // then
        verify(notificationService).sendPublicAccountUpdatedNotification(any());
        verify(notificationService).sendPrivateAccountUpdatedNotification(any());
        assertEquals(resp.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void sendEmailVerificationSNS_GivenAInvalidUid_WhenCallSNS_ThenShouldReturnABadRequest() throws CustomGigyaErrorException {
        // given
        EmailVerificationDTO emailVerification = new Gson().fromJson("{ \"uid\": \"496264a07789452b8fb331906bbf86ee\"}", EmailVerificationDTO.class);
        when(gigyaService.getAccountInfo(any())).thenThrow(CustomGigyaErrorException.class);
        // doNothing().when(notificationService).sendPublicAccountUpdatedNotification(any());
        // doNothing().when(notificationService).sendPrivateAccountUpdatedNotification(any());

        // when
        ResponseEntity<String> resp = notificationController.sendEmailVerificationSNS(emailVerification);

        // then
        assertEquals(resp.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void notifyMarketingConsentUpdated_GivenAValidUid_WhenCallSNS_ThenShouldReturnOK() throws Exception {
        // given
        UpdateMarketingConsentDTO marketingConsent = new Gson().fromJson("{ \"uid\": \"496264a07789452b8fb331906bbf86ee\"}", UpdateMarketingConsentDTO.class);
        when(gigyaService.getAccountInfo(any())).thenReturn(AccountUtils.getSiteAccount());
//        doNothing().when(notificationService).sendPublicAccountUpdatedNotification(any());
        //       doNothing().when(notificationService).sendPrivateAccountUpdatedNotification(any());

        // when
        ResponseEntity<String> resp = notificationController.notifyMarketingConsentUpdated(marketingConsent);

        // then
        verify(notificationService).sendPublicMarketingConsentUpdatedNotification(any());
        verify(notificationService).sendPrivateMarketingConsentUpdatedNotification(any());
        assertEquals(resp.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void notifyMarketingConsentUpdated_GivenAInvalidUid_WhenCallSNS_ThenShouldReturnANotFound() throws CustomGigyaErrorException {
        // given
        UpdateMarketingConsentDTO marketingConsent = new Gson().fromJson("{ \"uid\": \"496264a07789452b8fb331906bbf86ee\"}", UpdateMarketingConsentDTO.class);
        when(gigyaService.getAccountInfo(any())).thenThrow(new CustomGigyaErrorException("Unknown user", 403005));
        //  doNothing().when(notificationService).sendPublicMarketingConsentUpdatedNotification(any());
        //  doNothing().when(notificationService).sendPrivateMarketingConsentUpdatedNotification(any());

        // when
        ResponseEntity<String> resp = notificationController.notifyMarketingConsentUpdated(marketingConsent);

        // then
        assertEquals(resp.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void notifyMarketingConsentUpdated_GivenAInvalidUid_WhenCallSNS_ThenShouldReturnAnInternalServerError() throws CustomGigyaErrorException {
        // given
        UpdateMarketingConsentDTO marketingConsent = new Gson().fromJson("{ \"uid\": \"496264a07789452b8fb331906bbf86ee\"}", UpdateMarketingConsentDTO.class);
        when(gigyaService.getAccountInfo(any())).thenThrow(new CustomGigyaErrorException("Internal Server Error", 500000));
        //  doNothing().when(notificationService).sendPublicMarketingConsentUpdatedNotification(any());
        // doNothing().when(notificationService).sendPrivateMarketingConsentUpdatedNotification(any());

        // when
        ResponseEntity<String> resp = notificationController.notifyMarketingConsentUpdated(marketingConsent);

        // then
        assertEquals(resp.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
