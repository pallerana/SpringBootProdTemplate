package com.example.account.dto.account;

import com.example.account.dto.account.request.AccountCreateRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccountCreateRequestDTO validation.
 * Tests i18n validation error messages.
 */
class AccountCreateRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidRequest_AllFields() {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .countryCode("USA")
                .currencyCode("USD")
                .website("https://www.example.com")
                .country("United States")
                .addressLine1("123 Main St")
                .addressLine2("Suite 100")
                .city("New York")
                .state("NY")
                .zipcode("10001")
                .build();

        // Act
        Set<ConstraintViolation<AccountCreateRequestDTO>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void testValidRequest_RequiredFieldsOnly() {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .build();

        // Act
        Set<ConstraintViolation<AccountCreateRequestDTO>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "Valid request with required fields should have no violations");
    }

    @Test
    void testInvalidRequest_MissingAccountName() {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("")
                .email("test@example.com")
                .build();

        // Act
        Set<ConstraintViolation<AccountCreateRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Missing account name should cause validation error");
        
        // Find NotBlank violation specifically (empty string triggers both NotBlank and Size)
        ConstraintViolation<AccountCreateRequestDTO> notBlankViolation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("accountName"))
                .filter(v -> v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName().equals("NotBlank"))
                .findFirst()
                .orElse(null);
        
        // If NotBlank not found, get any accountName violation
        ConstraintViolation<AccountCreateRequestDTO> violation = notBlankViolation != null ? 
                notBlankViolation : 
                violations.stream()
                    .filter(v -> v.getPropertyPath().toString().equals("accountName"))
                    .findFirst()
                    .orElse(null);
        
        assertNotNull(violation, "Should have validation error for accountName field");
        // Check that message key is used (wrapped in {}) or the actual resolved message
        String message = violation.getMessage();
        // The message could be a key (wrapped in {}) or the resolved message
        // For NotBlank, it should be "Account Name is required" or the key "{account.name.notblank}"
        assertTrue(
            (message.startsWith("{") && message.endsWith("}")) || 
            message.contains("Account Name is required") ||
            (message.contains("Account Name") && message.contains("required")),
            "Error message should be from i18n: " + message);
    }

    @Test
    void testInvalidRequest_InvalidEmail() {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("invalid-email")
                .build();

        // Act
        Set<ConstraintViolation<AccountCreateRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Invalid email should cause validation error");
        
        ConstraintViolation<AccountCreateRequestDTO> violation = violations.stream()
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
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .countryCode("XX")
                .build();

        // Act
        Set<ConstraintViolation<AccountCreateRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Invalid country code should cause validation error");
        
        ConstraintViolation<AccountCreateRequestDTO> violation = violations.stream()
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
    void testInvalidRequest_InvalidCurrencyCode() {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .currencyCode("ABC")  // Invalid currency code (not in ISO 4217)
                .build();

        // Act
        Set<ConstraintViolation<AccountCreateRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Invalid currency code should cause validation error");
        
        ConstraintViolation<AccountCreateRequestDTO> violation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("currencyCode"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(violation, "Should have validation error for currencyCode field");
        String message = violation.getMessage();
        assertTrue(message.startsWith("{") && message.endsWith("}") || 
                   message.contains("Currency Code"),
                "Error message should be from i18n: " + message);
    }

    @Test
    void testInvalidRequest_SizeExceeded() {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .website("https://www." + "a".repeat(300) + ".com") // Exceeds 255 characters
                .build();

        // Act
        Set<ConstraintViolation<AccountCreateRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "Size exceeded should cause validation error");
        
        ConstraintViolation<AccountCreateRequestDTO> violation = violations.stream()
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




