package com.example.account.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validator for ISO 4217 currency codes (3-letter codes).
 * Uses Java's built-in Currency class to validate currency codes.
 */
public class CurrencyCodeValidator implements ConstraintValidator<ValidCurrencyCode, String> {

    // Build set of valid ISO 4217 currency codes from Java's Currency class
    private static final Set<String> VALID_CURRENCY_CODES = Currency.getAvailableCurrencies()
            .stream()
            .map(Currency::getCurrencyCode)
            .collect(Collectors.toUnmodifiableSet());

    @Override
    public void initialize(ValidCurrencyCode constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String currencyCode, ConstraintValidatorContext context) {
        // Allow null/empty values (use @NotNull/@NotBlank if required)
        if (StringUtils.isBlank(currencyCode)) {
            return true;
        }

        // Must be exactly 3 characters
        if (currencyCode.length() != 3) {
            return false;
        }

        // Convert to uppercase for validation
        String upperCode = currencyCode.toUpperCase();

        // Check if all characters are letters
        if (!upperCode.matches("[A-Z]{3}")) {
            return false;
        }

        // Check if it's a valid ISO currency code using Java's built-in Currency
        return VALID_CURRENCY_CODES.contains(upperCode);
    }
}

