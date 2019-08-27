package com.thermofisher.cdcam;

import com.thermofisher.cdcam.enums.cdc.APIMethods;
import com.thermofisher.cdcam.utils.Utils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Utils.class)
public class UtilsTests {

    @Test
    public void getAlphaNumericString_ifNumberIsProvided_returnSameSizeString() {
        //setup
        int stringSize = 10;
        //execution
        String value = Utils.getAlphaNumericString(stringSize);

        //validation
        Assert.assertEquals(value.length(), stringSize);
    }

    @Test
    public void ApiMethodGet_ifCalled_returnGETMethod() {
        //setup
        String getAccount = "accounts.getAccountInfo";
        //execution
        String enumGET = APIMethods.GET.getValue();
        //validation
        Assert.assertEquals(getAccount, enumGET);
    }

}
