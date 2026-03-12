package com.example.account.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Configuration for MessageSource to support both ValidationMessages.properties
 * and i18n/messages.properties.
 */
@Configuration
public class MessageSourceConfig {

    /**
     * Configures MessageSource to load both ValidationMessages.properties
     * (for validation messages) and i18n/messages.properties (for business messages).
     * 
     * @return Configured MessageSource bean
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        
        // Set basenames for both ValidationMessages and i18n/messages
        // Spring will try each basename in order when resolving messages
        // ValidationMessages is checked first for validation messages
        messageSource.setBasenames(
            "classpath:ValidationMessages",  // For validation messages (account.name.notblank, etc.) - checked first
            "classpath:i18n/messages"         // For business error messages (error.account.notFound, etc.)
        );
        
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // Cache for 1 hour
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(false); // Don't use code as default message
        
        return messageSource;
    }
}

