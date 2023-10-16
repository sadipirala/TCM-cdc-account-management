package com.thermofisher.cdcam.models;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.model.OptionalRequiredConstraint;
import com.thermofisher.cdcam.model.OptionalRequiredConstraintValidator;
import jakarta.validation.ConstraintViolation;
import lombok.Builder;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
/*
@Import({OptionalRequiredConstraintValidator.class})
@SpringBootTest
@SpringJUnitConfig
//@ContextConfiguration(locations = "file:src/test/resources/application-test.properties_1")
//@ContextConfiguration( classes = {OptionalRequiredConstraintValidatorTests.WebConfig.class})
//@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
public class OptionalRequiredConstraintValidatorTests {

   @Autowired
    private LocalValidatorFactoryBean validator;

    @Autowired
    private ApplicationContext applicationContext;
    //@Autowired
    //private SpringConstraintValidatorFactory validatorFactory;
    @BeforeEach
    public void initialize() {
        SpringConstraintValidatorFactory validatorFactory = new SpringConstraintValidatorFactory(applicationContext.getAutowireCapableBeanFactory());
        validator = new LocalValidatorFactoryBean();
        validator.setConstraintValidatorFactory(validatorFactory);
        validator.setApplicationContext(applicationContext);
        validator.afterPropertiesSet();
        MockitoAnnotations.openMocks(this);

    }


   // @Getter
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

       public Boolean getMandatory() {
           return isMandatory;
       }

       public TestClass() {

       }

       public TestClass(Boolean isMandatory, String optionalField) {
           this.isMandatory = isMandatory;
           this.optionalField = optionalField;
       }

       public String getOptionalField() {
           return optionalField;
       }

       public void setMandatory(Boolean mandatory) {
           isMandatory = mandatory;
       }

       public void setOptionalField(String optionalField) {
           this.optionalField = optionalField;
       }
   }

    @Test
    public void validate_givenRequiredFieldNotMatchingRequiredBooleanValue_andOptionalFieldNull_shouldBeValid() {
        // Given.
        TestClass testClass = new TestClass();
        testClass.isMandatory = false;
        testClass.optionalField=null;


        // When.
        Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);

        // Then.
        Assertions.assertEquals(0, violations.size());
    }

    @Test
    public void validate_givenRequiredFieldNotMatchingRequiredBooleanValue_andOptionalFieldEmpty_shouldBeValid() {
        // Given.
        TestClass testClass = new TestClass();
        testClass.isMandatory = false;
        testClass.optionalField="";

        // When.
        Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);

        // Then.
        Assertions.assertEquals(0, violations.size());
    }

    @Test
    public void validate_givenRequiredFieldMatchingRequiredBooleanValue_andOptionalFieldNull_shouldBeInvalid() {
        // Given.
        TestClass testClass = new TestClass();
        testClass.isMandatory = true;
        testClass.optionalField=null;

        // When.
        Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);

        // Then.
        Assertions.assertEquals(1, violations.size());
    }

    @Test
    public void validate_givenRequiredFieldMatchingRequiredBooleanValue_andOptionalFieldEmpty_shouldBeInvalid() {
        // Given.
        TestClass testClass = new TestClass();
        testClass.isMandatory = true;
        testClass.optionalField=" ";
        // When.
        Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);

        // Then.
        Assertions.assertEquals(1, violations.size());
    }

}*/

