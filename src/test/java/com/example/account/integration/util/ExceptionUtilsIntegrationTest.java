package com.example.account.integration.util;

import com.example.account.dto.error.ApiErrorResponseDTO;
import com.example.account.integration.BaseIntegrationTest;
import com.example.account.util.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ExceptionUtils.
 * Tests validation error message resolution with real MessageSource.
 */
class ExceptionUtilsIntegrationTest extends BaseIntegrationTest {

    private Validator validator;

    @org.springframework.beans.factory.annotation.Autowired
    public void setValidator(org.springframework.validation.Validator validator) {
        if (validator instanceof LocalValidatorFactoryBean) {
            this.validator = ((LocalValidatorFactoryBean) validator).getValidator();
        }
    }

    @Test
    void testResolveValidationErrorMessage_NotBlank() {
        // Create a test object with validation error
        TestDTO dto = new TestDTO();
        dto.setName(""); // Empty name should trigger NotBlank

        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Should have validation violations");

        // Convert to FieldError
        ConstraintViolation<TestDTO> violation = violations.iterator().next();
        FieldError fieldError = createFieldError("name", "", violation);

        // Test message resolution
        String message = ExceptionUtils.resolveValidationErrorMessage(fieldError);
        assertNotNull(message, "Message should be resolved");
        assertFalse(message.isEmpty(), "Message should not be empty");
        assertTrue(message.contains("required") || message.contains("Account Name"),
                "Message should contain 'required' or 'Account Name'");
    }

    @Test
    void testResolveValidationErrorMessage_Email() {
        // Create a test object with invalid email
        TestDTO dto = new TestDTO();
        dto.setName("Test");
        dto.setEmail("invalid-email"); // Invalid email

        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Should have validation violations");

        // Find email violation
        ConstraintViolation<TestDTO> emailViolation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("email"))
                .findFirst()
                .orElse(null);
        assertNotNull(emailViolation, "Should have email validation violation");

        FieldError fieldError = createFieldError("email", "invalid-email", emailViolation);

        // Test message resolution
        String message = ExceptionUtils.resolveValidationErrorMessage(fieldError);
        assertNotNull(message, "Message should be resolved");
        assertFalse(message.isEmpty(), "Message should not be empty");
        assertTrue(message.contains("invalid-email") || message.contains("Email"),
                "Message should contain rejected value or 'Email'");
    }

    @Test
    void testResolveValidationErrorMessage_Size() {
        // Create a test object with size violation
        TestDTO dto = new TestDTO();
        dto.setName("Test");
        dto.setWebsite("x".repeat(300)); // Exceeds max size

        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Should have validation violations");

        // Find size violation
        ConstraintViolation<TestDTO> sizeViolation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("website"))
                .findFirst()
                .orElse(null);
        assertNotNull(sizeViolation, "Should have size validation violation");

        FieldError fieldError = createFieldError("website", dto.getWebsite(), sizeViolation);

        // Test message resolution
        String message = ExceptionUtils.resolveValidationErrorMessage(fieldError);
        assertNotNull(message, "Message should be resolved");
        assertFalse(message.isEmpty(), "Message should not be empty");
        assertTrue(message.contains("255") || message.contains("exceed"),
                "Message should contain max size or 'exceed'");
    }

    @Test
    void testProcessValidationErrors_MultipleErrors() {
        // Create a test object with multiple validation errors
        TestDTO dto = new TestDTO();
        dto.setName(""); // NotBlank violation
        dto.setEmail("invalid"); // Email violation
        dto.setWebsite("x".repeat(300)); // Size violation

        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Should have validation violations");

        // Convert to FieldError array
        org.springframework.validation.ObjectError[] errors = violations.stream()
                .map(v -> createFieldError(
                        v.getPropertyPath().toString(),
                        v.getInvalidValue(),
                        v))
                .toArray(org.springframework.validation.ObjectError[]::new);

        // Test processing
        Map<String, String> errorMap = ExceptionUtils.processValidationErrors(errors);
        assertNotNull(errorMap, "Error map should not be null");
        assertFalse(errorMap.isEmpty(), "Error map should not be empty");
        assertTrue(errorMap.size() >= 2, "Should have at least 2 errors");

        // Verify each error has a resolved message
        errorMap.values().forEach(message -> {
            assertNotNull(message, "Each error should have a resolved message");
            assertFalse(message.isEmpty(), "Each error message should not be empty");
        });
    }

    @Test
    void testBuildErrorResponse_WithErrors() {
        Map<String, String> errors = Map.of(
                "name", "Account Name is required",
                "email", "Email must be valid"
        );

        ApiErrorResponseDTO response = ExceptionUtils.buildErrorResponse(
                HttpStatus.BAD_REQUEST, "Validation failed", errors);

        assertNotNull(response, "Response should not be null");
        assertEquals(400, response.getStatus(), "Status should be 400");
        assertEquals("Validation failed", response.getMessage(), "Message should match");
        assertNotNull(response.getErrors(), "Errors should not be null");
        assertEquals(2, response.getErrors().size(), "Should have 2 errors");
        assertEquals("Account Name is required", response.getErrors().get("name"));
        assertEquals("Email must be valid", response.getErrors().get("email"));
    }

    @Test
    void testBuildErrorResponse_WithoutErrors() {
        ApiErrorResponseDTO response = ExceptionUtils.buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", null);

        assertNotNull(response, "Response should not be null");
        assertEquals(500, response.getStatus(), "Status should be 500");
        assertEquals("Internal server error", response.getMessage(), "Message should match");
        assertNull(response.getErrors(), "Errors should be null when not provided");
    }

    @Test
    void testBuildErrorResponse_WithEmptyErrors() {
        Map<String, String> errors = Map.of();

        ApiErrorResponseDTO response = ExceptionUtils.buildErrorResponse(
                HttpStatus.BAD_REQUEST, "Validation failed", errors);

        assertNotNull(response, "Response should not be null");
        assertEquals(400, response.getStatus(), "Status should be 400");
        assertNull(response.getErrors(), "Errors should be null when empty");
    }

    @Test
    void testExtractAccountIdentifier_WithAccountIdString() {
        com.example.account.exception.AccountNotFoundException ex = 
                new com.example.account.exception.AccountNotFoundException("123");

        String identifier = ExceptionUtils.extractAccountIdentifier(ex);
        assertEquals("123", identifier, "Should extract account ID");
    }

    @Test
    void testExtractAccountIdentifier_WithAccountId() {
        com.example.account.exception.AccountNotFoundException ex = 
                new com.example.account.exception.AccountNotFoundException("ACC-000001");

        String identifier = ExceptionUtils.extractAccountIdentifier(ex);
        assertEquals("ACC-000001", identifier, "Should extract account reference ID");
    }

    @Test
    void testExtractAccountIdentifier_WithMessage() {
        com.example.account.exception.AccountNotFoundException ex = 
                new com.example.account.exception.AccountNotFoundException("Account not found");
        // Should extract message as fallback
        String identifier = ExceptionUtils.extractAccountIdentifier(ex);
        assertNotNull(identifier, "Should extract message as fallback");
        assertEquals("Account not found", identifier, "Should return the message");
    }

    /**
     * Helper method to create FieldError from ConstraintViolation.
     */
    private FieldError createFieldError(String fieldName, Object rejectedValue, 
                                        ConstraintViolation<?> violation) {
        return new FieldError(
                "testObject",
                fieldName,
                rejectedValue,
                false,
                violation.getMessageTemplate() != null ? 
                        new String[]{violation.getMessageTemplate()} : 
                        new String[]{"validation.error"},
                null,
                violation.getMessage()
        );
    }

    /**
     * Test DTO for validation testing.
     */
    static class TestDTO {
        @NotBlank(message = "{account.name.notblank}")
        private String name;

        @Email(message = "{account.email.email}")
        private String email;

        @Size(max = 255, message = "{account.website.size}")
        private String website;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }
    }
}

