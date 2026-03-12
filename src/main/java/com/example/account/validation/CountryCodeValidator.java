package com.example.account.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Validator for ISO 3166-1 alpha-3 country codes (3-letter codes).
 * Uses Java's built-in Locale class to validate country codes.
 */
public class CountryCodeValidator implements ConstraintValidator<ValidCountryCode, String> {

    // Build set of valid ISO 3166-1 alpha-3 country codes from Java's Locale
    private static final Set<String> VALID_COUNTRY_CODES = Stream.of(Locale.getISOCountries())
            .map(countryCode -> {
                try {
                    Locale locale = new Locale("", countryCode);
                    return locale.getISO3Country();
                } catch (Exception e) {
                    // If ISO3 country code cannot be retrieved, skip it
                    return null;
                }
            })
            .filter(code -> code != null && !code.isEmpty())
            .collect(Collectors.toUnmodifiableSet());

    @Override
    public void initialize(ValidCountryCode constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String countryCode, ConstraintValidatorContext context) {
        // Allow null/empty values (use @NotNull/@NotBlank if required)
        if (StringUtils.isBlank(countryCode)) {
            return true;
        }

        // Must be exactly 3 characters
        if (countryCode.length() != 3) {
            return false;
        }

        // Convert to uppercase for validation
        String upperCode = countryCode.toUpperCase();

        // Check if all characters are letters
        if (!upperCode.matches("[A-Z]{3}")) {
            return false;
        }

        // Check if it's a valid ISO country code using Java's built-in Locale
        return VALID_COUNTRY_CODES.contains(upperCode);
    }
}

