package com.example.account.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for ISO 4217 currency codes (3-letter codes).
 * Validates that the currency code is a valid 3-letter ISO currency code.
 */
@Documented
@Constraint(validatedBy = CurrencyCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrencyCode {
    
    String message() default "{account.currencyCode.invalid}";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}

