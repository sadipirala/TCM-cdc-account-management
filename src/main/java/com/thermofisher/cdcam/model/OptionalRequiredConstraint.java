package com.thermofisher.cdcam.model;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {OptionalRequiredConstraintValidator.class})
public @interface OptionalRequiredConstraint {
    String message() default "Parameter required when required field is true.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String optionalField();

    String requiredField();

    boolean requiredBooleanValue();

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        OptionalRequiredConstraint[] value();
    }
}
