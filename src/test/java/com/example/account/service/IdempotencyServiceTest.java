package com.example.account.service;

import com.example.account.model.IdempotencyKey;
import com.example.account.repository.IdempotencyKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IdempotencyService.
 */
@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    private static final String TEST_IDEMPOTENCY_KEY = "test-key-123";
    private static final String TEST_REQUEST_PATH = "/api/v1/accounts";
    private static final HttpMethod TEST_METHOD = HttpMethod.POST;

    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @InjectMocks
    private IdempotencyService idempotencyService;

    @Test
    void testGetCachedResponse_Success() {
        // Arrange
        IdempotencyKey key = IdempotencyKey.builder()
                .id(1L)
                .idempotencyKey(TEST_IDEMPOTENCY_KEY)
                .requestMethod(TEST_METHOD.name())
                .requestPath(TEST_REQUEST_PATH)
                .responseStatus(201)
                .responseBody("{\"accountId\":\"ACC-001\"}")
                .createdAt(OffsetDateTime.now())
                .build();

        when(idempotencyKeyRepository.findByIdempotencyKey(TEST_IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(key));

        // Act
        ResponseEntity<?> result = idempotencyService.getCachedResponse(
                TEST_IDEMPOTENCY_KEY, TEST_METHOD, TEST_REQUEST_PATH, Object.class);

        // Assert
        assertNotNull(result);
        assertEquals(201, result.getStatusCode().value());
        verify(idempotencyKeyRepository).findByIdempotencyKey(TEST_IDEMPOTENCY_KEY);
    }

    @Test
    void testGetCachedResponse_NullKey() {
        // Act
        ResponseEntity<?> result = idempotencyService.getCachedResponse(
                null, TEST_METHOD, TEST_REQUEST_PATH, Object.class);

        // Assert
        assertNull(result);
        verify(idempotencyKeyRepository, never()).findByIdempotencyKey(anyString());
    }

    @Test
    void testGetCachedResponse_BlankKey() {
        // Act
        ResponseEntity<?> result = idempotencyService.getCachedResponse(
                "   ", TEST_METHOD, TEST_REQUEST_PATH, Object.class);

        // Assert
        assertNull(result);
        verify(idempotencyKeyRepository, never()).findByIdempotencyKey(anyString());
    }

    @Test
    void testGetCachedResponse_NotFound() {
        // Arrange
        when(idempotencyKeyRepository.findByIdempotencyKey(TEST_IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> result = idempotencyService.getCachedResponse(
                TEST_IDEMPOTENCY_KEY, TEST_METHOD, TEST_REQUEST_PATH, Object.class);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetCachedResponse_MethodMismatch() {
        // Arrange
        IdempotencyKey key = IdempotencyKey.builder()
                .idempotencyKey(TEST_IDEMPOTENCY_KEY)
                .requestMethod(HttpMethod.PUT.name())
                .requestPath(TEST_REQUEST_PATH)
                .responseStatus(200)
                .responseBody("{}")
                .build();

        when(idempotencyKeyRepository.findByIdempotencyKey(TEST_IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(key));

        // Act
        ResponseEntity<?> result = idempotencyService.getCachedResponse(
                TEST_IDEMPOTENCY_KEY, TEST_METHOD, TEST_REQUEST_PATH, Object.class);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetCachedResponse_PathMismatch() {
        // Arrange
        IdempotencyKey key = IdempotencyKey.builder()
                .idempotencyKey(TEST_IDEMPOTENCY_KEY)
                .requestMethod(TEST_METHOD.name())
                .requestPath("/different/path")
                .responseStatus(200)
                .responseBody("{}")
                .build();

        when(idempotencyKeyRepository.findByIdempotencyKey(TEST_IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(key));

        // Act
        ResponseEntity<?> result = idempotencyService.getCachedResponse(
                TEST_IDEMPOTENCY_KEY, TEST_METHOD, TEST_REQUEST_PATH, Object.class);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetCachedResponse_DeserializationError() {
        // Arrange
        IdempotencyKey key = IdempotencyKey.builder()
                .idempotencyKey(TEST_IDEMPOTENCY_KEY)
                .requestMethod(TEST_METHOD.name())
                .requestPath(TEST_REQUEST_PATH)
                .responseStatus(201)
                .responseBody("invalid json")
                .build();

        when(idempotencyKeyRepository.findByIdempotencyKey(TEST_IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(key));

        // Act
        ResponseEntity<?> result = idempotencyService.getCachedResponse(
                TEST_IDEMPOTENCY_KEY, TEST_METHOD, TEST_REQUEST_PATH, Object.class);

        // Assert
        assertNull(result);
    }

    @Test
    void testStoreResponse_Success() {
        // Arrange
        ResponseEntity<String> response = ResponseEntity.status(HttpStatus.CREATED)
                .body("{\"accountId\":\"ACC-001\"}");

        when(idempotencyKeyRepository.save(any(IdempotencyKey.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        idempotencyService.storeResponse(TEST_IDEMPOTENCY_KEY, TEST_METHOD, TEST_REQUEST_PATH, response);

        // Assert
        verify(idempotencyKeyRepository).save(any(IdempotencyKey.class));
    }

    @Test
    void testStoreResponse_NullKey() {
        // Arrange
        ResponseEntity<String> response = ResponseEntity.ok("{}");

        // Act
        idempotencyService.storeResponse(null, TEST_METHOD, TEST_REQUEST_PATH, response);

        // Assert
        verify(idempotencyKeyRepository, never()).save(any(IdempotencyKey.class));
    }

    @Test
    void testStoreResponse_BlankKey() {
        // Arrange
        ResponseEntity<String> response = ResponseEntity.ok("{}");

        // Act
        idempotencyService.storeResponse("   ", TEST_METHOD, TEST_REQUEST_PATH, response);

        // Assert
        verify(idempotencyKeyRepository, never()).save(any(IdempotencyKey.class));
    }

    @Test
    void testStoreResponse_ExceptionHandling() {
        // Arrange
        ResponseEntity<String> response = ResponseEntity.ok("{}");
        when(idempotencyKeyRepository.save(any(IdempotencyKey.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> 
                idempotencyService.storeResponse(TEST_IDEMPOTENCY_KEY, TEST_METHOD, TEST_REQUEST_PATH, response));
    }
}




