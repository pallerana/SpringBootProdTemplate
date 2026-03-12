package com.example.account.util;

import com.example.account.dto.error.ApiErrorResponseDTO;
import com.example.account.exception.AccountNotFoundException;
import com.example.account.i18n.Translator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExceptionUtils.
 * Tests i18n message resolution and error response building.
 */
@ExtendWith(MockitoExtension.class)
class ExceptionUtilsTest {

    private MockedStatic<Translator> mockedTranslator;
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        messageSource = mock(MessageSource.class);
        mockedTranslator = mockStatic(Translator.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedTranslator != null) {
            mockedTranslator.close();
        }
    }

    @Test
    void testResolveValidationErrorMessage_WithRejectedValue() {
        // Arrange
        FieldError fieldError = createFieldError("email", "invalid-email", 
                new String[]{"Email.accountCreateRequestDTO.email", "Email.email", "Email"},
                new Object[]{new DefaultMessageSourceResolvable(new String[]{"email"}, "email"), ".*"},
                "{account.email.email}");
        
        mockedTranslator.when(() -> Translator.resolveMessage(any(MessageSourceResolvable.class)))
                .thenReturn("Email invalid-email must be valid");

        // Act
        String result = ExceptionUtils.resolveValidationErrorMessage(fieldError);

        // Assert
        assertEquals("Email invalid-email must be valid", result);
        mockedTranslator.verify(() -> Translator.resolveMessage(any(MessageSourceResolvable.class)));
    }

    @Test
    void testResolveValidationErrorMessage_SizeConstraint() {
        // Arrange
        FieldError fieldError = createFieldError("zipcode", "very-long-zipcode",
                new String[]{"Size.accountCreateRequestDTO.zipcode", "Size.zipcode", "Size"},
                new Object[]{new DefaultMessageSourceResolvable(new String[]{"zipcode"}, "zipcode"), 20, 0},
                "{account.zipcode.size}");
        
        mockedTranslator.when(() -> Translator.resolveMessage(any(MessageSourceResolvable.class)))
                .thenReturn("Zipcode must not exceed 20 characters");

        // Act
        String result = ExceptionUtils.resolveValidationErrorMessage(fieldError);

        // Assert
        assertEquals("Zipcode must not exceed 20 characters", result);
    }

    @Test
    void testProcessValidationErrors() {
        // Arrange
        FieldError fieldError1 = createFieldError("accountName", "",
                new String[]{"NotBlank.accountCreateRequestDTO.accountName"},
                new Object[]{new DefaultMessageSourceResolvable(new String[]{"accountName"}, "accountName")},
                "{account.name.notblank}");
        
        FieldError fieldError2 = createFieldError("email", "invalid",
                new String[]{"Email.accountCreateRequestDTO.email"},
                new Object[]{new DefaultMessageSourceResolvable(new String[]{"email"}, "email")},
                "{account.email.email}");
        
        ObjectError[] errors = new ObjectError[]{fieldError1, fieldError2};
        
        mockedTranslator.when(() -> Translator.resolveMessage(any(MessageSourceResolvable.class)))
                .thenReturn("Account Name is required")
                .thenReturn("Email invalid must be valid");

        // Act
        Map<String, String> result = ExceptionUtils.processValidationErrors(errors);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Account Name is required", result.get("accountName"));
        assertEquals("Email invalid must be valid", result.get("email"));
    }

    @Test
    void testBuildErrorResponse_WithErrors() {
        // Arrange
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "Validation failed";
        Map<String, String> errors = new HashMap<>();
        errors.put("accountName", "Account Name is required");
        errors.put("email", "Email invalid must be valid");

        // Act
        ApiErrorResponseDTO result = ExceptionUtils.buildErrorResponse(status, message, errors);

        // Assert
        assertNotNull(result);
        assertEquals(400, result.getStatus());
        assertEquals("Validation failed", result.getMessage());
        assertNotNull(result.getTimestamp());
        assertEquals(2, result.getErrors().size());
        assertEquals("Account Name is required", result.getErrors().get("accountName"));
        assertEquals("Email invalid must be valid", result.getErrors().get("email"));
    }

    @Test
    void testBuildErrorResponse_WithoutErrors() {
        // Arrange
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "An internal server error occurred";

        // Act
        ApiErrorResponseDTO result = ExceptionUtils.buildErrorResponse(status, message, null);

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getStatus());
        assertEquals("An internal server error occurred", result.getMessage());
        assertNull(result.getErrors());
    }

    @Test
    void testExtractAccountIdentifier_WithAccountIdString() {
        // Arrange
        AccountNotFoundException ex = new AccountNotFoundException("1");

        // Act
        String result = ExceptionUtils.extractAccountIdentifier(ex);

        // Assert
        assertEquals("1", result);
    }

    @Test
    void testExtractAccountIdentifier_WithAccountId() {
        // Arrange
        AccountNotFoundException ex = new AccountNotFoundException("ACC-000001");

        // Act
        String result = ExceptionUtils.extractAccountIdentifier(ex);

        // Assert
        assertEquals("ACC-000001", result);
    }

    @Test
    void testExtractAccountIdentifier_WithMessage() {
        // Arrange
        AccountNotFoundException ex = new AccountNotFoundException("Account not found");

        // Act
        String result = ExceptionUtils.extractAccountIdentifier(ex);

        // Assert
        assertEquals("Account not found", result);
    }

    private FieldError createFieldError(String field, Object rejectedValue, String[] codes, 
                                       Object[] arguments, String defaultMessage) {
        return new FieldError("accountCreateRequestDTO", field, rejectedValue, false, codes, 
                arguments, defaultMessage);
    }
}

