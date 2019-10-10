package com.thermofisher.cdcam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    @Test
    public void getFederationAccountInfo_ifGivenUserInfoAndObj_returnAccountInfoWithPassword() throws Exception {
        // setup
        Mockito.when(accountBuilder.getAccountInfo(any(GSObject.class))).thenReturn(account);
        Mockito.when(accountBuilder.getFederationAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // execution
        AccountInfo res = accountBuilder.getFederationAccountInfo(new GSObject());

        // validation
        ObjectNode jsonAccount = mapper.valueToTree(res);
        String password = jsonAccount.get("password").asText();
        String _expectedAccount = mapper.writeValueAsString(account);
        String _account = mapper.writeValueAsString(jsonAccount);
        assertTrue(password != null);
        assertTrue(_expectedAccount.equals(_account));
    }

    @Test
    public void getFederationAccountInfo_ifGivenAInvalidObj_returnNull() throws Exception {
        // setup
        Mockito.when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        Mockito.when(accountBuilder.getFederationAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // execution
        AccountInfo res = accountBuilder.getFederationAccountInfo(new GSObject(invalidObj));

        // validation
        Assert.assertNull(res);
    }

    @Test
    public void getAccountToNotifyRegistration_ShouldReturnAccountInfoWithExpectedData() throws JsonProcessingException, Exception {
        // given
        prepareAccountInfoToNotify(account);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String expectedAccountInfo = mapper.writeValueAsString(account);
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        when(accountBuilder.getAccountToNotifyRegistration(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo accountInfo = accountBuilder.getAccountToNotifyRegistration(new GSObject(obj));
        String _accountInfo = mapper.writeValueAsString(accountInfo);

        // then
        assertEquals(expectedAccountInfo, _accountInfo);
    }

    @Test
    public void getAccountToNotifyRegistration_WhenAnExceptionOccurs_ThenNullShouldBeReturned() throws Exception {
        // given
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        when(accountBuilder.getAccountToNotifyRegistration(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo accountInfo = accountBuilder.getAccountToNotifyRegistration(new GSObject("{}"));

        // then
        assertNull(accountInfo);
    }

    public void prepareAccountInfoToNotify(AccountInfo account) {
        account.setMember(null);
        account.setLocaleName(null);
        account.setLoginProvider(null);
        account.setRegAttempts(0);
    }
}
