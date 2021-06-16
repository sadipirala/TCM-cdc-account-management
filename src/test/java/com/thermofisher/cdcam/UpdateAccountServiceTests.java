package com.thermofisher.cdcam;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.model.dto.MarketingConsentDTO;
import com.thermofisher.cdcam.model.dto.ProfileInfoDTO;
import com.thermofisher.cdcam.services.UpdateAccountService;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class UpdateAccountServiceTests {
    private final String uid = "1234567890";
    private final String timezone = "America/Tijuana";
    private final ProfileInfoDTO profileInfoDTO = ProfileInfoDTO.builder()
        .uid(uid)
        .firstName("firstName")
        .lastName("lastName")
        .email("email@test.com")
        .username("username")
        .marketingConsentDTO(
            MarketingConsentDTO.builder()
                .city("city")
                .company("company")
                .country("country")
                .consent(true)
                .build()
        ).build();
    
    @InjectMocks
    private UpdateAccountService updateAccountService;

    @Mock
    private CDCResponseHandler cdcAccountsService;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void updateTimezoneInCDC_GivenAValidUIDAndTimezone_ReturnOK() throws Exception {
        // setup
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("code", HttpStatus.OK.value());
        response.put("log", "");
        when(cdcAccountsService.update(any())).thenReturn(response);

        // execution
        HttpStatus updateResponse = updateAccountService.updateTimezoneInCDC(uid, timezone);

        // validation
        Assert.assertEquals(updateResponse, HttpStatus.OK);
    }

    @Test
    public void updateTimezoneInCDC_GivenAnInvalidUIDAndTimezone_ReturnInternalServerError() throws Exception {
        // setup
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("log", "");
        when(cdcAccountsService.update(any())).thenReturn(response);

        // execution
        HttpStatus updateResponse = updateAccountService.updateTimezoneInCDC(uid, timezone);

        // validation
        Assert.assertEquals(updateResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void updateProfile_GivenAValidProfileInfoDTO_WhenCallupdateProfile_ThenShouldReturnOK() throws Exception {
        // setup
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("code", HttpStatus.OK.value());
        response.put("log", "");
        when(cdcAccountsService.update(any())).thenReturn(response);

        // execution
        HttpStatus updateResponse = updateAccountService.updateProfile(profileInfoDTO);

        // validation
        Assert.assertEquals(updateResponse, HttpStatus.OK);
    }

    @Test
    public void updateProfile_GivenAnInvalidProfileInfoDTO_WhenCallupdateProfile_ThenShouldReturnBadRequest() throws Exception {
        // setup
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("code", HttpStatus.BAD_REQUEST.value());
        response.put("log", "");
        when(cdcAccountsService.update(any())).thenReturn(response);

        // execution
        HttpStatus updateResponse = updateAccountService.updateProfile(ProfileInfoDTO.builder().uid("").build());

        // validation
        Assert.assertEquals(updateResponse, HttpStatus.BAD_REQUEST);
    }
}
