package com.thermofisher.cdcam.utils.cdc;

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
    public void buildNewCDCAccount_GivenAccountInfoContainsHiraganaName_ThenCDCAccountShouldContainHiraganaName() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        String hiraganaName = accountInfo.getHiraganaName();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"hiraganaName\":\"%s\"", hiraganaName)));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoContainsJobRole_ThenCDCAccountShouldContainJobRole() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        String jobRole = accountInfo.getJobRole();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"jobRole\":\"%s\"", jobRole)));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoContainsInterest_ThenCDCAccountShouldContainInterest() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        String interest = accountInfo.getInterest();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"interest\":\"%s\"", interest)));
    }

    @Test
    public void buildNewCDCAccount_GivenChinaAccountContainsPhoneNumberAndIsMember_ThenCDCAccountShouldContainPhoneNumber() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setCountry("cn");
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
        accountInfo.setCountry("cn");
        accountInfo.setMember("false");
        String phoneNumber = accountInfo.getPhoneNumber();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertFalse(result.getData().contains(String.format("\"phoneNumber\":\"%s\"", phoneNumber)));
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
    public void buildNewCDCAccount_GivenAspireAccountContainsDepartmentAndIsNotAMember_ThenCDCAccountShouldNotContainDepartment() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setMember("false");
        String department = accountInfo.getDepartment();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertFalse(result.getProfile().contains(String.format("\"department\":\"%s\"", department)));
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
    public void buildNewCDCAccount_GivenAccountInfoContainsEcommerceTransaction_ThenCDCAccountShouldContainEcommerceTransaction() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        Boolean eCommerceTransaction = accountInfo.getECommerceTransaction();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"eComerceTransaction\":%s", eCommerceTransaction)));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoContainsPersonalInfoMandatory_ThenCDCAccountShouldContainPersonalInfoMandatory() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        Boolean personalInfoMandatory = accountInfo.getPersonalInfoMandatory();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"personalInfoMandatory\":%s", personalInfoMandatory)));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoContainsPersonalInfoOptional_ThenCDCAccountShouldContainPersonalInfoOptional() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        Boolean personalInfoOptional = accountInfo.getPersonalInfoOptional();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"personalInfoOptional\":%s", personalInfoOptional)));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoContainsPrivateInfoMandatory_ThenCDCAccountShouldContainPrivateInfoMandatory() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        Boolean privateInfoMandatory = accountInfo.getPrivateInfoMandatory();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"privateInfoMandatory\":%s", privateInfoMandatory)));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoContainsPrivateInfoOptional_ThenCDCAccountShouldContainPrivateInfoOptional() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        Boolean privateInfoOptional = accountInfo.getPrivateInfoOptional();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"privateInfoOptional\":%s", privateInfoOptional)));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoContainsProcessingConsignment_ThenCDCAccountShouldContainProcessingConsignment() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        Boolean processingConsignment = accountInfo.getProcessingConsignment();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"processingConsignment\":%s", processingConsignment)));
    }

    @Test
    public void buildNewCDCAccount_GivenAccountInfoContainsTermsOfUse_ThenCDCAccountShouldContainTermsOfUse() throws JSONException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        Boolean termsOfUse = accountInfo.getTermsOfUse();

        // when
        CDCNewAccount result = CDCAccountsHandler.buildCDCNewAccount(accountInfo);

        // then
        assertTrue(result.getData().contains(String.format("\"termsOfUse\":%s", termsOfUse)));
    }
}
