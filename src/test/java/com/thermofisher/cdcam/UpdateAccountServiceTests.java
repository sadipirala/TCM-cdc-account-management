package com.thermofisher.cdcam;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.CdcamApplication;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class UpdateAccountServiceTests {

    private final String uid = "1234567890";
    private final String timezone = "America/Tijuana";
    @InjectMocks
    private UpdateAccountService updateAccountService;

    @Mock
    private CDCResponseHandler cdcAccountsService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
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
}
