package com.thermofisher.cdcam.model;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {OptionalRequiredWhenTrueConstraintValidator.class})
public @interface OptionalRequiredWhenTrueConstraint {
    String message() default "Parameter required when required field is true.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String optionalField();
    String requiredBooleanField();

    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        OptionalRequiredWhenTrueConstraint[] value();
    }
}
