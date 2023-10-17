package com.thermofisher.cdcam.utils.cdc;


import com.thermofisher.cdcam.model.cdc.China;
import com.thermofisher.cdcam.model.cdc.Japan;
import com.thermofisher.cdcam.model.cdc.Korea;
import com.thermofisher.cdcam.model.cdc.Registration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


@ActiveProfiles("test")
@ExtendWith(value = SpringExtension.class)
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
    public void getInterest_IfChinaObjectIsNotNull_ShouldReturnInterest() {
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
    public void getPhoneNumber_IfChinaObjectIsNotNull_ShouldReturnPhoneNumber() {
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
    public void getReceiveMarketingInformation_IfKoreaObjectIsNull_ShouldReturnReceiveMarketingInformationNull() {
        // given
        Korea mockKorea = null;
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean receiveMarketingInformation = registrationAttributesHandler.getReceiveMarketingInformation();

        // then
        assertNull(receiveMarketingInformation);
    }

    @Test
    public void getReceiveMarketingInformation_IfKoreaObjectIsNotNull_ShouldReturnReceiveMarketingInformation() {
        // given
        Korea mockKorea = Korea.builder().receiveMarketingInformation(true).build();
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean receiveMarketingInformation = registrationAttributesHandler.getReceiveMarketingInformation();

        // then
        assertEquals(receiveMarketingInformation, mockKorea.getReceiveMarketingInformation());
    }

    @Test
    public void getThirdPartyTransferPersonalInfoMandatory_IfKoreaObjectIsNull_ShouldReturnThirdPartyTransferPersonalInfoMandatoryNull() {
        // given
        Korea mockKorea = null;
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean thirdPartyTransferPersonalInfoMandatory = registrationAttributesHandler.getThirdPartyTransferPersonalInfoMandatory();

        // then
        assertNull(thirdPartyTransferPersonalInfoMandatory);
    }

    @Test
    public void getThirdPartyTransferPersonalInfoMandatory_IfKoreaObjectIsNotNull_ShouldReturnThirdPartyTransferPersonalInfoMandatory() {
        // given
        Korea mockKorea = Korea.builder().thirdPartyTransferPersonalInfoMandatory(true).build();
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean thirdPartyTransferPersonalInfoMandatory = registrationAttributesHandler.getThirdPartyTransferPersonalInfoMandatory();

        // then
        assertEquals(thirdPartyTransferPersonalInfoMandatory, mockKorea.getThirdPartyTransferPersonalInfoMandatory());
    }

    @Test
    public void getThirdPartyTransferPersonalInfoOptional_IfKoreaObjectIsNull_ShouldReturnThirdPartyTransferPersonalInfoOptionalNull() {
        // given
        Korea mockKorea = null;
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean thirdPartyTransferPersonalInfoOptional = registrationAttributesHandler.getThirdPartyTransferPersonalInfoOptional();

        // then
        assertNull(thirdPartyTransferPersonalInfoOptional);
    }

    @Test
    public void getThirdPartyTransferPersonalInfoOptional_IfKoreaObjectIsNotNull_ShouldReturnThirdPartyTransferPersonalInfoOptional() {
        // given
        Korea mockKorea = Korea.builder().thirdPartyTransferPersonalInfoOptional(true).build();
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean thirdPartyTransferPersonalInfoOptional = registrationAttributesHandler.getThirdPartyTransferPersonalInfoOptional();

        // then
        assertEquals(thirdPartyTransferPersonalInfoOptional, mockKorea.getThirdPartyTransferPersonalInfoOptional());
    }

    @Test
    public void getCollectionAndUsePersonalInfoMandatory_IfKoreaObjectIsNull_ShouldReturnCollectionAndUsePersonalInfoMandatoryNull() {
        // given
        Korea mockKorea = null;
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean collectionAndUsePersonalInfoMandatory = registrationAttributesHandler.getCollectionAndUsePersonalInfoMandatory();

        // then
        assertNull(collectionAndUsePersonalInfoMandatory);
    }

    @Test
    public void getCollectionAndUsePersonalInfoMandatory_IfKoreaObjectIsNotNull_ShouldReturnCollectionAndUsePersonalInfoMandatory() {
        // given
        Korea mockKorea = Korea.builder().collectionAndUsePersonalInfoMandatory(true).build();
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean collectionAndUsePersonalInfoMandatory = registrationAttributesHandler.getCollectionAndUsePersonalInfoMandatory();

        // then
        assertEquals(collectionAndUsePersonalInfoMandatory, mockKorea.getCollectionAndUsePersonalInfoMandatory());
    }

    @Test
    public void getCollectionAndUsePersonalInfoOptional_IfKoreaObjectIsNull_ShouldReturnCollectionAndUsePersonalInfoOptionalNull() {
        // given
        Korea mockKorea = null;
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean collectionAndUsePersonalInfoOptional = registrationAttributesHandler.getCollectionAndUsePersonalInfoOptional();

        // then
        assertNull(collectionAndUsePersonalInfoOptional);
    }

    @Test
    public void getCollectionAndUsePersonalInfoOptional_IfKoreaObjectIsNotNull_ShouldReturnCollectionAndUsePersonalInfoOptional() {
        // given
        Korea mockKorea = Korea.builder().collectionAndUsePersonalInfoOptional(true).build();
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean collectionAndUsePersonalInfoOptional = registrationAttributesHandler.getCollectionAndUsePersonalInfoOptional();

        // then
        assertEquals(collectionAndUsePersonalInfoOptional, mockKorea.getCollectionAndUsePersonalInfoOptional());
    }

    @Test
    public void getCollectionAndUsePersonalInfoMarketing_IfKoreaObjectIsNull_ShouldReturnCollectionAndUsePersonalInfoMarketingNull() {
        // given
        Korea mockKorea = null;
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean collectionAndUsePersonalInfoMarketing = registrationAttributesHandler.getCollectionAndUsePersonalInfoMarketing();

        // then
        assertNull(collectionAndUsePersonalInfoMarketing);
    }

    @Test
    public void getCollectionAndUsePersonalInfoMarketing_IfKoreaObjectIsNotNull_ShouldReturnCollectionAndUsePersonalInfoMarketing() {
        // given
        Korea mockKorea = Korea.builder().collectionAndUsePersonalInfoMarketing(true).build();
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean collectionAndUsePersonalInfoMarketing = registrationAttributesHandler.getCollectionAndUsePersonalInfoMarketing();

        // then
        assertEquals(collectionAndUsePersonalInfoMarketing, mockKorea.getCollectionAndUsePersonalInfoMarketing());
    }

    @Test
    public void getOverseasTransferPersonalInfoMandatory_IfKoreaObjectIsNull_ShouldReturnOverseasTransferPersonalInfoMandatoryNull() {
        // given
        Korea mockKorea = null;
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean overseasTransferPersonalInfoMandatory = registrationAttributesHandler.getOverseasTransferPersonalInfoMandatory();

        // then
        assertNull(overseasTransferPersonalInfoMandatory);
    }

    @Test
    public void getOverseasTransferPersonalInfoMandatory_IfKoreaObjectIsNotNull_ShouldReturnOverseasTransferPersonalInfoMandatory() {
        // given
        Korea mockKorea = Korea.builder().overseasTransferPersonalInfoMandatory(true).build();
        Registration registration = Registration.builder().korea(mockKorea).build();
        registrationAttributesHandler = new RegistrationAttributesHandler(registration);

        // when
        Boolean overseasTransferPersonalInfoMandatory = registrationAttributesHandler.getOverseasTransferPersonalInfoMandatory();

        // then
        assertEquals(overseasTransferPersonalInfoMandatory, mockKorea.getOverseasTransferPersonalInfoMandatory());
    }

}
