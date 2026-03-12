package com.example.account.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import java.lang.reflect.Field;
import java.util.Locale;

import static java.util.Locale.US;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for Translator.
 */
@ExtendWith(MockitoExtension.class)
class TranslatorTest {

    @Mock
    private MessageSource messageSource;

    private MessageSource originalMessageSource;

    @BeforeEach
    void setUp() throws Exception {
        // Save original messageSource
        Field field = Translator.class.getDeclaredField("messageSource");
        field.setAccessible(true);
        originalMessageSource = (MessageSource) field.get(null);
        
        // Set mock messageSource
        field.set(null, messageSource);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore original messageSource
        Field field = Translator.class.getDeclaredField("messageSource");
        field.setAccessible(true);
        field.set(null, originalMessageSource);
    }

    @Test
    void testGetMessage_Success() {
        // Arrange
        String messageCode = "test.message";
        String expectedMessage = "Test Message";
        when(messageSource.getMessage(eq(messageCode), isNull(), any(Locale.class)))
                .thenReturn(expectedMessage);

        // Act
        String result = Translator.getMessage(messageCode);

        // Assert
        assertEquals(expectedMessage, result);
        verify(messageSource).getMessage(eq(messageCode), isNull(), any(Locale.class));
    }

    @Test
    void testGetMessage_WithArgs() {
        // Arrange
        String messageCode = "test.message";
        Object[] args = new Object[]{"arg1", "arg2"};
        String expectedMessage = "Test Message with arg1 and arg2";
        when(messageSource.getMessage(eq(messageCode), eq(args), any(Locale.class)))
                .thenReturn(expectedMessage);

        // Act
        String result = Translator.getMessage(messageCode, args);

        // Assert
        assertEquals(expectedMessage, result);
        verify(messageSource).getMessage(eq(messageCode), eq(args), any(Locale.class));
    }

    @Test
    void testGetMessage_NotFound_FallbackToUS() {
        // Arrange
        String messageCode = "nonexistent.message";
        String fallbackMessage = "Fallback Message";
        when(messageSource.getMessage(eq(messageCode), isNull(), any(Locale.class)))
                .thenThrow(new NoSuchMessageException(messageCode))
                .thenReturn(fallbackMessage);

        // Act
        String result = Translator.getMessage(messageCode);

        // Assert
        assertEquals(fallbackMessage, result);
        verify(messageSource, times(2)).getMessage(eq(messageCode), isNull(), any(Locale.class));
    }

    @Test
    void testGetMessage_NotFound_NoFallback() {
        // Arrange
        String messageCode = "nonexistent.message";
        when(messageSource.getMessage(eq(messageCode), isNull(), any(Locale.class)))
                .thenThrow(new NoSuchMessageException(messageCode));

        // Act
        String result = Translator.getMessage(messageCode);

        // Assert
        assertEquals(messageCode, result);
    }

    @Test
    void testResolveMessage_Success() {
        // Arrange
        MessageSourceResolvable resolvable = mock(MessageSourceResolvable.class);
        String expectedMessage = "Resolved Message";
        when(messageSource.getMessage(eq(resolvable), any(Locale.class)))
                .thenReturn(expectedMessage);

        // Act
        String result = Translator.resolveMessage(resolvable);

        // Assert
        assertEquals(expectedMessage, result);
        verify(messageSource).getMessage(eq(resolvable), any(Locale.class));
    }

    @Test
    void testResolveMessage_NotFound_FallbackToUS() {
        // Arrange
        MessageSourceResolvable resolvable = mock(MessageSourceResolvable.class);
        String expectedMessage = "Fallback Resolved Message";
        when(messageSource.getMessage(eq(resolvable), any(Locale.class)))
                .thenThrow(new NoSuchMessageException("code"))
                .thenReturn(expectedMessage);

        // Act
        String result = Translator.resolveMessage(resolvable);

        // Assert
        assertEquals(expectedMessage, result);
        verify(messageSource, times(2)).getMessage(eq(resolvable), any(Locale.class));
    }

    @Test
    void testResolveMessage_NotFound_UseDefaultMessage() {
        // Arrange
        MessageSourceResolvable resolvable = mock(MessageSourceResolvable.class);
        String defaultMessage = "default.message";
        String[] codes = new String[]{"code1", "code2"};
        Object[] args = new Object[]{"arg1"};
        
        when(resolvable.getDefaultMessage()).thenReturn(defaultMessage);
        lenient().when(resolvable.getCodes()).thenReturn(codes); // Only used in error logging, not in successful path
        when(resolvable.getArguments()).thenReturn(args);
        
        // Use lenient() for all exception paths to avoid unnecessary stubbing issues
        // First try with current locale - fails
        lenient().when(messageSource.getMessage(eq(resolvable), any(Locale.class)))
                .thenThrow(new NoSuchMessageException("code"));
        // Second try with US locale - also fails
        lenient().when(messageSource.getMessage(eq(resolvable), eq(US)))
                .thenThrow(new NoSuchMessageException("code"));
        // Third try: default message with current locale - fails
        lenient().when(messageSource.getMessage(eq(defaultMessage), eq(args), any(Locale.class)))
                .thenThrow(new NoSuchMessageException("code"));
        // Fourth try: default message with US locale - succeeds
        when(messageSource.getMessage(eq(defaultMessage), eq(args), eq(US)))
                .thenReturn("Resolved from default");

        // Act
        String result = Translator.resolveMessage(resolvable);

        // Assert
        assertEquals("Resolved from default", result);
    }

    @Test
    void testGetMessage_WithArgs_NotFound_NoFallback() {
        // Arrange
        String messageCode = "nonexistent.message";
        Object[] args = new Object[]{"arg1", "arg2"};
        when(messageSource.getMessage(eq(messageCode), eq(args), any(Locale.class)))
                .thenThrow(new NoSuchMessageException(messageCode));

        // Act
        String result = Translator.getMessage(messageCode, args);

        // Assert
        assertEquals(messageCode, result);
        verify(messageSource, times(2)).getMessage(eq(messageCode), eq(args), any(Locale.class));
    }

    @Test
    void testGetMessage_WithArgs_NotFound_FallbackToUS() {
        // Arrange
        String messageCode = "nonexistent.message";
        Object[] args = new Object[]{"arg1", "arg2"};
        String fallbackMessage = "Fallback Message with args";
        when(messageSource.getMessage(eq(messageCode), eq(args), any(Locale.class)))
                .thenThrow(new NoSuchMessageException(messageCode))
                .thenReturn(fallbackMessage);

        // Act
        String result = Translator.getMessage(messageCode, args);

        // Assert
        assertEquals(fallbackMessage, result);
        verify(messageSource, times(2)).getMessage(eq(messageCode), eq(args), any(Locale.class));
    }

    @Test
    void testResolveMessage_NotFound_DefaultMessageNull() {
        // Arrange
        MessageSourceResolvable resolvable = mock(MessageSourceResolvable.class);
        String[] codes = new String[]{"code1", "code2"};
        
        when(resolvable.getDefaultMessage()).thenReturn(null);
        lenient().when(resolvable.getCodes()).thenReturn(codes);
        
        // First try with current locale - fails
        lenient().when(messageSource.getMessage(eq(resolvable), any(Locale.class)))
                .thenThrow(new NoSuchMessageException("code"));
        // Second try with US locale - also fails
        lenient().when(messageSource.getMessage(eq(resolvable), eq(US)))
                .thenThrow(new NoSuchMessageException("code"));

        // Act
        String result = Translator.resolveMessage(resolvable);

        // Assert
        assertEquals("", result); // Should return empty string when defaultMessage is null
        verify(resolvable).getDefaultMessage();
    }

    @Test
    void testResolveMessage_NotFound_DefaultMessageEmpty() {
        // Arrange
        MessageSourceResolvable resolvable = mock(MessageSourceResolvable.class);
        String[] codes = new String[]{"code1", "code2"};
        
        when(resolvable.getDefaultMessage()).thenReturn("");
        lenient().when(resolvable.getCodes()).thenReturn(codes);
        
        // First try with current locale - fails
        lenient().when(messageSource.getMessage(eq(resolvable), any(Locale.class)))
                .thenThrow(new NoSuchMessageException("code"));
        // Second try with US locale - also fails
        lenient().when(messageSource.getMessage(eq(resolvable), eq(US)))
                .thenThrow(new NoSuchMessageException("code"));

        // Act
        String result = Translator.resolveMessage(resolvable);

        // Assert
        assertEquals("", result); // Should return empty string when defaultMessage is empty
        verify(resolvable).getDefaultMessage();
    }

    @Test
    void testResolveMessage_NotFound_DefaultMessageAsKey_FailsBothLocales() {
        // Arrange
        MessageSourceResolvable resolvable = mock(MessageSourceResolvable.class);
        String defaultMessage = "default.message.key";
        String[] codes = new String[]{"code1", "code2"};
        Object[] args = new Object[]{"arg1"};
        
        when(resolvable.getDefaultMessage()).thenReturn(defaultMessage);
        lenient().when(resolvable.getCodes()).thenReturn(codes);
        when(resolvable.getArguments()).thenReturn(args);
        
        // First try with current locale - fails
        lenient().when(messageSource.getMessage(eq(resolvable), any(Locale.class)))
                .thenThrow(new NoSuchMessageException("code"));
        // Second try with US locale - also fails
        lenient().when(messageSource.getMessage(eq(resolvable), eq(US)))
                .thenThrow(new NoSuchMessageException("code"));
        // Third try: default message with current locale - fails
        lenient().when(messageSource.getMessage(eq(defaultMessage), eq(args), any(Locale.class)))
                .thenThrow(new NoSuchMessageException("code"));
        // Fourth try: default message with US locale - also fails
        lenient().when(messageSource.getMessage(eq(defaultMessage), eq(args), eq(US)))
                .thenThrow(new NoSuchMessageException("code"));

        // Act
        String result = Translator.resolveMessage(resolvable);

        // Assert
        assertEquals(defaultMessage, result); // Should return defaultMessage as final fallback
        verify(resolvable, atLeastOnce()).getCodes(); // Should log warning with codes
    }

    @Test
    void testResolveMessage_NotFound_DefaultMessageAsKey_FailsCurrentLocale_SucceedsUS() {
        // Arrange
        MessageSourceResolvable resolvable = mock(MessageSourceResolvable.class);
        String defaultMessage = "default.message.key";
        String[] codes = new String[]{"code1", "code2"};
        Object[] args = new Object[]{"arg1"};
        
        when(resolvable.getDefaultMessage()).thenReturn(defaultMessage);
        lenient().when(resolvable.getCodes()).thenReturn(codes);
        when(resolvable.getArguments()).thenReturn(args);
        
        // First try with current locale - fails
        lenient().when(messageSource.getMessage(eq(resolvable), any(Locale.class)))
                .thenThrow(new NoSuchMessageException("code"));
        // Second try with US locale - also fails
        lenient().when(messageSource.getMessage(eq(resolvable), eq(US)))
                .thenThrow(new NoSuchMessageException("code"));
        // Third try: default message with current locale - fails
        lenient().when(messageSource.getMessage(eq(defaultMessage), eq(args), any(Locale.class)))
                .thenThrow(new NoSuchMessageException("code"));
        // Fourth try: default message with US locale - succeeds
        when(messageSource.getMessage(eq(defaultMessage), eq(args), eq(US)))
                .thenReturn("Resolved from default key");

        // Act
        String result = Translator.resolveMessage(resolvable);

        // Assert
        assertEquals("Resolved from default key", result);
    }
}

