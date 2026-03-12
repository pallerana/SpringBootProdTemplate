package com.example.account.util;

import com.example.account.dto.error.ApiErrorResponseDTO;
import com.example.account.exception.AccountNotFoundException;
import com.example.account.i18n.Translator;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for exception handling and error response building.
 * Leverages Spring's built-in MessageSourceResolvable for i18n message resolution.
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
        // Utility class
    }

    /**
     * Resolves validation error message from a FieldError using Spring's built-in message resolution.
     * FieldError implements MessageSourceResolvable, so Spring's MessageSource can resolve it directly.
     * Includes rejected value in arguments to display invalid values in error messages.
     * 
     * @param fieldError The field error from validation (implements MessageSourceResolvable)
     * @return Resolved error message
     */
    public static String resolveValidationErrorMessage(FieldError fieldError) {
        Object rejectedValue = fieldError.getRejectedValue();
        Object[] originalArgs = fieldError.getArguments();
        
        // Prepare arguments including rejected value as first argument
        Object[] messageArgs = prepareMessageArguments(originalArgs, rejectedValue);
        
        // Create a MessageSourceResolvable wrapper that includes rejected value in arguments
        // Spring's MessageSource will automatically try each code in order until one resolves
        MessageSourceResolvable resolvable = new MessageSourceResolvable() {
            @Override
            public String[] getCodes() {
                return fieldError.getCodes();
            }
            
            @Override
            public Object[] getArguments() {
                return messageArgs;
            }
            
            @Override
            public String getDefaultMessage() {
                String defaultMsg = fieldError.getDefaultMessage();
                // If default message is a message key (wrapped in {}), extract it
                if (defaultMsg != null && defaultMsg.startsWith("{") && defaultMsg.endsWith("}")) {
                    return defaultMsg.substring(1, defaultMsg.length() - 1);
                }
                return defaultMsg;
            }
        };
        
        // Use Spring's MessageSource to resolve the message
        // Spring will try each code in order: Constraint.DTO.field, Constraint.field, Constraint, defaultMessage
        return Translator.resolveMessage(resolvable);
    }

    /**
     * Processes validation errors and returns a map of field names to error messages.
     * 
     * @param fieldErrors Array of field errors from validation
     * @return Map of field names to resolved error messages
     */
    public static Map<String, String> processValidationErrors(org.springframework.validation.ObjectError[] fieldErrors) {
        Map<String, String> errors = new HashMap<>();
        for (org.springframework.validation.ObjectError error : fieldErrors) {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                String fieldName = fieldError.getField();
                String errorMessage = resolveValidationErrorMessage(fieldError);
                errors.put(fieldName, errorMessage != null ? errorMessage : "");
            }
        }
        return errors;
    }

    /**
     * Prepares message arguments by including the rejected value as the first argument.
     * Spring's validation framework passes arguments like: [fieldName (MessageSourceResolvable), value1, value2, ...]
     * We extract the actual values (skipping MessageSourceResolvable) and prepend rejectedValue.
     * 
     * @param originalArgs Original arguments from the validation error
     * @param rejectedValue The rejected value that failed validation
     * @return Array of arguments with rejected value as first element, followed by actual constraint values
     */
    private static Object[] prepareMessageArguments(Object[] originalArgs, Object rejectedValue) {
        if (originalArgs == null || originalArgs.length == 0) {
            // If no original args, just return rejected value if present
            return rejectedValue != null ? new Object[]{rejectedValue} : new Object[0];
        }
        
        // Spring's validation passes: [fieldName (MessageSourceResolvable), constraintValue1, constraintValue2, ...]
        // We need to skip the first MessageSourceResolvable and extract actual values
        int actualValueCount = 0;
        for (int i = 0; i < originalArgs.length; i++) {
            // Skip MessageSourceResolvable objects (field name) and extract actual constraint values
            if (!(originalArgs[i] instanceof MessageSourceResolvable)) {
                actualValueCount++;
            }
        }
        
        // Build new array: [rejectedValue, actualValue1, actualValue2, ...]
        int newLength = (rejectedValue != null ? 1 : 0) + actualValueCount;
        Object[] newArgs = new Object[newLength];
        int index = 0;
        
        // Add rejected value as first argument if present
        if (rejectedValue != null) {
            newArgs[index++] = rejectedValue;
        }
        
        // Add actual constraint values (skip MessageSourceResolvable)
        for (Object arg : originalArgs) {
            if (!(arg instanceof MessageSourceResolvable)) {
                newArgs[index++] = arg;
            }
        }
        
        return newArgs;
    }


    /**
     * Builds an ApiErrorResponseDTO with the given parameters.
     * 
     * @param status HTTP status code
     * @param message Error message
     * @param errors Map of field errors (can be null)
     * @return ApiErrorResponseDTO instance
     */
    public static ApiErrorResponseDTO buildErrorResponse(HttpStatus status, String message, Map<String, String> errors) {
        ApiErrorResponseDTO.ApiErrorResponseDTOBuilder builder = ApiErrorResponseDTO.builder()
                .timestamp(OffsetDateTime.now().toString())
                .status(status.value())
                .message(message);
        
        if (errors != null && !errors.isEmpty()) {
            builder.errors(errors);
        }
        
        return builder.build();
    }

    /**
     * Extracts account identifier from AccountNotFoundException.
     * 
     * @param ex The AccountNotFoundException
     * @return Account identifier as string
     */
    public static String extractAccountIdentifier(AccountNotFoundException ex) {
        if (ex.getAccountId() != null) {
            return ex.getAccountId();
        } else {
            return ex.getMessage();
        }
    }
}

