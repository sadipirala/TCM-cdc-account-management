package com.thermofisher.cdcam.models;

import com.thermofisher.cdcam.model.OptionalRequiredConstraint;
import com.thermofisher.cdcam.model.OptionalRequiredConstraintValidator;
import lombok.Builder;
import lombok.Getter;
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

    @Builder
    @Getter
    @OptionalRequiredConstraint.List({
            @OptionalRequiredConstraint(
                    optionalField = "optionalField",
                    requiredField = "isMandatory",
                    requiredBooleanValue = true
            )
    })
    public static class TestClass {
        private Boolean isMandatory;
        private String optionalField;
    }

    @Test
    public void validate_givenRequiredFieldNotMatchingRequiredBooleanValue_andOptionalFieldNull_shouldBeValid() {
        // Given.
        TestClass testClass = TestClass.builder()
                .isMandatory(false)
                .optionalField(null)
                .build();

        // When.
        Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);

        // Then.
        Assert.assertEquals(0, violations.size());
    }

    @Test
    public void validate_givenRequiredFieldNotMatchingRequiredBooleanValue_andOptionalFieldEmpty_shouldBeValid() {
        // Given.
        TestClass testClass = TestClass.builder()
                .isMandatory(false)
                .optionalField(" ")
                .build();

        // When.
        Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);

        // Then.
        Assert.assertEquals(0, violations.size());
    }

    @Test
    public void validate_givenRequiredFieldMatchingRequiredBooleanValue_andOptionalFieldNull_shouldBeInvalid() {
        // Given.
        TestClass testClass = TestClass.builder()
                .isMandatory(true)
                .optionalField(null)
                .build();

        // When.
        Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);

        // Then.
        Assert.assertEquals(1, violations.size());
    }

    @Test
    public void validate_givenRequiredFieldMatchingRequiredBooleanValue_andOptionalFieldEmpty_shouldBeInvalid() {
        // Given.
        TestClass testClass = TestClass.builder()
                .isMandatory(true)
                .optionalField(" ")
                .build();

        // When.
        Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);

        // Then.
        Assert.assertEquals(1, violations.size());
    }
}
