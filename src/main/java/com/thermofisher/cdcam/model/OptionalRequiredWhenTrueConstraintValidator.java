package com.thermofisher.cdcam.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OptionalRequiredWhenTrueConstraintValidator
    implements ConstraintValidator<OptionalRequiredWhenTrueConstraint, Object> {

    private String optionalField;
    private String requiredBooleanField;

    @Override
    public void initialize(OptionalRequiredWhenTrueConstraint constraint) {
        this.optionalField = constraint.optionalField();
        this.requiredBooleanField = constraint.requiredBooleanField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object fieldValue = new BeanWrapperImpl(value).getPropertyValue(optionalField);
        Object requiredBooleanFieldValue = new BeanWrapperImpl(value).getPropertyValue(requiredBooleanField);

        assert requiredBooleanFieldValue != null;

        if (requiredBooleanFieldValue.equals(false)) {
            return true;
        }

        return !StringUtils.isBlank((CharSequence) fieldValue);
    }
}
