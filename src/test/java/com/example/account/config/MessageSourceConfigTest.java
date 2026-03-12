package com.example.account.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageSourceConfig.
 */
class MessageSourceConfigTest {

    @Test
    void testMessageSourceBean() {
        // Arrange
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MessageSourceConfig.class);

        // Act
        MessageSource messageSource = context.getBean(MessageSource.class);

        // Assert
        assertNotNull(messageSource);
        context.close();
    }
}

