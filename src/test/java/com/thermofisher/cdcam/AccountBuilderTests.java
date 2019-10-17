package com.thermofisher.cdcam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSObject;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.utils.AccountInfoUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AccountBuilder.class)
public class AccountBuilderTests {
    private final ObjectMapper mapper = new ObjectMapper();
    private String obj = AccountInfoUtils.cdcResponse;
    private String invalidObj = AccountInfoUtils.invalidCDCResponse;
    private AccountInfo account;

    @Mock
    AccountBuilder accountBuilder = new AccountBuilder();

    @Before
    public void setup() {
        account = AccountInfoUtils.getAccount();
    }

    @Test
    public void getAccountInfo_ifGivenAnyUserInfoAndObj_returnAccountInfo() throws Exception {
        // setup
        Mockito.when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        
        // execution
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(obj));

        // validation
        String expectedAccount = mapper.writeValueAsString(account);
        String resAccount = mapper.writeValueAsString(res);
        assertTrue(expectedAccount.equals(resAccount));
        assertNull(res.getPassword());
    }

    @Test
    public void getAccountInfo_ifGivenAInvalidObj_returnNull() throws Exception {
        // setup
        Mockito.when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(invalidObj);

        // execution
        AccountInfo res = accountBuilder.getAccountInfo(jsonObj);

        // validation
        Assert.assertNull(res);
    }
}
