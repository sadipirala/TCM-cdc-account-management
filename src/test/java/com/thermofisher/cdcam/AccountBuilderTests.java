package com.thermofisher.cdcam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSObject;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.utils.AccountUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AccountBuilder.class)
public class AccountBuilderTests {
    private Logger logger = LogManager.getLogger(AccountBuilder.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private String federatedObj = AccountUtils.federatedCdcResponse;
    private String siteObj = AccountUtils.siteUserCdcResponse;
    private String invalidObj = AccountUtils.invalidCDCResponse;
    private AccountInfo federatedAccount;
    private AccountInfo siteAccount;

    @Mock
    AccountBuilder accountBuilder = new AccountBuilder();

    @Before
    public void setup() {
        federatedAccount = AccountUtils.getFederatedAccount();
        siteAccount = AccountUtils.getSiteAccount();
        ReflectionTestUtils.setField(accountBuilder, "logger", logger);
    }

    @Test
    public void getAccountInfo_ifGivenFederatedUserInfoAndObj_returnAccountInfo() throws Exception {
        // setup
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        
        // execution
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(federatedObj));

        // validation
        String expectedAccount = mapper.writeValueAsString(federatedAccount);
        String resAccount = mapper.writeValueAsString(res);
        assertEquals(expectedAccount, resAccount);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserInfoAndObj_returnAccountInfo() throws Exception {
        //setup
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        //execution
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteObj));

        //validation
        String expectedAccount = mapper.writeValueAsString(siteAccount);
        String resAccount = mapper.writeValueAsString(res);
        assertTrue(expectedAccount.equals(resAccount));
    }

    @Test
    public void getAccountInfo_ifGivenAInvalidObj_returnNull() throws Exception {
        // setup
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(invalidObj);

        // execution
        AccountInfo res = accountBuilder.getAccountInfo(jsonObj);

        // validation
        Assert.assertNull(res);
    }
}
