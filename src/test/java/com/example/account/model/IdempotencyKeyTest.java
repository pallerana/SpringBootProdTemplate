package com.example.account.model;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IdempotencyKey entity.
 */
class IdempotencyKeyTest {

    @Test
    void testIdempotencyKeyBuilder() {
        // Arrange & Act
        IdempotencyKey key = IdempotencyKey.builder()
                .id(1L)
                .idempotencyKey("test-key-123")
                .requestMethod("POST")
                .requestPath("/api/v1/accounts")
                .responseStatus(201)
                .responseBody("{\"accountId\":\"ACC-001\"}")
                .createdAt(OffsetDateTime.now())
                .build();

        // Assert
        assertNotNull(key);
        assertEquals(1L, key.getId());
        assertEquals("test-key-123", key.getIdempotencyKey());
        assertEquals("POST", key.getRequestMethod());
        assertEquals("/api/v1/accounts", key.getRequestPath());
        assertEquals(201, key.getResponseStatus());
        assertNotNull(key.getResponseBody());
        assertNotNull(key.getCreatedAt());
    }

    @Test
    void testIdempotencyKeyNoArgsConstructor() {
        // Arrange & Act
        IdempotencyKey key = new IdempotencyKey();

        // Assert
        assertNotNull(key);
        assertNull(key.getId());
        assertNull(key.getIdempotencyKey());
    }

    @Test
    void testIdempotencyKeySetters() {
        // Arrange
        IdempotencyKey key = new IdempotencyKey();
        OffsetDateTime now = OffsetDateTime.now();

        // Act
        key.setId(1L);
        key.setIdempotencyKey("test-key");
        key.setRequestMethod("POST");
        key.setRequestPath("/api/v1/accounts");
        key.setResponseStatus(201);
        key.setResponseBody("{}");
        key.setCreatedAt(now);

        // Assert
        assertEquals(1L, key.getId());
        assertEquals("test-key", key.getIdempotencyKey());
        assertEquals("POST", key.getRequestMethod());
        assertEquals("/api/v1/accounts", key.getRequestPath());
        assertEquals(201, key.getResponseStatus());
        assertEquals("{}", key.getResponseBody());
        assertEquals(now, key.getCreatedAt());
    }

    @Test
    void testIdempotencyKeyOnCreate() {
        // Arrange
        IdempotencyKey key = new IdempotencyKey();
        assertNull(key.getCreatedAt());

        // Act
        key.onCreate();

        // Assert
        assertNotNull(key.getCreatedAt());
    }

    @Test
    void testIdempotencyKeyOnCreate_AlreadySet() {
        // Arrange
        OffsetDateTime existingTime = OffsetDateTime.now().minusHours(1);
        IdempotencyKey key = new IdempotencyKey();
        key.setCreatedAt(existingTime);

        // Act
        key.onCreate();

        // Assert
        assertEquals(existingTime, key.getCreatedAt());
    }
}




