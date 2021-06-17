package com.thermofisher.cdcam.utils.cdc;

import com.thermofisher.cdcam.enums.CountryCodes;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.Utils;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
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
    public void buildNewCDCAccount_GivenAspireAccountContainsCompanyAndIsNotAMember_ThenCDCAccountShouldNotContainCompany() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setMember("false");
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
        accountInfo.setMember("false");
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
        accountInfo.setMember("false");
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
        accountInfo.setMember("false");
        String phoneNumber = accountInfo.getPhoneNumber();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertFalse(result.getData().contains(String.format("\"phoneNumber\":\"%s\"", phoneNumber)));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoHasJapanAsCountry_ThenCDCAccountShouldContainJapanObject() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
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
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
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
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setCountry(CountryCodes.CHINA.getValue());
        accountInfo.setMember("true");
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
        accountInfo.setMember("false");
        String phoneNumber = accountInfo.getPhoneNumber();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertFalse(result.getData().contains(String.format("\"phoneNumber\":\"%s\"", phoneNumber)));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoHasKoreaAsCountry_ThenCDCAccountShouldKoreaObject() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
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
}
