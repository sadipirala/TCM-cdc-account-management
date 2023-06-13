package com.thermofisher.cdcam.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.enums.CountryCodes;
import com.thermofisher.cdcam.enums.cdc.GigyaCodes;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;

@SpringBootTest(classes = { CdcamApplication.class })
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class EmailVerificationServiceTests {

    @InjectMocks
    EmailVerificationService emailVerificationService;

    @Mock
    GigyaService gigyaService;

    /* @Test
    public void getDefaultVerifiedDate_GivenCountryIsCanada_ShouldReturnNull() {
        // given
        String country = CountryCodes.CANADA.getValue();

        // when
        String result = EmailVerificationService.getDefaultVerifiedDate(country);

        // expect
        assertNull(result);
    } */

    @Test
    public void getDefaultVerifiedDate_GivenCountryIsNotSupportedForEmailVerification_ShouldReturnDefaultVerifiedDate() {
        // given
        String DEFAULT_VERIFIED_DATE = "0001-01-01";
        String country = CountryCodes.JAPAN.getValue();

        // when
        String result = EmailVerificationService.getDefaultVerifiedDate(country);

        // expect
        assertEquals(DEFAULT_VERIFIED_DATE, result);
    }

    @Test
    public void isVerificationPending_GivenErrorCodeIsPendingRegistration_AndVerificationIsPending_ShouldReturnTrue() {
        // given
        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setErrorCode(GigyaCodes.ACCOUNT_PENDING_REGISTRATION.getValue());
        cdcResponseData.setErrorDetails("Missing required fields for registration: data.verifiedEmailDate");

        // when
        boolean result = EmailVerificationService.isVerificationPending(cdcResponseData);

        // expect
        assertTrue(result);
    }

    @Test
    public void isVerificationPending_GivenErrorCodeIsPendingRegistration_AndVerificationIsNotPending_ShouldReturnFalse() {
        // given
        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setErrorCode(GigyaCodes.ACCOUNT_PENDING_REGISTRATION.getValue());
        cdcResponseData.setErrorDetails("Missing required fields for registration: data.company");

        // when
        boolean result = EmailVerificationService.isVerificationPending(cdcResponseData);

        // expect
        assertFalse(result);
    }

    @Test
    public void isVerificationPending_GivenErrorCodeIsNotPendingRegistration_ShouldReturnFalse() {
        // given
        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setErrorCode(GigyaCodes.PENDING_CODE_VERIFICATION.getValue());
        cdcResponseData.setErrorDetails("Missing required fields for registration: data.verifiedEmailDate");

        // when
        boolean result = EmailVerificationService.isVerificationPending(cdcResponseData);

        // expect
        assertFalse(result);
    }
    
    @Test
    public void sendVerificationByLinkEmailSync_triggerVerificationEmailProcess_givenRequestIsSuccessful_whenTriggered_ReturnResponse() throws IOException {
        // given
        HttpStatus mockStatus = HttpStatus.OK;
        CDCResponseData mockResponse = mock(CDCResponseData.class);
        when(mockResponse.getStatusCode()).thenReturn(mockStatus.value());
        when(gigyaService.sendVerificationEmail(any())).thenReturn(mockResponse);

        // when
        CDCResponseData response = emailVerificationService.sendVerificationByLinkEmailSync("test");

        // then
        assertEquals(response.getStatusCode(), mockStatus.value());
    }

    @Test
    public void sendVerificationByLinkEmailSync_triggerVerificationEmailProcess_givenRequestIsNotSuccessful_whenTriggered_ReturnResponse() throws IOException {
        // given
        HttpStatus mockStatus = HttpStatus.BAD_REQUEST;
        CDCResponseData mockResponse = mock(CDCResponseData.class);
        when(mockResponse.getStatusCode()).thenReturn(mockStatus.value());
        when(gigyaService.sendVerificationEmail(any())).thenReturn(mockResponse);

        // when
        CDCResponseData response = emailVerificationService.sendVerificationByLinkEmailSync("test");

        // then
        assertEquals(response.getStatusCode(), mockStatus.value());
    }

    @Test
    public void sendVerificationByLinkEmailSync_triggerVerificationEmailProcess_givenExceptionOccurs_whenTriggered_ReturnInternalServerErrorResponse() throws IOException {
        // given
        when(gigyaService.sendVerificationEmail(any())).thenThrow(IOException.class);

        // when
        CDCResponseData response = emailVerificationService.sendVerificationByLinkEmailSync("test");

        // then
        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void sendVerificationByLinkEmail_triggerVerificationEmailProcess_whenTriggered_sendVerificationProcessShouldBeCalled() throws IOException {
        // given
        String uid = "abc123";
        HttpStatus mockStatus = HttpStatus.BAD_REQUEST;
        CDCResponseData mockResponse = mock(CDCResponseData.class);
        when(mockResponse.getStatusCode()).thenReturn(mockStatus.value());
        when(gigyaService.sendVerificationEmail(uid)).thenReturn(mockResponse);

        // when
        emailVerificationService.sendVerificationByLinkEmail(uid);

        // then
        verify(gigyaService, times(1)).sendVerificationEmail(uid);
    }
}
