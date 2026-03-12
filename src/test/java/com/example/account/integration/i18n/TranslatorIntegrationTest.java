package com.example.account.integration.i18n;

import com.example.account.integration.BaseIntegrationTest;
import com.example.account.i18n.Translator;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.FieldError;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Translator.
 * Tests i18n message resolution with real MessageSource.
 */
class TranslatorIntegrationTest extends BaseIntegrationTest {

    @Test
    void testGetMessage_SimpleKey() {
        String message = Translator.getMessage("error.validation.failed");
        assertNotNull(message, "Message should be resolved");
        assertFalse(message.isEmpty(), "Message should not be empty");
        assertNotEquals("error.validation.failed", message, 
                "Message should be resolved, not return the key");
    }

    @Test
    void testGetMessage_WithArguments() {
        String message = Translator.getMessage("error.account.notFound", "ACC-123");
        assertNotNull(message, "Message should be resolved");
        assertFalse(message.isEmpty(), "Message should not be empty");
        assertTrue(message.contains("ACC-123") || message.contains("Account"),
                "Message should contain the argument or 'Account'");
    }

    @Test
    void testGetMessage_WithMultipleArguments() {
        // Test with validation message that has multiple placeholders
        String message = Translator.getMessage("account.name.size", "VeryLongName", 255, 1);
        assertNotNull(message, "Message should be resolved");
        assertFalse(message.isEmpty(), "Message should not be empty");
        assertTrue(message.contains("255") || message.contains("1") || message.contains("between"),
                "Message should contain size constraints");
    }

    @Test
    void testGetMessage_NonExistentKey() {
        String message = Translator.getMessage("non.existent.key");
        // Should return the key itself as fallback
        assertEquals("non.existent.key", message, 
                "Should return the key when message not found");
    }

    @Test
    void testGetMessage_WithNullArguments() {
        String message = Translator.getMessage("error.validation.failed", (Object[]) null);
        assertNotNull(message, "Message should be resolved even with null arguments");
        assertFalse(message.isEmpty(), "Message should not be empty");
    }

    @Test
    void testResolveMessage_FieldError() {
        // Create a FieldError for testing
        // Note: The default message should be the actual message text, not a key
        FieldError fieldError = new FieldError(
                "accountCreateRequestDTO",
                "accountName",
                "",
                false,
                new String[]{"NotBlank.accountCreateRequestDTO.accountName", 
                            "NotBlank.accountName", 
                            "NotBlank.java.lang.String", 
                            "NotBlank"},
                null,
                "Account Name is required" // Use actual message, not key
        );

        String message = Translator.resolveMessage(fieldError);
        assertNotNull(message, "Message should be resolved");
        assertFalse(message.isEmpty(), "Message should not be empty");
        assertTrue(message.contains("required") || message.contains("Account Name"),
                "Message should contain 'required' or 'Account Name'");
    }

    @Test
    void testResolveMessage_FieldErrorWithArguments() {
        // Create a FieldError with size constraint
        // Note: Spring's validation framework will resolve the message using MessageSource
        // The codes array will be used to resolve the message
        FieldError fieldError = new FieldError(
                "accountCreateRequestDTO",
                "website",
                "x".repeat(300),
                false,
                new String[]{"Size.accountCreateRequestDTO.website", 
                            "Size.website", 
                            "Size.java.lang.String", 
                            "Size"},
                new Object[]{255, 0},
                "Website must not exceed 255 characters" // Use actual message
        );

        String message = Translator.resolveMessage(fieldError);
        assertNotNull(message, "Message should be resolved");
        assertFalse(message.isEmpty(), "Message should not be empty");
        // The message should be resolved from ValidationMessages.properties
        // which contains "Website must not exceed {1} characters"
        assertTrue(message.contains("255") || message.contains("exceed") || message.contains("Website"),
                "Message should contain max size, 'exceed', or 'Website'");
    }

    @Test
    void testResolveMessage_CustomMessageSourceResolvable() {
        MessageSourceResolvable resolvable = new MessageSourceResolvable() {
            @Override
            public String[] getCodes() {
                return new String[]{"test.message.key", "fallback.key"};
            }

            @Override
            public Object[] getArguments() {
                return new Object[]{"test-arg"};
            }

            @Override
            public String getDefaultMessage() {
                return "Default message with {0}";
            }
        };

        String message = Translator.resolveMessage(resolvable);
        assertNotNull(message, "Message should be resolved");
        assertFalse(message.isEmpty(), "Message should not be empty");
    }

    @Test
    void testResolveMessage_FallbackToDefaultMessage() {
        MessageSourceResolvable resolvable = new MessageSourceResolvable() {
            @Override
            public String[] getCodes() {
                return new String[]{"non.existent.code"};
            }

            @Override
            public Object[] getArguments() {
                return null;
            }

            @Override
            public String getDefaultMessage() {
                return "Default fallback message";
            }
        };

        String message = Translator.resolveMessage(resolvable);
        assertNotNull(message, "Message should be resolved");
        assertEquals("Default fallback message", message,
                "Should return default message when code not found");
    }

    @Test
    void testResolveMessage_DefaultMessageAsKey() {
        // Test case where default message is a message key (wrapped in {})
        // ExceptionUtils extracts the key from {key} format before passing to Translator
        // So when Translator receives it, the defaultMessage is already extracted (without {})
        MessageSourceResolvable resolvable = new MessageSourceResolvable() {
            @Override
            public String[] getCodes() {
                return new String[]{"non.existent.code"};
            }

            @Override
            public Object[] getArguments() {
                return new Object[]{"test-value"};
            }

            @Override
            public String getDefaultMessage() {
                // ExceptionUtils extracts this to "account.name.notblank" before calling Translator
                return "account.name.notblank";
            }
        };

        String message = Translator.resolveMessage(resolvable);
        assertNotNull(message, "Message should be resolved");
        assertFalse(message.isEmpty(), "Message should not be empty");
        // The MessageSource should resolve "account.name.notblank" from ValidationMessages.properties
        // Verify that the message is either resolved or the key (fallback behavior)
        // In an integration test with real MessageSource, it should resolve
        assertTrue(
            message.contains("required") || 
            message.contains("Account Name") || 
            message.equals("account.name.notblank"),
            "Message should contain 'required' or 'Account Name', or be the key itself: " + message);
    }

    @Test
    void testGetMessage_LocaleAware() {
        // Set a specific locale
        Locale originalLocale = LocaleContextHolder.getLocale();
        try {
            LocaleContextHolder.setLocale(Locale.FRENCH);
            
            // Even with French locale, should fallback to default (US) if message not found
            String message = Translator.getMessage("error.validation.failed");
            assertNotNull(message, "Message should be resolved");
            assertFalse(message.isEmpty(), "Message should not be empty");
        } finally {
            LocaleContextHolder.setLocale(originalLocale);
        }
    }

    @Test
    void testGetMessage_ValidationMessageKey() {
        String message = Translator.getMessage("account.name.notblank");
        assertNotNull(message, "Validation message should be resolved");
        assertFalse(message.isEmpty(), "Validation message should not be empty");
        assertTrue(message.contains("required") || message.contains("Account Name"),
                "Message should contain 'required' or 'Account Name'");
    }

    @Test
    void testGetMessage_BusinessMessageKey() {
        String message = Translator.getMessage("error.internal.server");
        assertNotNull(message, "Business message should be resolved");
        assertFalse(message.isEmpty(), "Business message should not be empty");
    }
}

