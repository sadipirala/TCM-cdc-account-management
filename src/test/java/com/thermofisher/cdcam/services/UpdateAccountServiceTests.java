package com.thermofisher.cdcam.services;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.cdcam.model.dto.MarketingConsentDTO;
import com.thermofisher.cdcam.model.dto.ProfileInfoDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateAccountServiceTests {
    private final String uid = "1234567890";
    private final String timezone = "America/Tijuana";
    private final ProfileInfoDTO profileInfoDTO = ProfileInfoDTO.builder()
        .uid(uid)
        .firstName("firstName")
        .lastName("lastName")
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
    private GigyaService gigyaService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void updateTimezoneInCDC_GivenAValidUIDAndTimezone_ReturnOK() throws Exception {
        // given
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("code", HttpStatus.OK.value());
        response.put("log", "");
        when(gigyaService.update(any())).thenReturn(response);

        // when
        HttpStatus updateResponse = updateAccountService.updateTimezoneInCDC(uid, timezone);

        // then
        Assertions.assertEquals(updateResponse, HttpStatus.OK);
    }

    @Test
    public void updateTimezoneInCDC_GivenAnInvalidUIDAndTimezone_ReturnInternalServerError() throws Exception {
        // given
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("log", "");
        when(gigyaService.update(any())).thenReturn(response);

        // when
        HttpStatus updateResponse = updateAccountService.updateTimezoneInCDC(uid, timezone);

        // then
        Assertions.assertEquals(updateResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void updateProfile_GivenAValidProfileInfoDTO_WhenCallupdateProfile_ThenShouldReturnOK() throws Exception {
        // given
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("code", HttpStatus.OK.value());
        response.put("log", "");
        when(gigyaService.update(any())).thenReturn(response);

        // when
        HttpStatus updateResponse = updateAccountService.updateProfile(profileInfoDTO);

        // then
        Assertions.assertEquals(updateResponse, HttpStatus.OK);
    }

    @Test
    public void updateProfile_GivenAnInvalidProfileInfoDTO_WhenCallupdateProfile_ThenShouldReturnBadRequest() throws Exception {
        // given
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("code", HttpStatus.BAD_REQUEST.value());
        response.put("log", "");
        when(gigyaService.update(any())).thenReturn(response);

        // when
        HttpStatus updateResponse = updateAccountService.updateProfile(ProfileInfoDTO.builder().uid("").build());

        // then
        Assertions.assertEquals(updateResponse, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void updateProfile_GivenAProfileWithEmail_WhenCallUpdateProfileAndLoginEmailIsRemoved_ThenShouldReturnAnOkHttpStatusCode() throws Exception {
        // given
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        profileInfoDTO.setEmail("email@email.com");
        profileInfoDTO.setActualEmail("email@email.com");
        profileInfoDTO.setActualUsername("email@email.com");
        ReflectionTestUtils.setField(updateAccountService, "isLegacyValidationEnabled", true);
        response.put("code", HttpStatus.OK.value());
        response.put("log", "");
        when(gigyaService.update(any())).thenReturn(response);

        // when
        HttpStatus updateResponse = updateAccountService.updateProfile(profileInfoDTO);

        // then
        Assertions.assertEquals(updateResponse, HttpStatus.OK);
    }


}
