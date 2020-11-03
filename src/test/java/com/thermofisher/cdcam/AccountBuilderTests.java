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
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AccountBuilder.class)
public class AccountBuilderTests {
    private Logger logger = LogManager.getLogger(AccountBuilder.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private String federatedCdcResponse;
    private String siteCdcResponse;
    private String invalidCdcResponse;
    private AccountInfo federatedAccount;
    private AccountInfo siteAccount;

    @Mock
    AccountBuilder accountBuilder = new AccountBuilder();

    @Before
    public void setup() throws ParseException, IOException {
        siteCdcResponse = AccountUtils.getSiteAccountJsonString();
        federatedCdcResponse = AccountUtils.getFederatedAccountJsonString();
        invalidCdcResponse = AccountUtils.getInvalidAccountJsonString();
        federatedAccount = AccountUtils.getFederatedAccount();
        siteAccount = AccountUtils.getSiteAccount();
        ReflectionTestUtils.setField(accountBuilder, "logger", logger);
    }

    @Test
    public void getAccountInfo_ifGivenFederatedUserInfoAndObj_returnAccountInfo() throws Exception {
        // given
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        
        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(federatedCdcResponse));

        // then
        String expectedAccount = mapper.writeValueAsString(federatedAccount);
        String resAccount = mapper.writeValueAsString(res);
        assertEquals(expectedAccount, resAccount);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserInfoAndObj_returnAccountInfo() throws Exception {
        // given
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponse));

        // then
        String expectedAccount = mapper.writeValueAsString(siteAccount);
        String resAccount = mapper.writeValueAsString(res);
        assertTrue(expectedAccount.equals(resAccount));
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithHiraganaName_returnAccountInfoWithHiraganaName() throws Exception {
        // given
        String hiraganaName = AccountUtils.hiraganaName;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponse));

        // then
        assertEquals(res.getHiraganaName(), hiraganaName);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithJobRole_returnAccountInfoWithJobRole() throws Exception {
        // given
        String jobRole = AccountUtils.jobRole;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponse));

        // then
        assertEquals(res.getJobRole(), jobRole);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithInterest_returnAccountInfoWithInterest() throws Exception {
        // given
        String interest = AccountUtils.interest;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponse));

        // then
        assertEquals(res.getInterest(), interest);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithPhoneNumber_returnAccountInfoWithPhoneNumber() throws Exception {
        // given
        String phoneNumber = AccountUtils.phoneNumber;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponse));

        // then
        assertEquals(res.getPhoneNumber(), phoneNumber);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithECommerceTransaction_returnAccountInfoWithECommerceTransaction() throws Exception {
        // given
        Boolean eCommerceTransaction = AccountUtils.eComerceTransaction;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponse));

        // then
        assertEquals(res.getECommerceTransaction(), eCommerceTransaction);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithPersonalInfoMandatory_returnAccountInfoWithPersonalInfoMandatory() throws Exception {
        // given
        Boolean personalInfoMandatory = AccountUtils.personalInfoMandatory;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponse));

        // then
        assertEquals(res.getPersonalInfoMandatory(), personalInfoMandatory);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithPersonalInfoOptional_returnAccountInfoWithPersonalInfoOptional() throws Exception {
        // given
        Boolean personalInfoOptional = AccountUtils.personalInfoOptional;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponse));

        // then
        assertEquals(res.getPersonalInfoOptional(), personalInfoOptional);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithPrivateInfoMandatory_returnAccountInfoWithPrivateInfoMandatory() throws Exception {
        // given
        Boolean privateInfoMandatory = AccountUtils.privateInfoMandatory;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponse));

        // then
        assertEquals(res.getPrivateInfoMandatory(), privateInfoMandatory);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithPrivateInfoOptional_returnAccountInfoWithPrivateInfoOptional() throws Exception {
        // given
        Boolean privateInfoOptional = AccountUtils.privateInfoOptional;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponse));

        // then
        assertEquals(res.getPersonalInfoOptional(), privateInfoOptional);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithProcessingConsignment_returnAccountInfoWithProcessingConsignment() throws Exception {
        // given
        Boolean processingConsignment = AccountUtils.processingConsignment;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponse));

        // then
        assertEquals(res.getProcessingConsignment(), processingConsignment);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithTermsOfUse_returnAccountInfoWithTermsOfUse() throws Exception {
        // given
        Boolean termsOfUse = AccountUtils.termsOfUse;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponse));

        // then
        assertEquals(res.getTermsOfUse(), termsOfUse);
    }

    @Test
    public void getAccountInfo_ifGivenAInvalidObj_returnNull() throws Exception {
        // given
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(invalidCdcResponse);

        // when
        AccountInfo res = accountBuilder.getAccountInfo(jsonObj);

        // then
        Assert.assertNull(res);
    }
}
