package com.example.account.exception;

import com.example.account.dto.error.ApiErrorResponseDTO;
import com.example.account.i18n.Translator;
import com.example.account.util.ExceptionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests i18n error message handling.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private MockedStatic<Translator> mockedTranslator;
    private MockedStatic<ExceptionUtils> mockedExceptionUtils;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        mockedTranslator = mockStatic(Translator.class);
        mockedExceptionUtils = mockStatic(ExceptionUtils.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedTranslator != null) {
            mockedTranslator.close();
        }
        if (mockedExceptionUtils != null) {
            mockedExceptionUtils.close();
        }
    }

    @Test
    void testHandleAccountNotFoundException() {
        // Arrange
        AccountNotFoundException ex = new AccountNotFoundException("ACC-000001");
        String accountIdentifier = "ACC-000001";
        String errorMessage = "Account not found with ID: ACC-000001";
        
        ApiErrorResponseDTO errorResponse = ApiErrorResponseDTO.builder()
                .status(400)
                .message(errorMessage)
                .timestamp("2024-01-01T00:00:00Z")
                .build();

        mockedExceptionUtils.when(() -> ExceptionUtils.extractAccountIdentifier(ex))
                .thenReturn(accountIdentifier);
        mockedTranslator.when(() -> Translator.getMessage(eq("error.account.notFound"), any(Object[].class)))
                .thenReturn(errorMessage);
        mockedExceptionUtils.when(() -> ExceptionUtils.buildErrorResponse(eq(HttpStatus.BAD_REQUEST), 
                eq(errorMessage), isNull()))
                .thenReturn(errorResponse);

        // Act
        ResponseEntity<ApiErrorResponseDTO> result = globalExceptionHandler.handleAccountNotFoundException(ex);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals(400, result.getBody().getStatus());
        assertEquals(errorMessage, result.getBody().getMessage());
        
        mockedExceptionUtils.verify(() -> ExceptionUtils.extractAccountIdentifier(ex));
        mockedTranslator.verify(() -> Translator.getMessage(eq("error.account.notFound"), any(Object[].class)));
    }

    @Test
    void testHandleValidationExceptions() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        
        FieldError fieldError1 = new FieldError("accountCreateRequestDTO", "accountName", "",
                false, new String[]{"NotBlank.accountCreateRequestDTO.accountName"}, null, "{account.name.notblank}");
        FieldError fieldError2 = new FieldError("accountCreateRequestDTO", "email", "invalid",
                false, new String[]{"Email.accountCreateRequestDTO.email"}, null, "{account.email.email}");
        
        org.springframework.validation.ObjectError[] errors = new org.springframework.validation.ObjectError[]{
                fieldError1, fieldError2
        };

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(java.util.Arrays.asList(errors));

        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("accountName", "Account Name is required");
        errorMap.put("email", "Email invalid must be valid");
        
        String validationMessage = "Validation failed";
        ApiErrorResponseDTO errorResponse = ApiErrorResponseDTO.builder()
                .status(400)
                .message(validationMessage)
                .errors(errorMap)
                .timestamp("2024-01-01T00:00:00Z")
                .build();

        mockedExceptionUtils.when(() -> ExceptionUtils.processValidationErrors(any()))
                .thenReturn(errorMap);
        mockedTranslator.when(() -> Translator.getMessage("error.validation.failed"))
                .thenReturn(validationMessage);
        mockedExceptionUtils.when(() -> ExceptionUtils.buildErrorResponse(eq(HttpStatus.BAD_REQUEST), 
                eq(validationMessage), eq(errorMap)))
                .thenReturn(errorResponse);

        // Act
        ResponseEntity<ApiErrorResponseDTO> result = globalExceptionHandler.handleValidationExceptions(ex);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals(400, result.getBody().getStatus());
        assertEquals(validationMessage, result.getBody().getMessage());
        assertNotNull(result.getBody().getErrors());
        assertEquals(2, result.getBody().getErrors().size());
        assertEquals("Account Name is required", result.getBody().getErrors().get("accountName"));
        assertEquals("Email invalid must be valid", result.getBody().getErrors().get("email"));
        
        mockedExceptionUtils.verify(() -> ExceptionUtils.processValidationErrors(any()));
        mockedTranslator.verify(() -> Translator.getMessage("error.validation.failed"));
    }

    @Test
    void testHandleNoResourceFoundException() {
        // Arrange
        NoResourceFoundException ex = mock(NoResourceFoundException.class);
        String resourcePath = "/api/v1/api/v1/accounts";
        when(ex.getResourcePath()).thenReturn(resourcePath);
        
        String errorMessage = "The requested resource '/api/v1/api/v1/accounts' was not found. Please check the URL and try again.";
        
        ApiErrorResponseDTO errorResponse = ApiErrorResponseDTO.builder()
                .status(404)
                .message(errorMessage)
                .timestamp("2024-01-01T00:00:00Z")
                .build();

        mockedTranslator.when(() -> Translator.getMessage(eq("error.resource.notFound"), any(Object[].class)))
                .thenReturn(errorMessage);
        mockedExceptionUtils.when(() -> ExceptionUtils.buildErrorResponse(eq(HttpStatus.NOT_FOUND), 
                eq(errorMessage), isNull()))
                .thenReturn(errorResponse);

        // Act
        ResponseEntity<ApiErrorResponseDTO> result = globalExceptionHandler.handleNoResourceFoundException(ex);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals(404, result.getBody().getStatus());
        assertEquals(errorMessage, result.getBody().getMessage());
        
        mockedTranslator.verify(() -> Translator.getMessage(eq("error.resource.notFound"), any(Object[].class)));
        mockedExceptionUtils.verify(() -> ExceptionUtils.buildErrorResponse(eq(HttpStatus.NOT_FOUND), 
                eq(errorMessage), isNull()));
    }

    @Test
    void testHandleGenericException() {
        // Arrange
        Exception ex = new RuntimeException("Unexpected error");
        String errorMessage = "An internal server error occurred";
        
        ApiErrorResponseDTO errorResponse = ApiErrorResponseDTO.builder()
                .status(500)
                .message(errorMessage)
                .timestamp("2024-01-01T00:00:00Z")
                .build();

        mockedTranslator.when(() -> Translator.getMessage("error.internal.server"))
                .thenReturn(errorMessage);
        mockedExceptionUtils.when(() -> ExceptionUtils.buildErrorResponse(eq(HttpStatus.INTERNAL_SERVER_ERROR), 
                eq(errorMessage), isNull()))
                .thenReturn(errorResponse);

        // Act
        ResponseEntity<ApiErrorResponseDTO> result = globalExceptionHandler.handleGenericException(ex);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals(500, result.getBody().getStatus());
        assertEquals(errorMessage, result.getBody().getMessage());
        
        mockedTranslator.verify(() -> Translator.getMessage("error.internal.server"));
    }
}

