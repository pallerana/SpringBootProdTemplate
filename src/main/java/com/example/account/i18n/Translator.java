package com.example.account.i18n;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import static java.util.Locale.US;

/**
 * Translator wrapper for i18n message resolution.
 * Provides static methods for easy access to localized messages.
 */
@Component
@Slf4j
public class Translator {

    private static MessageSource messageSource;

    @Autowired
    public Translator(MessageSource messageSource) {
        Translator.messageSource = messageSource;
    }

    /**
     * Get localized message by message code.
     * 
     * @param msgCode Message code/key
     * @return Localized message
     */
    public static String getMessage(String msgCode) {
        String message = "";
        try {
            message = messageSource.getMessage(msgCode, null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            try {
                message = messageSource.getMessage(msgCode, null, US);
            } catch (NoSuchMessageException fallback) {
                log.warn("No message key found: {}", msgCode);
                message = msgCode;
            }
        }
        return message;
    }

    /**
     * Get localized message by message code with arguments.
     * 
     * @param msgCode Message code/key
     * @param args Arguments for message placeholders
     * @return Localized message
     */
    public static String getMessage(String msgCode, Object... args) {
        String message = "";
        try {
            message = messageSource.getMessage(msgCode, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            try {
                message = messageSource.getMessage(msgCode, args, US);
            } catch (NoSuchMessageException fallback) {
                log.warn("No message key found: {}", msgCode);
                message = msgCode;
            }
        }
        return message;
    }

    /**
     * Resolves a message using Spring's MessageSourceResolvable mechanism.
     * This leverages Spring's built-in message code resolution which tries codes in order.
     * 
     * @param resolvable The MessageSourceResolvable (e.g., FieldError)
     * @return Resolved message
     */
    public static String resolveMessage(MessageSourceResolvable resolvable) {
        try {
            return messageSource.getMessage(resolvable, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            try {
                return messageSource.getMessage(resolvable, US);
            } catch (NoSuchMessageException fallback) {
                // If all codes fail, try to resolve using default message as a code
                String defaultMessage = resolvable.getDefaultMessage();
                if (defaultMessage != null && !defaultMessage.isEmpty()) {
                    try {
                        return messageSource.getMessage(defaultMessage, resolvable.getArguments(), LocaleContextHolder.getLocale());
                    } catch (NoSuchMessageException e2) {
                        try {
                            return messageSource.getMessage(defaultMessage, resolvable.getArguments(), US);
                        } catch (NoSuchMessageException e3) {
                            log.warn("No message found for codes: {}", String.join(", ", resolvable.getCodes()));
                            return defaultMessage;
                        }
                    }
                }
                log.warn("No message found for codes: {}", String.join(", ", resolvable.getCodes()));
                return defaultMessage != null ? defaultMessage : "";
            }
        }
    }
}

