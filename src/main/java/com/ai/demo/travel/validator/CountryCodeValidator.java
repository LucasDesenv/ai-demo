package com.ai.demo.travel.validator;

import com.ai.demo.utils.CountryCodesUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CountryCodeValidator implements ConstraintValidator<ValidCountryCode, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return CountryCodesUtil.ISO_COUNTRIES.containsKey(value);
    }

}
