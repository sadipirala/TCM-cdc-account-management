package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.model.HttpServiceResponse;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InvitationServiceTest {

    @InjectMocks
    InvitationService invitationService;

    @Mock
    HttpService httpService;

    @BeforeEach
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
