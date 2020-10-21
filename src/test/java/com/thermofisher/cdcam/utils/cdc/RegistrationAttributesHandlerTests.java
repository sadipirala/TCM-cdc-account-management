package com.thermofisher.cdcam.utils.cdc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.model.cdc.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * RegistrationAttributesHandlerTests
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class RegistrationAttributesHandlerTests {

    private RegistrationAttributesHandler registrationAttributesHandler;

    @Test
    public void getHiraganaName_IfJapanObjectIsNull_ShouldReturnHiraganaNameNull() {
        // given
        Japan mockJapan = null;
        Registration registration = Registration.builder().japan(mockJapan).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        String hiraganaName = registrationAttributesHandler.getHiraganaName();

        // then
        assertNull(hiraganaName);
    }

    @Test
    public void getHiraganaName_IfJapanObjectIsNotNull_ShouldReturnHiraganaName() {
        // given
        Japan mockJapan = Japan.builder().hiraganaName("Test hiragana").build();
        Registration registration = Registration.builder().japan(mockJapan).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        String hiraganaName = registrationAttributesHandler.getHiraganaName();

        // then
        assertEquals(hiraganaName, mockJapan.getHiraganaName());
    }

    @Test
    public void getJobRole_IfChinaObjectIsNull_ShouldReturnReturnJobRoleNull() {
        // given
        China mockChina = null;
        Registration registration = Registration.builder().china(mockChina).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        String jobRole = registrationAttributesHandler.getJobRole();

        // then
        assertNull(jobRole);
    }

    @Test
    public void getJobRole_IfChinaObjectIsNotNull_ShouldReturnJobRole() {
        // given
        China mockChina = China.builder().jobRole("China Job Role").build();
        Registration registration = Registration.builder().china(mockChina).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        String jobRole = registrationAttributesHandler.getJobRole();

        // then
        assertEquals(jobRole, mockChina.getJobRole());
    }

    @Test
    public void getInterest_IfChinaObjectIsNull_ShouldReturnReturnInterestNull() {
        // given
        China mockChina = null;
        Registration registration = Registration.builder().china(mockChina).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        String interest = registrationAttributesHandler.getInterest();

        // then
        assertNull(interest);
    }

    @Test
    public void getInterest_IfChinaObjectIsNotNull_ShouldReturnInterest(){
        // given
        China mockChina = China.builder().interest("China Interest").build();
        Registration registration = Registration.builder().china(mockChina).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        String interest = registrationAttributesHandler.getInterest();

        // then
        assertEquals(interest, mockChina.getInterest());
    }

    @Test
    public void getPhoneNumber_IfChinaObjectIsNull_ShouldReturnReturnPhoneNumberNull() {
        // given
        China mockChina = null;
        Registration registration = Registration.builder().china(mockChina).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        String phoneNumber = registrationAttributesHandler.getPhoneNumber();

        // then
        assertNull(phoneNumber);
    }

    @Test
    public void getPhoneNumber_IfChinaObjectIsNotNull_ShouldReturnPhoneNumber(){
        // given
        China mockChina = China.builder().phoneNumber("6648675309").build();
        Registration registration = Registration.builder().china(mockChina).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        String phoneNumber = registrationAttributesHandler.getPhoneNumber();

        // then
        assertEquals(phoneNumber, mockChina.getPhoneNumber());
    }

    @Test
    public void getEcommerceTransaction_IfKoreaObjectIsNull_ShouldReturnEcommerceTransactionNull(){
        // given
        Korea mockKorea = null;
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean eCommerceTransaction = registrationAttributesHandler.getEcomerceTransaction();

        // then
        assertNull(eCommerceTransaction);
    }

    @Test
    public void getEcommerceTransaction_IfKoreaObjectIsNotNull_ShouldReturnEcommerceTransaction(){
        // given
        Korea mockKorea = Korea.builder().eComerceTransaction(true).build();
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean eCommerceTransaction = registrationAttributesHandler.getEcomerceTransaction();

        // then
        assertEquals(eCommerceTransaction, mockKorea.getEComerceTransaction());
    }

    @Test
    public void getPersonalInfoMandatory_IfKoreaObjectIsNull_ShouldReturnPersonalInfoMandatoryNull(){
        // given
        Korea mockKorea = null;
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean personalInfoMandatory = registrationAttributesHandler.getPersonalInfoMandatory();

        // then
        assertNull(personalInfoMandatory);
    }

    @Test
    public void getPersonalInfoMandatory_IfKoreaObjectIsNotNull_ShouldReturnPersonalInfoMandatory(){
        // given
        Korea mockKorea = Korea.builder().personalInfoMandatory(true).build();
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean personalInfoMandatory = registrationAttributesHandler.getPersonalInfoMandatory();

        // then
        assertEquals(personalInfoMandatory, mockKorea.getPersonalInfoMandatory());
    }

    @Test
    public void getPersonalInfoOptional_IfKoreaObjectIsNull_ShouldReturnPersonalInfoOptionalNull(){
        // given
        Korea mockKorea = null;
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean personalInfoOptional = registrationAttributesHandler.getPersonalInfoOptional();

        // then
        assertNull(personalInfoOptional);
    }

    @Test
    public void getPersonalInfoOptional_IfKoreaObjectIsNotNull_ShouldReturnPersonalInfoOptional(){
        // given
        Korea mockKorea = Korea.builder().personalInfoOptional(true).build();
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean personalInfoOptional = registrationAttributesHandler.getPersonalInfoOptional();

        // then
        assertEquals(personalInfoOptional, mockKorea.getPersonalInfoOptional());
    }

    @Test
    public void getPrivateInfoOptional_IfKoreaObjectIsNull_ShouldReturnPrivateInfoOptionalNull(){
        // given
        Korea mockKorea = null;
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean privateInfoOptional = registrationAttributesHandler.getPrivateInfoOptional();

        // then
        assertNull(privateInfoOptional);
    }

    @Test
    public void getPrivateInfoOptional_IfKoreaObjectIsNotNull_ShouldReturnPrivateInfoOptional(){
        // given
        Korea mockKorea = Korea.builder().privateInfoOptional(true).build();
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean privateInfoOptional = registrationAttributesHandler.getPrivateInfoOptional();

        // then
        assertEquals(privateInfoOptional, mockKorea.getPrivateInfoOptional());
    }

    @Test
    public void getPrivateInfoMandatory_IfKoreaObjectIsNull_ShouldReturnPrivateInfoMandatoryNull(){
        // given
        Korea mockKorea = null;
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean privateInfoMandatory = registrationAttributesHandler.getPrivateInfoMandatory();

        // then
        assertNull(privateInfoMandatory);
    }

    @Test
    public void getPrivateInfoMandatory_IfKoreaObjectIsNotNull_ShouldReturnPrivateInfoMandatory(){
        // given
        Korea mockKorea = Korea.builder().privateInfoMandatory(true).build();
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean privateInfoMandatory = registrationAttributesHandler.getPrivateInfoMandatory();

        // then
        assertEquals(privateInfoMandatory, mockKorea.getPrivateInfoMandatory());
    }

    @Test
    public void getProcessingConsignment_IfKoreaObjectIsNull_ShouldReturnProcessingConsignmentNull(){
        // given
        Korea mockKorea = null;
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean processingConsignment = registrationAttributesHandler.getProcessingConsignment();

        // then
        assertNull(processingConsignment);
    }

    @Test
    public void getProcessingConsignment_IfKoreaObjectIsNotNull_ShouldReturnProcessingConsignment(){
        // given
        Korea mockKorea = Korea.builder().processingConsignment(true).build();
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean processingConsignment = registrationAttributesHandler.getProcessingConsignment();

        // then
        assertEquals(processingConsignment, mockKorea.getProcessingConsignment());
    }

    @Test
    public void getTermsOfUse_IfKoreaObjectIsNull_ShouldReturnTermsOfUseNull(){
        // given
        Korea mockKorea = null;
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean termsOfUse = registrationAttributesHandler.getTermsOfUse();

        // then
        assertNull(termsOfUse);
    }

    @Test
    public void getTermsOfUse_IfKoreaObjectIsNotNull_ShouldReturnTermsOfUse(){
        // given
        Korea mockKorea = Korea.builder().termsOfUse(true).build();
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean termsOfUse = registrationAttributesHandler.getTermsOfUse();

        // then
        assertEquals(termsOfUse, mockKorea.getTermsOfUse());
    }
}
