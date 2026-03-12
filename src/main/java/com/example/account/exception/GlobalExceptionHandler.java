package com.example.account.exception;

import com.example.account.dto.error.ApiErrorResponseDTO;
import com.example.account.i18n.Translator;
import com.example.account.util.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

/**
 * Global exception handler for the application with i18n support.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleAccountNotFoundException(AccountNotFoundException ex) {
        log.error("Account not found: {}", ex.getMessage());
        String accountIdentifier = ExceptionUtils.extractAccountIdentifier(ex);
        String message = Translator.getMessage("error.account.notFound", accountIdentifier);
        
        ApiErrorResponseDTO response = ExceptionUtils.buildErrorResponse(HttpStatus.BAD_REQUEST, message, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        Map<String, String> errors = ExceptionUtils.processValidationErrors(
                ex.getBindingResult().getAllErrors().toArray(new org.springframework.validation.ObjectError[0]));
        
        String message = Translator.getMessage("error.validation.failed");
        ApiErrorResponseDTO response = ExceptionUtils.buildErrorResponse(HttpStatus.BAD_REQUEST, message, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("Resource not found: {}", ex.getResourcePath());
        String message = Translator.getMessage("error.resource.notFound", ex.getResourcePath());
        
        ApiErrorResponseDTO response = ExceptionUtils.buildErrorResponse(HttpStatus.NOT_FOUND, message, null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDTO> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        String message = Translator.getMessage("error.internal.server");
        
        ApiErrorResponseDTO response = ExceptionUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

