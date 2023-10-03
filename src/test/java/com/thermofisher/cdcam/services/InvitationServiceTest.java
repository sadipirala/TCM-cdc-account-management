package com.thermofisher.cdcam.services;

import com.thermofisher.CdcamApplication;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.thermofisher.cdcam.model.HttpServiceResponse;

@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
@SpringBootTest//(classes = CdcamApplication.class)
public class InvitationServiceTest {

    @InjectMocks
    InvitationService invitationService;

    @Mock
    HttpService httpService;
    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void updateInvitationCountry_givenCountryIsUpdatedCorrectly_returnStatus200() {
        // given
        JSONObject requestBody = new JSONObject();
        HttpServiceResponse httpResponse = HttpServiceResponse.builder().status(200).build();
        when(httpService.put(any(), any())).thenReturn(httpResponse);

        // when
        Integer response = invitationService.updateInvitationCountry(requestBody);

        // then
        assertTrue(response.equals(200));
    }

    @Test
    public void updateInvitationCountry_givenAnErrorOccursWhileUpdatingCountry_returnStatus400() {
        // given
        JSONObject requestBody = new JSONObject();
        HttpServiceResponse httpResponse = HttpServiceResponse.builder().status(400).build();
        when(httpService.put(any(), any())).thenReturn(httpResponse);

        // when
        Integer response = invitationService.updateInvitationCountry(requestBody);

        // then
        assertTrue(response.equals(400));
    }
}
