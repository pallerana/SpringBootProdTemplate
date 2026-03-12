package com.example.account.dto.account;

import com.example.account.dto.account.request.AccountUpdateRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccountUpdateRequestDTO validation.
 * Tests i18n validation error messages.
 */
class AccountUpdateRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidRequest_AllFields() {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Updated Account")
                .email("updated@example.com")
                .countryCode("USA")
                .currencyCode("USD")
                .website("https://www.example.com")
                .country("United States")
                .addressLine1("123 Main St")
                .addressLine2("Suite 100")
                .city("New York")
                .state("NY")
                .zipcode("10001")
                .status("ACTIVE")
                .build();

        // Act
        Set<ConstraintViolation<AccountUpdateRequestDTO>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void testValidRequest_PartialUpdate() {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .email("updated@example.com")
                .build();

        // Act
        Set<ConstraintViolation<AccountUpdateRequestDTO>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Valid partial update should have no violations");
    }

    @Test
    void testInvalidRequest_InvalidEmail() {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .email("invalid-email")
                .build();

        // Act
        Set<ConstraintViolation<AccountUpdateRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Invalid email should cause validation error");
        
        ConstraintViolation<AccountUpdateRequestDTO> violation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("email"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(violation, "Should have validation error for email field");
        String message = violation.getMessage();
        assertTrue(message.startsWith("{") && message.endsWith("}") || 
                   message.contains("Email") && message.contains("must be valid"),
                "Error message should be from i18n: " + message);
    }

    @Test
    void testInvalidRequest_InvalidCountryCode() {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .countryCode("XX")
                .build();

        // Act
        Set<ConstraintViolation<AccountUpdateRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Invalid country code should cause validation error");
        
        ConstraintViolation<AccountUpdateRequestDTO> violation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("countryCode"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(violation, "Should have validation error for countryCode field");
        String message = violation.getMessage();
        assertTrue(message.startsWith("{") && message.endsWith("}") || 
                   message.contains("Country Code"),
                "Error message should be from i18n: " + message);
    }

    @Test
    void testInvalidRequest_SizeExceeded() {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .website("https://www." + "a".repeat(300) + ".com")  // Exceeds 255 characters
                .build();

        // Act
        Set<ConstraintViolation<AccountUpdateRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Size exceeded should cause validation error");
        
        ConstraintViolation<AccountUpdateRequestDTO> violation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("website"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(violation, "Should have validation error for website field");
        String message = violation.getMessage();
        assertTrue(message.startsWith("{") && message.endsWith("}") || 
                   message.contains("Website") && message.contains("exceed"),
                "Error message should be from i18n: " + message);
    }
}

