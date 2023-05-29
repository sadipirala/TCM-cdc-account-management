package com.thermofisher.cdcam.utils.cdc;

import com.thermofisher.cdcam.enums.CountryCodes;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccountV2;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.Utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CDCAccountsHandler.class)
public class CDCAccountsHandlerTests {

    @Test
    public void buildNewCDCAccount_ShouldBuildACDCNewAccountObjectAsCDCNeeds() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        CDCNewAccount expectedCDCNewAccount = AccountUtils.getNewCDCAccount(accountInfo);

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertEquals(expectedCDCNewAccount.getUsername(), result.getUsername());
        assertEquals(expectedCDCNewAccount.getEmail(), result.getEmail());
        assertEquals(expectedCDCNewAccount.getPassword(), result.getPassword());
        assertEquals(expectedCDCNewAccount.getData(), result.getData());
        assertEquals(expectedCDCNewAccount.getProfile(), result.getProfile());
    }

    @Test
    public void buildNewCDCAccount_ShouldContainProfileLocaleAsLanguageCode() throws JSONException {
        // given
        String locale = AccountUtils.localeName;
        String expectedLocale = Utils.parseLocale(locale);
        AccountInfo accountInfo = AccountUtils.getSiteAccount();

        // when
        CDCNewAccount expectedAccount = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(expectedAccount.getProfile().contains(String.format("\"locale\":\"%s\"", expectedLocale)));
    }

    @Test
    public void buildNewCDCAccount_ShouldBuildACDCNewAccountObjectWithLocaleNull() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccountWithoutLocale();
        CDCNewAccount expectedCDCNewAccount = AccountUtils.getNewCDCAccount(accountInfo);

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertEquals(expectedCDCNewAccount.getUsername(), result.getUsername());
        assertEquals(expectedCDCNewAccount.getEmail(), result.getEmail());
        assertEquals(expectedCDCNewAccount.getPassword(), result.getPassword());
        assertEquals(expectedCDCNewAccount.getData(), result.getData());
        assertEquals(expectedCDCNewAccount.getProfile(), result.getProfile());
    }

    @Test
    public void buildNewCDCAccount_GivenAspireAccountContainsCompanyAndIsNotAMember_ThenCDCAccountShouldNotContainCompany() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setMarketingConsent(false);
        String company = accountInfo.getCompany();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertFalse(result.getProfile().contains(String.format("\"company\":\"%s\"", company)));
    }

    @Test
    public void buildNewCDCAccount_GivenAspireAccountContainsCityAndIsNotAMember_ThenCDCAccountShouldNotContainCity() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setMarketingConsent(false);
        String city = accountInfo.getCity();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertFalse(result.getProfile().contains(String.format("\"city\":\"%s\"", city)));
    }

    @Test
    public void buildNewCDCAccount_GivenAspireAccountContainsCountryAndIsNotAMember_ThenCDCAccountShouldContainCountry() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setMarketingConsent(false);
        String country = accountInfo.getCountry();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getProfile().contains(String.format("\"country\":\"%s\"", country)));
    }

    @Test
    public void buildNewCDCAccount_GivenAspireAccountContainsPhoneNumberAndIsNotAMember_ThenCDCAccountShouldNotContainPhoneNumber() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setMarketingConsent(false);
        String phoneNumber = accountInfo.getPhoneNumber();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertFalse(result.getData().contains(String.format("\"phoneNumber\":\"%s\"", phoneNumber)));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoHasJapanAsCountry_ThenCDCAccountShouldContainJapanObject() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccountJapan();
        accountInfo.setCountry(CountryCodes.JAPAN.getValue());

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"hiraganaName\":\"%s\"", accountInfo.getHiraganaName())));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoDoesNotHaveJapanAsCountry_ThenCDCAccountShouldNotJapanObject() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setCountry(CountryCodes.CHINA.getValue());

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertFalse(result.getData().contains("japan"));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoHasChinaAsCountry_ThenCDCAccountShouldContainChinaObject() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccountChina();
        accountInfo.setCountry(CountryCodes.CHINA.getValue());

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"jobRole\":\"%s\"", accountInfo.getJobRole())));
        assertTrue(result.getData().contains(String.format("\"interest\":\"%s\"", accountInfo.getInterest())));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoDoesNotHaveChinaAsCountry_ThenCDCAccountShouldNotContainChinaObject() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setCountry(CountryCodes.JAPAN.getValue());

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertFalse(result.getData().contains("china"));
    }

    @Test
    public void buildNewCDCAccount_GivenChinaAccountContainsPhoneNumberAndIsMember_ThenCDCAccountShouldContainPhoneNumber() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccountChina();
        accountInfo.setCountry(CountryCodes.CHINA.getValue());
        accountInfo.setMarketingConsent(true);
        String phoneNumber = accountInfo.getPhoneNumber();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"phoneNumber\":\"%s\"", phoneNumber)));
    }

    @Test
    public void buildNewCDCAccount_GivenChinaAccountContainsPhoneNumberAndIsNotMember_ThenCDCAccountShouldNotContainPhoneNumber() throws JSONException {
        // given
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setCountry(CountryCodes.CHINA.getValue());
        accountInfo.setMarketingConsent(false);
        String phoneNumber = accountInfo.getPhoneNumber();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertFalse(result.getData().contains(String.format("\"phoneNumber\":\"%s\"", phoneNumber)));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoHasKoreaAsCountry_ThenCDCAccountShouldKoreaObject() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccountKorea();
        accountInfo.setCountry(CountryCodes.KOREA.getValue());

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"receiveMarketingInformation\":%s", accountInfo.getReceiveMarketingInformation())));
        assertTrue(result.getData().contains(String.format("\"thirdPartyTransferPersonalInfoMandatory\":%s", accountInfo.getThirdPartyTransferPersonalInfoMandatory())));
        assertTrue(result.getData().contains(String.format("\"thirdPartyTransferPersonalInfoOptional\":%s", accountInfo.getThirdPartyTransferPersonalInfoOptional())));
        assertTrue(result.getData().contains(String.format("\"collectionAndUsePersonalInfoMandatory\":%s", accountInfo.getCollectionAndUsePersonalInfoMandatory())));
        assertTrue(result.getData().contains(String.format("\"collectionAndUsePersonalInfoOptional\":%s", accountInfo.getCollectionAndUsePersonalInfoOptional())));
        assertTrue(result.getData().contains(String.format("\"collectionAndUsePersonalInfoMarketing\":%s", accountInfo.getCollectionAndUsePersonalInfoMarketing())));
        assertTrue(result.getData().contains(String.format("\"overseasTransferPersonalInfoOptional\":%s", accountInfo.getOverseasTransferPersonalInfoOptional())));
        assertTrue(result.getData().contains(String.format("\"overseasTransferPersonalInfoMandatory\":%s", accountInfo.getOverseasTransferPersonalInfoMandatory())));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoDoesNotHaveKoreaAsCountry_ThenCDCAccountShouldNotHaveKoreaObject() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setCountry(CountryCodes.CHINA.getValue());

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertFalse(result.getData().contains("korea"));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountHasProviderId_ThenCDCAccountShouldHaveProviderClientId() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setOpenIdProviderId(RandomStringUtils.randomAlphanumeric(10));

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getData().contains("clientID"));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountDoesNotHaveProviderId_ThenCDCAccountShouldNotHaveProviderClientId() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertFalse(result.getData().contains("clientID"));
    }

    @Test
    public void buildNewCDCAccount_v2_ShouldBuildACDCNewAccountObjectWithCorrectFields() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        CDCNewAccountV2 expectedCDCNewAccount = AccountUtils.getNewCDCAccountV2(accountInfo);

        // when
        CDCNewAccountV2 result = CDCAccountsHandler.buildCDCNewAccountV2(accountInfo);

        // then
        assertEquals(expectedCDCNewAccount.getUsername(), result.getUsername());
        assertEquals(expectedCDCNewAccount.getEmail(), result.getEmail());
        assertEquals(expectedCDCNewAccount.getPassword(), result.getPassword());
        assertEquals(expectedCDCNewAccount.getData(), result.getData());
        assertEquals(expectedCDCNewAccount.getProfile(), result.getProfile());
        assertEquals(expectedCDCNewAccount.getPreferences(), result.getPreferences());
    }

    @Test
    public void buildNewCDCAccount_v2_ShouldBuildACDCNewAccountObjectWithLocaleNull() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccountWithoutLocale();
        CDCNewAccountV2 expectedCDCNewAccount = AccountUtils.getNewCDCAccountV2(accountInfo);

        // when
        CDCNewAccountV2 result = CDCAccountsHandler.buildCDCNewAccountV2(accountInfo);

        // then
        assertEquals(expectedCDCNewAccount.getUsername(), result.getUsername());
        assertEquals(expectedCDCNewAccount.getEmail(), result.getEmail());
        assertEquals(expectedCDCNewAccount.getPassword(), result.getPassword());
        assertEquals(expectedCDCNewAccount.getData(), result.getData());
        assertEquals(expectedCDCNewAccount.getProfile(), result.getProfile());
        assertEquals(expectedCDCNewAccount.getPreferences(), result.getPreferences());
    }

    @Test
    public void buildNewCDCAccount_v2_GivenAccountInfoHasJapanAsCountry_ThenCDCAccountShouldContainJapanObject() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccountJapan();
        accountInfo.setCountry(CountryCodes.JAPAN.getValue());

        // when
        CDCNewAccountV2 result = CDCAccountsHandler.buildCDCNewAccountV2(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"hiraganaName\":\"%s\"", accountInfo.getHiraganaName())));
    }

    @Test
    public void buildNewCDCAccount_v2_GivenAccountInfoHasChinaAsCountry_ThenCDCAccountShouldContainChinaObject() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccountChina();
        accountInfo.setCountry(CountryCodes.CHINA.getValue());

        // when
        CDCNewAccountV2 result = CDCAccountsHandler.buildCDCNewAccountV2(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"jobRole\":\"%s\"", accountInfo.getJobRole())));
        assertTrue(result.getData().contains(String.format("\"interest\":\"%s\"", accountInfo.getInterest())));
    }

    @Test
    public void buildNewCDCAccount_v2_GivenAccountHasProviderId_ThenCDCAccountShouldHaveProviderClientId() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setOpenIdProviderId(RandomStringUtils.randomAlphanumeric(10));

        // when
        CDCNewAccountV2 result = CDCAccountsHandler.buildCDCNewAccountV2(accountInfo);

        // then
        assertTrue(result.getData().contains("clientID"));
    }

    @Test
    public void buildNewCDCAccount_v2_GivenAccountInfoHasKoreaAsCountry_ThenCDCAccountShouldKoreaObject() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccountKorea();
        accountInfo.setCountry(CountryCodes.KOREA.getValue());

        // when
        CDCNewAccountV2 result = CDCAccountsHandler.buildCDCNewAccountV2(accountInfo);

        // then
        assertTrue(result.getPreferences().contains(String.format("\"receiveMarketingInformation\":{\"isConsentGranted\":%s}", accountInfo.getReceiveMarketingInformation())));
        assertTrue(result.getPreferences().contains(String.format("\"thirdPartyTransferPersonalInfoMandatory\":{\"isConsentGranted\":%s}", accountInfo.getThirdPartyTransferPersonalInfoMandatory())));
        assertTrue(result.getPreferences().contains(String.format("\"thirdPartyTransferPersonalInfoOptional\":{\"isConsentGranted\":%s}", accountInfo.getThirdPartyTransferPersonalInfoOptional())));
        assertTrue(result.getPreferences().contains(String.format("\"collectionAndUsePersonalInfoMandatory\":{\"isConsentGranted\":%s}", accountInfo.getCollectionAndUsePersonalInfoMandatory())));
        assertTrue(result.getPreferences().contains(String.format("\"collectionAndUsePersonalInfoOptional\":{\"isConsentGranted\":%s}", accountInfo.getCollectionAndUsePersonalInfoOptional())));
        assertTrue(result.getPreferences().contains(String.format("\"collectionAndUsePersonalInfoMarketing\":{\"isConsentGranted\":%s}", accountInfo.getCollectionAndUsePersonalInfoMarketing())));
        assertTrue(result.getPreferences().contains(String.format("\"overseasTransferPersonalInfoOptional\":{\"isConsentGranted\":%s}", accountInfo.getOverseasTransferPersonalInfoOptional())));
        assertTrue(result.getPreferences().contains(String.format("\"overseasTransferPersonalInfoMandatory\":{\"isConsentGranted\":%s}", accountInfo.getOverseasTransferPersonalInfoMandatory())));
    }
}
