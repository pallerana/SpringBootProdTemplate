package com.example.account.integration.config;

import com.example.account.integration.BaseIntegrationTest;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for configuration classes.
 * Verifies that all configuration beans are properly loaded and configured.
 */
class ConfigIntegrationTest extends BaseIntegrationTest {

    @Autowired(required = false)
    private MessageSource messageSource;

    @Autowired(required = false)
    private OpenAPI openAPI;

    @Test
    void testMessageSourceConfig_BeanLoaded() {
        assertNotNull(messageSource, "MessageSource bean should be loaded");
    }

    @Test
    void testMessageSourceConfig_LoadsValidationMessages() {
        assertNotNull(messageSource, "MessageSource bean should be loaded");
        
        // Test that validation messages are loaded
        String message = messageSource.getMessage("account.name.notblank", null, null);
        assertNotNull(message, "Validation message should be loaded");
        assertFalse(message.isEmpty(), "Validation message should not be empty");
        assertNotEquals("account.name.notblank", message, "Message should be resolved, not return the key");
    }

    @Test
    void testMessageSourceConfig_LoadsBusinessMessages() {
        assertNotNull(messageSource, "MessageSource bean should be loaded");
        
        // Test that business messages are loaded
        String message = messageSource.getMessage("error.account.notFound", new Object[]{"TEST-123"}, null);
        assertNotNull(message, "Business error message should be loaded");
        assertFalse(message.isEmpty(), "Business error message should not be empty");
        assertTrue(message.contains("TEST-123") || message.contains("Account"), 
                "Message should contain account identifier or 'Account'");
    }

    @Test
    void testMessageSourceConfig_FallbackToDefaultLocale() {
        assertNotNull(messageSource, "MessageSource bean should be loaded");
        
        // Test that messages fallback to default locale when not found in current locale
        String message = messageSource.getMessage("account.name.notblank", null, java.util.Locale.FRENCH);
        assertNotNull(message, "Message should fallback to default locale");
        assertFalse(message.isEmpty(), "Message should not be empty");
    }

    @Test
    void testOpenAPIConfig_BeanLoaded() {
        assertNotNull(openAPI, "OpenAPI bean should be loaded");
    }

    @Test
    void testOpenAPIConfig_InfoConfigured() {
        assertNotNull(openAPI, "OpenAPI bean should be loaded");
        assertNotNull(openAPI.getInfo(), "OpenAPI info should be configured");
        assertNotNull(openAPI.getInfo().getTitle(), "OpenAPI title should be set");
        assertNotNull(openAPI.getInfo().getVersion(), "OpenAPI version should be set");
    }

    @Test
    void testOpenAPIConfig_ServersConfigured() {
        assertNotNull(openAPI, "OpenAPI bean should be loaded");
        assertNotNull(openAPI.getServers(), "OpenAPI servers should be configured");
        assertFalse(openAPI.getServers().isEmpty(), "At least one server should be configured");
    }

    @Test
    void testOpenAPIConfig_SecuritySchemeConfigured() {
        assertNotNull(openAPI, "OpenAPI bean should be loaded");
        assertNotNull(openAPI.getComponents(), "OpenAPI components should be configured");
        assertNotNull(openAPI.getComponents().getSecuritySchemes(), 
                "OpenAPI security schemes should be configured");
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("Bearer Authentication"),
                "Bearer Authentication security scheme should be configured");
    }
}

