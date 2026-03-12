package com.example.account.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for ISO 3166-1 alpha-3 country codes (3-letter codes).
 * Validates that the country code is a valid 3-letter ISO country code.
 */
@Documented
@Constraint(validatedBy = CountryCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCountryCode {
    
    String message() default "{account.countryCode.invalid}";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}

