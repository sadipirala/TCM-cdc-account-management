package com.thermofisher.cdcam.builders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSObject;
import com.google.gson.JsonSyntaxException;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.utils.AccountUtils;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class  AccountBuilderTests {

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

    @BeforeEach
    public void setup() throws IOException, JsonSyntaxException {
        MockitoAnnotations.openMocks(this);
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

        //   ReflectionTestUtils.setField(accountBuilder, "log", LoggerFactory.getLogger(AccountBuilder.class));
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
        Assertions.assertThat(expectedAccount).isEqualTo(resAccount);
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
        Assertions.assertThat(expectedAccount).isEqualTo(resAccount);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithHiraganaName_returnAccountInfoWithHiraganaName() throws Exception {
        // given
        String hiraganaName = AccountUtils.hiraganaName;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseJapan));

        // then
        Assertions.assertThat(res.getHiraganaName()).isEqualTo(hiraganaName);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithJobRole_returnAccountInfoWithJobRole() throws Exception {
        // given
        String jobRole = AccountUtils.jobRole;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseChina));

        // then
        Assertions.assertThat(res.getJobRole()).isEqualTo(jobRole);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithInterest_returnAccountInfoWithInterest() throws Exception {
        // given
        String interest = AccountUtils.interest;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseChina));

        // then
        Assertions.assertThat(res.getInterest()).isEqualTo(interest);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithPhoneNumber_returnAccountInfoWithPhoneNumber() throws Exception {
        // given
        String phoneNumber = AccountUtils.phoneNumber;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseChina));

        // then
        Assertions.assertThat(res.getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithReceiveMarketingInformation_returnAccountInfoWithReceiveMarketingInformation() throws Exception {
        // given
        Boolean receiveMarketingInformation = AccountUtils.receiveMarketingInformation;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo response = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        Assertions.assertThat(receiveMarketingInformation).isEqualTo(response.getReceiveMarketingInformation());
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithThirdPartyTransferPersonalInfoMandatory_returnAccountInfoWithThirdPartyTransferPersonalInfoMandatory() throws Exception {
        // given
        Boolean thirdPartyTransferPersonalInfoMandatory = AccountUtils.thirdPartyTransferPersonalInfoMandatory;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        Assertions.assertThat(res.getThirdPartyTransferPersonalInfoMandatory()).isEqualTo(thirdPartyTransferPersonalInfoMandatory);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithThirdPartyTransferPersonalInfoOptional_returnAccountInfoWithThirdPartyTransferPersonalInfoOptional() throws Exception {
        // given
        Boolean thirdPartyTransferPersonalInfoOptional = AccountUtils.thirdPartyTransferPersonalInfoOptional;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        Assertions.assertThat(res.getThirdPartyTransferPersonalInfoOptional()).isEqualTo(thirdPartyTransferPersonalInfoOptional);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithCollectionAndUsePersonalInfoMandatory_returnAccountInfoWithCollectionAndUsePersonalInfoMandatory() throws Exception {
        // given
        Boolean collectionAndUsePersonalInfoMandatory = AccountUtils.collectionAndUsePersonalInfoMandatory;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        Assertions.assertThat(res.getCollectionAndUsePersonalInfoMandatory()).isEqualTo(collectionAndUsePersonalInfoMandatory);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithCollectionAndUsePersonalInfoOptional_returnAccountInfoWithCollectionAndUsePersonalInfoOptional() throws Exception {
        // given
        Boolean collectionAndUsePersonalInfoOptional = AccountUtils.collectionAndUsePersonalInfoOptional;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        Assertions.assertThat(res.getCollectionAndUsePersonalInfoOptional()).isEqualTo(collectionAndUsePersonalInfoOptional);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithCollectionAndUsePersonalInfoMarketing_returnAccountInfoWithCollectionAndUsePersonalInfoMarketing() throws Exception {
        // given
        Boolean collectionAndUsePersonalInfoMarketing = AccountUtils.collectionAndUsePersonalInfoMarketing;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        Assertions.assertThat(res.getCollectionAndUsePersonalInfoMarketing()).isEqualTo(collectionAndUsePersonalInfoMarketing);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithOverseasTransferPersonalInfoMandatory_returnAccountInfoWithOverseasTransferPersonalInfoMandatory() throws Exception {
        // given
        Boolean overseasTransferPersonalInfoMandatory = AccountUtils.overseasTransferPersonalInfoMandatory;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        Assertions.assertThat(res.getOverseasTransferPersonalInfoMandatory()).isEqualTo(overseasTransferPersonalInfoMandatory);
    }

    @Test
    public void getAccountInfo_ifGivenSiteUserWithOverseasTransferPersonalInfoOptional_returnAccountInfoWithOverseasTransferPersonalInfoOptional() throws Exception {
        // given
        Boolean overseasTransferPersonalInfoOptional = AccountUtils.overseasTransferPersonalInfoOptional;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponseKorea));

        // then
        Assertions.assertThat(res.getOverseasTransferPersonalInfoOptional()).isEqualTo(overseasTransferPersonalInfoOptional);
    }

    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithReceiveMarketingInformation_returnAccountInfoWithReceiveMarketingInformation() throws Exception {
        // given
        Boolean receiveMarketingInformation = AccountUtils.receiveMarketingInformation;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo response = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        Assertions.assertThat(receiveMarketingInformation).isEqualTo(response.getReceiveMarketingInformation());
    }

    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithThirdPartyTransferPersonalInfoMandatory_returnAccountInfoWithThirdPartyTransferPersonalInfoMandatory() throws Exception {
        // given
        Boolean thirdPartyTransferPersonalInfoMandatory = AccountUtils.thirdPartyTransferPersonalInfoMandatory;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        Assertions.assertThat(res.getThirdPartyTransferPersonalInfoMandatory()).isEqualTo(thirdPartyTransferPersonalInfoMandatory);
    }

    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithThirdPartyTransferPersonalInfoOptional_returnAccountInfoWithThirdPartyTransferPersonalInfoOptional() throws Exception {
        // given
        Boolean thirdPartyTransferPersonalInfoOptional = AccountUtils.thirdPartyTransferPersonalInfoOptional;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        Assertions.assertThat(res.getThirdPartyTransferPersonalInfoOptional()).isEqualTo(thirdPartyTransferPersonalInfoOptional);
    }

    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithCollectionAndUsePersonalInfoMandatory_returnAccountInfoWithCollectionAndUsePersonalInfoMandatory() throws Exception {
        // given
        Boolean collectionAndUsePersonalInfoMandatory = AccountUtils.collectionAndUsePersonalInfoMandatory;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        Assertions.assertThat(res.getCollectionAndUsePersonalInfoMandatory()).isEqualTo(collectionAndUsePersonalInfoMandatory);
    }

    @Test
    public void getAccountInfoV2_getThermofisher_legacyUserName() throws Exception {

        // given
        String legacyUsername = AccountUtils.getThermofisher().getLegacyUsername();
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponse));

        // then
        Assertions.assertThat(res.getLegacyUserName()).isEqualTo(legacyUsername);
    }

    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithCollectionAndUsePersonalInfoOptional_returnAccountInfoWithCollectionAndUsePersonalInfoOptional() throws Exception {
        // given
        Boolean collectionAndUsePersonalInfoOptional = AccountUtils.collectionAndUsePersonalInfoOptional;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        Assertions.assertThat(res.getCollectionAndUsePersonalInfoOptional()).isEqualTo(collectionAndUsePersonalInfoOptional);
    }

    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithCollectionAndUsePersonalInfoMarketing_returnAccountInfoWithCollectionAndUsePersonalInfoMarketing() throws Exception {
        // given
        Boolean collectionAndUsePersonalInfoMarketing = AccountUtils.collectionAndUsePersonalInfoMarketing;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        Assertions.assertThat(res.getCollectionAndUsePersonalInfoMarketing()).isEqualTo(collectionAndUsePersonalInfoMarketing);
    }

    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithOverseasTransferPersonalInfoMandatory_returnAccountInfoWithOverseasTransferPersonalInfoMandatory() throws Exception {
        // given
        Boolean overseasTransferPersonalInfoMandatory = AccountUtils.overseasTransferPersonalInfoMandatory;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        Assertions.assertThat(res.getOverseasTransferPersonalInfoMandatory()).isEqualTo(overseasTransferPersonalInfoMandatory);
    }

    @Test
    public void getAccountInfoV2_ifGivenSiteUserWithOverseasTransferPersonalInfoOptional_returnAccountInfoWithOverseasTransferPersonalInfoOptional() throws Exception {
        // given
        Boolean overseasTransferPersonalInfoOptional = AccountUtils.overseasTransferPersonalInfoOptional;
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(new GSObject(siteCdcResponseKoreaV2));

        // then
        Assertions.assertThat(res.getOverseasTransferPersonalInfoOptional()).isEqualTo(overseasTransferPersonalInfoOptional);
    }

    @Test
    public void getAccountInfoV2_ShouldSetDefaultValues_WhenPropertiesAreMissing() throws Exception {
        // given
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(siteAccountIncompleteAccount);

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(jsonObj);

        // then
        Assertions.assertThat(res.getEmailAddress()).isEqualTo("");
        Assertions.assertThat(res.getFirstName()).isEqualTo("");
        Assertions.assertThat(res.getLastName()).isEqualTo("");
    }

    @Test
    public void getAccountInfo_ifGivenAInvalidObj_returnNull() throws Exception {
        // given
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(invalidCdcResponse);

        // when
        AccountInfo res = accountBuilder.getAccountInfo(jsonObj);

        // then
        Assertions.assertThat(res).isNull();
    }

    @Test
    public void getAccountInfo_GivenUserAllowsMarketingConsent_ThenIsConsentGrantedShouldBeTrue() throws Exception {
        // given
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(siteCdcResponse);

        // when
        AccountInfo res = accountBuilder.getAccountInfo(jsonObj);

        // then
        Assertions.assertThat(res.isMarketingConsent()).isTrue();
    }

    @Test
    public void getAccountInfo_ShouldSetDefaultValues_WhenPropertiesAreMissing() throws Exception {
        // given
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(siteAccountIncompleteAccount);

        // when
        AccountInfo res = accountBuilder.getAccountInfo(jsonObj);

        // then
        Assertions.assertThat(res.getEmailAddress()).isEqualTo("");
        Assertions.assertThat(res.getFirstName()).isEqualTo("");
        Assertions.assertThat(res.getLastName()).isEqualTo("");
    }

    @Test
    public void getAccountInfoV2_GivenUserAllowsMarketingConsent_ThenIsConsentGrantedShouldBeTrue() throws Exception {
        // given
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(siteCdcResponseV2);

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(jsonObj);

        // then
        Assertions.assertThat(res.isMarketingConsent()).isTrue();
    }

    @Test
    public void getAccountInfoV2_ifGivenAInvalidObj_returnNull() throws Exception {
        // given
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(invalidCdcResponse);

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(jsonObj);

        // then
        Assertions.assertThat(res).isNull();
    }

    @Test
    public void getAccountInfo_WhenPreferencesIsAnEmptyObject_ThenIsConsentGrantedShouldBeFalse() throws Exception {
        // given
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(siteCdcMarketingConsentFalseResponse);

        // when
        AccountInfo res = accountBuilder.getAccountInfo(jsonObj);

        // then
        Assertions.assertThat(res.isMarketingConsent()).isFalse();
    }

    @Test
    public void getAccountInfoV2_WhenPreferencesIsAnEmptyObject_ThenIsConsentGrantedShouldBeFalse() throws Exception {
        // given
        when(accountBuilder.getAccountInfoV2(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(siteCdcMarketingConsentFalseResponseV2);

        // when
        AccountInfo res = accountBuilder.getAccountInfoV2(jsonObj);

        // then
        Assertions.assertThat(res.isMarketingConsent()).isFalse();
    }

    public void getAccountInfo_GivenAccountHasOpenIdProvider_ThenReturnAssignProviderClientId() throws Exception {
        // given
        String openIdProviderId = AccountUtils.openIdProviderId;
        when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject(siteCdcResponse));

        // then
        Assertions.assertThat(res.getOpenIdProviderId()).isEqualTo(openIdProviderId);
    }
}
