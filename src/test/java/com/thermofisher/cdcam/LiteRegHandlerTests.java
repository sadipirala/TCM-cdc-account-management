package com.thermofisher.cdcam;

import com.gigya.socialize.GSResponse;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class LiteRegHandlerTests {

    @InjectMocks
    LiteRegHandler liteRegHandler;

    @Mock
    CDCAccounts cdcAccounts;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void process_givenEmailList_ReturnEECUserList() throws JSONException {
        GSResponse mockGSResponse = Mockito.mock(GSResponse.class);

        String mockResponse = "{\n" +
                "  \"totalCount\": 1,\n" +
                "  \"results\": [\n" +
                "  \t{\n" +
                "  \t\t\"UID\": \"abc123\",\n" +
                "  \t\t\"isRegistered\": true\n" +
                "  \t}\n" +
                "  ]\n" +
                "}";

        when(mockGSResponse.getResponseText()).thenReturn(mockResponse);
        when(cdcAccounts.searchByEmail(anyString())).thenReturn(mockGSResponse);

        List<String> emails = new ArrayList();
        emails.add("test1");
        emails.add("test2");

        EmailList emailList = EmailList.builder().emails(emails).build();

        List<EECUser> output = liteRegHandler.process(emailList);
        Assert.assertNotNull(output);
    }
}
