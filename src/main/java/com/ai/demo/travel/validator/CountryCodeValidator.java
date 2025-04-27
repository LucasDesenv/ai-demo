package com.ai.demo.travel.validator;

import com.ai.demo.utils.CountryCodesUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CountryCodeValidator implements ConstraintValidator<ValidCountryCode, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return CountryCodesUtil.ISO_COUNTRIES.containsKey(value);
    }

}
