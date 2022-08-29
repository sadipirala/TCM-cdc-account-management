package com.thermofisher.cdcam.builders;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSObject;
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

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AccountBuilder.class)
public class AccountBuilderTests {
    private Logger logger = LogManager.getLogger(AccountBuilder.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private String federatedCdcResponse;
    private String siteCdcResponse;
    private String siteCdcResponseV2;
    private String siteCdcMarketingConsentFalseResponse;
    private String siteCdcMarketingConsentFalseResponseV2;
    private String siteCdcResponseJapan;
    private String siteCdcResponseKorea;
    private String siteCdcResponseKoreaV2;
    private String siteCdcResponseChina;
    private String invalidCdcResponse;
    private String siteAccountIncompleteAccount;
    private AccountInfo federatedAccount;
    private AccountInfo siteAccountJapan;

    @Mock
    AccountBuilder accountBuilder = new AccountBuilder();

    @Before
    public void setup() throws ParseException, IOException {
        siteCdcResponse = AccountUtils.getSiteAccountJsonString();
        siteCdcResponseV2 = AccountUtils.getSiteAccountJsonStringV2();
        siteCdcMarketingConsentFalseResponse = AccountUtils.getSiteAccountWithMarketingConsentAsFalse();
        siteCdcMarketingConsentFalseResponseV2 = AccountUtils.getSiteAccountWithMarketingConsentAsFalseV2();
        siteCdcResponseJapan = AccountUtils.getSiteAccountJapanJsonString();
        siteCdcResponseKorea = AccountUtils.getSiteAccountKoreaJsonString();
        siteCdcResponseKoreaV2 = AccountUtils.getSiteAccountKoreaJsonStringV2();
        siteCdcResponseChina = AccountUtils.getSiteAccountChinaJsonString();
        federatedCdcResponse = AccountUtils.getFederatedAccountJsonString();
        invalidCdcResponse = AccountUtils.getInvalidAccountJsonString();
        federatedAccount = AccountUtils.getFederatedAccount();
        siteAccountJapan = AccountUtils.getSiteAccountJapan();
        siteAccountIncompleteAccount = AccountUtils.getSiteAccountIncomplete();

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
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseJapan));

        // then
        String expectedAccount = mapper.writeValueAsString(siteAccountJapan);
        String resAccount = mapper.writeValueAsString(res);
        assertTrue(expectedAccount.equals(resAccount));
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithHiraganaName_returnAccountInfoWithHiraganaName() throws Exception {
        // given
        String hiraganaName = AccountUtils.hiraganaName;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseJapan));

        // then
        assertEquals(res.getHiraganaName(), hiraganaName);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithJobRole_returnAccountInfoWithJobRole() throws Exception {
        // given
        String jobRole = AccountUtils.jobRole;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseChina));

        // then
        assertEquals(res.getJobRole(), jobRole);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithInterest_returnAccountInfoWithInterest() throws Exception {
        // given
        String interest = AccountUtils.interest;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseChina));

        // then
        assertEquals(res.getInterest(), interest);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithPhoneNumber_returnAccountInfoWithPhoneNumber() throws Exception {
        // given
        String phoneNumber = AccountUtils.phoneNumber;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseChina));

        // then
        assertEquals(res.getPhoneNumber(), phoneNumber);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithReceiveMarketingInformation_returnAccountInfoWithReceiveMarketingInformation() throws Exception {
        // given
        Boolean receiveMarketingInformation = AccountUtils.receiveMarketingInformation;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo response = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        assertEquals(receiveMarketingInformation, response.getReceiveMarketingInformation());
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithThirdPartyTransferPersonalInfoMandatory_returnAccountInfoWithThirdPartyTransferPersonalInfoMandatory() throws Exception {
        // given
        Boolean thirdPartyTransferPersonalInfoMandatory = AccountUtils.thirdPartyTransferPersonalInfoMandatory;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        assertEquals(res.getThirdPartyTransferPersonalInfoMandatory(), thirdPartyTransferPersonalInfoMandatory);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithThirdPartyTransferPersonalInfoOptional_returnAccountInfoWithThirdPartyTransferPersonalInfoOptional() throws Exception {
        // given
        Boolean thirdPartyTransferPersonalInfoOptional = AccountUtils.thirdPartyTransferPersonalInfoOptional;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        assertEquals(res.getThirdPartyTransferPersonalInfoOptional(), thirdPartyTransferPersonalInfoOptional);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithCollectionAndUsePersonalInfoMandatory_returnAccountInfoWithCollectionAndUsePersonalInfoMandatory() throws Exception {
        // given
        Boolean collectionAndUsePersonalInfoMandatory = AccountUtils.collectionAndUsePersonalInfoMandatory;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        assertEquals(res.getCollectionAndUsePersonalInfoMandatory(), collectionAndUsePersonalInfoMandatory);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithCollectionAndUsePersonalInfoOptional_returnAccountInfoWithCollectionAndUsePersonalInfoOptional() throws Exception {
        // given
        Boolean collectionAndUsePersonalInfoOptional = AccountUtils.collectionAndUsePersonalInfoOptional;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        assertEquals(res.getCollectionAndUsePersonalInfoOptional(), collectionAndUsePersonalInfoOptional);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithCollectionAndUsePersonalInfoMarketing_returnAccountInfoWithCollectionAndUsePersonalInfoMarketing() throws Exception {
        // given
        Boolean collectionAndUsePersonalInfoMarketing = AccountUtils.collectionAndUsePersonalInfoMarketing;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        assertEquals(res.getCollectionAndUsePersonalInfoMarketing(), collectionAndUsePersonalInfoMarketing);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithOverseasTransferPersonalInfoMandatory_returnAccountInfoWithOverseasTransferPersonalInfoMandatory() throws Exception {
        // given
        Boolean overseasTransferPersonalInfoMandatory = AccountUtils.overseasTransferPersonalInfoMandatory;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        assertEquals(res.getOverseasTransferPersonalInfoMandatory(), overseasTransferPersonalInfoMandatory);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithOverseasTransferPersonalInfoOptional_returnAccountInfoWithOverseasTransferPersonalInfoOptional() throws Exception {
        // given
        Boolean overseasTransferPersonalInfoOptional = AccountUtils.overseasTransferPersonalInfoOptional;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        assertEquals(res.getOverseasTransferPersonalInfoOptional(), overseasTransferPersonalInfoOptional);
    }
    
    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithReceiveMarketingInformation_returnAccountInfoWithReceiveMarketingInformation() throws Exception {
        // given
        Boolean receiveMarketingInformation = AccountUtils.receiveMarketingInformation;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo response = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        assertEquals(receiveMarketingInformation, response.getReceiveMarketingInformation());
    }

    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithThirdPartyTransferPersonalInfoMandatory_returnAccountInfoWithThirdPartyTransferPersonalInfoMandatory() throws Exception {
        // given
        Boolean thirdPartyTransferPersonalInfoMandatory = AccountUtils.thirdPartyTransferPersonalInfoMandatory;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        assertEquals(res.getThirdPartyTransferPersonalInfoMandatory(), thirdPartyTransferPersonalInfoMandatory);
    }

    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithThirdPartyTransferPersonalInfoOptional_returnAccountInfoWithThirdPartyTransferPersonalInfoOptional() throws Exception {
        // given
        Boolean thirdPartyTransferPersonalInfoOptional = AccountUtils.thirdPartyTransferPersonalInfoOptional;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        assertEquals(res.getThirdPartyTransferPersonalInfoOptional(), thirdPartyTransferPersonalInfoOptional);
    }

    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithCollectionAndUsePersonalInfoMandatory_returnAccountInfoWithCollectionAndUsePersonalInfoMandatory() throws Exception {
        // given
        Boolean collectionAndUsePersonalInfoMandatory = AccountUtils.collectionAndUsePersonalInfoMandatory;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        assertEquals(res.getCollectionAndUsePersonalInfoMandatory(), collectionAndUsePersonalInfoMandatory);
    }

    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithCollectionAndUsePersonalInfoOptional_returnAccountInfoWithCollectionAndUsePersonalInfoOptional() throws Exception {
        // given
        Boolean collectionAndUsePersonalInfoOptional = AccountUtils.collectionAndUsePersonalInfoOptional;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        assertEquals(res.getCollectionAndUsePersonalInfoOptional(), collectionAndUsePersonalInfoOptional);
    }

    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithCollectionAndUsePersonalInfoMarketing_returnAccountInfoWithCollectionAndUsePersonalInfoMarketing() throws Exception {
        // given
        Boolean collectionAndUsePersonalInfoMarketing = AccountUtils.collectionAndUsePersonalInfoMarketing;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        assertEquals(res.getCollectionAndUsePersonalInfoMarketing(), collectionAndUsePersonalInfoMarketing);
    }

    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithOverseasTransferPersonalInfoMandatory_returnAccountInfoWithOverseasTransferPersonalInfoMandatory() throws Exception {
        // given
        Boolean overseasTransferPersonalInfoMandatory = AccountUtils.overseasTransferPersonalInfoMandatory;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        assertEquals(res.getOverseasTransferPersonalInfoMandatory(), overseasTransferPersonalInfoMandatory);
    }

    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithOverseasTransferPersonalInfoOptional_returnAccountInfoWithOverseasTransferPersonalInfoOptional() throws Exception {
        // given
        Boolean overseasTransferPersonalInfoOptional = AccountUtils.overseasTransferPersonalInfoOptional;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        assertEquals(res.getOverseasTransferPersonalInfoOptional(), overseasTransferPersonalInfoOptional);
    }

    @Test
    public void getAccountInfoV2_ShouldSetDefaultValues_WhenPropertiesAreMissing() throws Exception {
        // given
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(siteAccountIncompleteAccount);

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(jsonObj);

        // then
        assertEquals(res.getEmailAddress(), "");
        assertEquals(res.getFirstName(), "");
        assertEquals(res.getLastName(), "");
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

    @Test
    public void getAccountInfo_GivenUserAllowsMarketingConsent_ThenIsConsentGrantedShouldBeTrue() throws Exception {
        // given
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(siteCdcResponse);

        // when
        AccountInfo res = accountBuilder.getAccountInfo(jsonObj);

        // then
        assertTrue(res.isMarketingConsent());
    }

    @Test
    public void getAccountInfo_ShouldSetDefaultValues_WhenPropertiesAreMissing() throws Exception {
        // given
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(siteAccountIncompleteAccount);

        // when
        AccountInfo res = accountBuilder.getAccountInfo(jsonObj);

        // then
        assertEquals(res.getEmailAddress(), "");
        assertEquals(res.getFirstName(), "");
        assertEquals(res.getLastName(), "");
    }

    @Test
    public void getAccountInfoV2_GivenUserAllowsMarketingConsent_ThenIsConsentGrantedShouldBeTrue() throws Exception {
        // given
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(siteCdcResponseV2);

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(jsonObj);

        // then
        assertTrue(res.isMarketingConsent());
    }

    @Test
    public void getAccountInfoV2_ifGivenAInvalidObj_returnNull() throws Exception {
        // given
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(invalidCdcResponse);

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(jsonObj);

        // then
        Assert.assertNull(res);
    }

    @Test
    public void getAccountInfo_WhenPreferencesIsAnEmptyObject_ThenIsConsentGrantedShouldBeFalse() throws Exception {
        // given
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(siteCdcMarketingConsentFalseResponse);

        // when
        AccountInfo res = accountBuilder.getAccountInfo(jsonObj);

        // then
        assertFalse(res.isMarketingConsent());
    }
    
    @Test
    public void getAccountInfoV2_WhenPreferencesIsAnEmptyObject_ThenIsConsentGrantedShouldBeFalse() throws Exception {
        // given
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(siteCdcMarketingConsentFalseResponseV2);

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(jsonObj);

        // then
        assertFalse(res.isMarketingConsent());
    }

    public void getAccountInfo_GivenAccountHasOpenIdProvider_ThenReturnAssignProviderClientId() throws Exception {
        // given
        String openIdProviderId = AccountUtils.openIdProviderId;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponse));

        // then
        assertEquals(res.getOpenIdProviderId(), openIdProviderId);
    }
}