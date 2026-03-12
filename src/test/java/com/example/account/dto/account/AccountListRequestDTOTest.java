package com.example.account.dto.account;

import com.example.account.dto.account.request.AccountListRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccountListRequestDTO validation.
 * Tests i18n validation error messages.
 */
class AccountListRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidRequest_AllFields() {
        // Arrange
        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .accountName("Test Account")
                .accountId("ACC-000001")
                .currency("USD")
                .countryCode("USA")
                .city("New York")
                .state("NY")
                .zipcode("10001")
                .status("ACTIVE")
                .pageNumber(1)
                .pageSize(25)
                .build();

        // Act
        Set<ConstraintViolation<AccountListRequestDTO>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void testValidRequest_NoFilters() {
        // Arrange
        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .pageNumber(1)
                .pageSize(25)
                .build();

        // Act
        Set<ConstraintViolation<AccountListRequestDTO>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Valid request with no filters should have no violations");
    }

    @Test
    void testInvalidRequest_InvalidPageNumber() {
        // Arrange
        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .pageNumber(0)  // Invalid: must be >= 1
                .pageSize(25)
                .build();

        // Act
        Set<ConstraintViolation<AccountListRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Invalid page number should cause validation error");
        
        ConstraintViolation<AccountListRequestDTO> violation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("pageNumber"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(violation, "Should have validation error for pageNumber field");
        String message = violation.getMessage();
        assertTrue(message.startsWith("{") && message.endsWith("}") || 
                   message.contains("Page number"),
                "Error message should be from i18n: " + message);
    }

    @Test
    void testInvalidRequest_InvalidPageSize() {
        // Arrange
        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .pageNumber(1)
                .pageSize(0)  // Invalid: must be >= 1
                .build();

        // Act
        Set<ConstraintViolation<AccountListRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Invalid page size should cause validation error");
        
        ConstraintViolation<AccountListRequestDTO> violation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("pageSize"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(violation, "Should have validation error for pageSize field");
        String message = violation.getMessage();
        assertTrue(message.startsWith("{") && message.endsWith("}") || 
                   message.contains("Page size"),
                "Error message should be from i18n: " + message);
    }

    @Test
    void testInvalidRequest_InvalidCountryCode() {
        // Arrange
        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .countryCode("XX")  // Invalid country code
                .pageNumber(1)
                .pageSize(25)
                .build();

        // Act
        Set<ConstraintViolation<AccountListRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Invalid country code should cause validation error");
        
        ConstraintViolation<AccountListRequestDTO> violation = violations.stream()
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
    void testInvalidRequest_InvalidCurrency() {
        // Arrange
        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .currency("ABC")  // Invalid currency code (not in ISO 4217)
                .pageNumber(1)
                .pageSize(25)
                .build();

        // Act
        Set<ConstraintViolation<AccountListRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Invalid currency code should cause validation error");
        
        ConstraintViolation<AccountListRequestDTO> violation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("currency"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(violation, "Should have validation error for currency field");
        String message = violation.getMessage();
        assertTrue(message.startsWith("{") && message.endsWith("}") || 
                   message.contains("Currency Code"),
                "Error message should be from i18n: " + message);
    }
}

