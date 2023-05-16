package com.thermofisher.cdcam.models;

import com.thermofisher.cdcam.model.OptionalRequiredConstraintValidator;
import com.thermofisher.cdcam.model.dto.ConsentDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

import javax.validation.ConstraintViolation;
import java.util.Set;

@RunWith(SpringRunner.class)
@Import({OptionalRequiredConstraintValidator.class})
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
public class OptionalRequiredConstraintValidatorTests {

    private LocalValidatorFactoryBean validator;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Before
    public void initialize() {
        SpringConstraintValidatorFactory validatorFactory = new SpringConstraintValidatorFactory(applicationContext.getAutowireCapableBeanFactory());
        validator = new LocalValidatorFactoryBean();
        validator.setConstraintValidatorFactory(validatorFactory);
        validator.setApplicationContext(applicationContext);
        validator.afterPropertiesSet();
    }

    @Test
    public void ConsentDTO_isValid_givenNullMarketingConsent_shouldBeInvalid() {
        // Given.
        ConsentDTO consentDTO = ConsentDTO.builder()
                .uid("abc123")
                .city("Carlsbad")
                .company("Thermo Fisher Scientific")
                .marketingConsent(null)
                .build();

        // When.
        Set<ConstraintViolation<ConsentDTO>> violations = validator.validate(consentDTO);

        // Then.
        Assert.assertEquals(1, violations.size());
    }

    @Test
    public void ConsentDTO_isValid_givenNullUid_shouldBeInvalid() {
        // Given.
        ConsentDTO consentDTO = ConsentDTO.builder()
                .uid(null)
                .city("Carlsbad")
                .company("Thermo Fisher Scientific")
                .marketingConsent(false)
                .build();

        // When.
        Set<ConstraintViolation<ConsentDTO>> violations = validator.validate(consentDTO);

        // Then.
        Assert.assertEquals(1, violations.size());
    }

    @Test
    public void ConsentDTO_isValid_givenEmptyUid_shouldBeInvalid() {
        // Given.
        ConsentDTO consentDTO = ConsentDTO.builder()
                .uid(" ")
                .city("Carlsbad")
                .company("Thermo Fisher Scientific")
                .marketingConsent(false)
                .build();

        // When.
        Set<ConstraintViolation<ConsentDTO>> violations = validator.validate(consentDTO);

        // Then.
        Assert.assertEquals(1, violations.size());
    }

    @Test
    public void ConsentDTO_isValid_givenNullCityAndCompany_whenMarketingConsentTrue_shouldBeInvalid() {
        // Given.
        ConsentDTO consentDTO = ConsentDTO.builder()
                .uid("abc123")
                .city(null)
                .company(null)
                .marketingConsent(true)
                .build();

        // When.
        Set<ConstraintViolation<ConsentDTO>> violations = validator.validate(consentDTO);

        // Then.
        Assert.assertEquals(2, violations.size());
    }

    @Test
    public void ConsentDTO_isValid_givenEmptyCityAndCompany_whenMarketingConsentTrue_shouldBeInvalid() {
        // Given.
        ConsentDTO consentDTO = ConsentDTO.builder()
                .uid("abc123")
                .city(" ")
                .company(" ")
                .marketingConsent(true)
                .build();

        // When.
        Set<ConstraintViolation<ConsentDTO>> violations = validator.validate(consentDTO);

        // Then.
        Assert.assertEquals(2, violations.size());
    }

    @Test
    public void ConsentDTO_isValid_givenNullCityAndCompany_whenMarketingConsentFalse_shouldBeValid() {
        // Given.
        ConsentDTO consentDTO = ConsentDTO.builder()
                .uid("abc123")
                .city(null)
                .company(null)
                .marketingConsent(false)
                .build();

        // When.
        Set<ConstraintViolation<ConsentDTO>> violations = validator.validate(consentDTO);

        // Then.
        Assert.assertEquals(0, violations.size());
    }

    @Test
    public void ConsentDTO_isValid_givenEmptyCityAndCompany_whenMarketingConsentFalse_shouldBeValid() {
        // Given.
        ConsentDTO consentDTO = ConsentDTO.builder()
                .uid("abc123")
                .city(" ")
                .company(" ")
                .marketingConsent(false)
                .build();

        // When.
        Set<ConstraintViolation<ConsentDTO>> violations = validator.validate(consentDTO);

        // Then.
        Assert.assertEquals(0, violations.size());
    }
}
