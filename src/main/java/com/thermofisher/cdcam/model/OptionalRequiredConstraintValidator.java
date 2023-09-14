package com.thermofisher.cdcam.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OptionalRequiredConstraintValidator
    implements ConstraintValidator<OptionalRequiredConstraint, Object> {

    private String optionalField;
    private String requiredField;
    private boolean requiredBooleanValue;

    @Override
    public void initialize(OptionalRequiredConstraint constraint) {
        this.optionalField = constraint.optionalField();
        this.requiredField = constraint.requiredField();
        this.requiredBooleanValue = constraint.requiredBooleanValue();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object optionalFieldValue = new BeanWrapperImpl(value).getPropertyValue(optionalField);
        Object requiredFieldValue = new BeanWrapperImpl(value).getPropertyValue(requiredField);

        // Value that makes conditional fields required is not present.
        if (requiredFieldValue == null) {
            return true;
        }

        if (requiredBooleanValue != (boolean) requiredFieldValue) {
            return true;
        }

        return StringUtils.isNotBlank((String) optionalFieldValue);
    }
}