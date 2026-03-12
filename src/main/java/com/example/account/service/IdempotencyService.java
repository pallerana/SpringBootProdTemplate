package com.example.account.service;

import com.google.gson.Gson;
import com.example.account.model.IdempotencyKey;
import com.example.account.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Service for handling idempotency key operations.
 * Prevents duplicate processing of requests with the same idempotency key.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final Gson gson = new Gson();

    /**
     * Check if an idempotency key exists and return cached response if found.
     * 
     * @param idempotencyKey The idempotency key from request header
     * @param requestMethod The HTTP method
     * @param requestPath The request path
     * @param responseType The expected response type class
     * @return Cached response if key exists, null otherwise
     */
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <T> ResponseEntity<T> getCachedResponse(String idempotencyKey, HttpMethod requestMethod, String requestPath, Class<T> responseType) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }

        String methodName = requestMethod != null ? requestMethod.name() : null;
        return idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey)
                .filter(key -> key.getRequestMethod().equals(methodName) && key.getRequestPath().equals(requestPath))
                .map(key -> {
                    log.debug("Found cached response for idempotency key: {}", idempotencyKey);
                    try {
                        T responseBody = gson.fromJson(key.getResponseBody(), responseType);
                        return ResponseEntity.status(key.getResponseStatus()).body(responseBody);
                    } catch (Exception e) {
                        log.warn("Failed to deserialize cached response for key: {}", idempotencyKey, e);
                        return null;
                    }
                })
                .orElse(null);
    }

    /**
     * Store the response for an idempotency key.
     * 
     * @param idempotencyKey The idempotency key from request header
     * @param requestMethod The HTTP method
     * @param requestPath The request path
     * @param response The response to cache
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void storeResponse(String idempotencyKey, HttpMethod requestMethod, String requestPath, ResponseEntity<?> response) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }

        try {
            String responseBody = gson.toJson(response.getBody());
            Integer responseStatus = response.getStatusCode().value();
            String methodName = requestMethod != null ? requestMethod.name() : null;

            IdempotencyKey key = IdempotencyKey.builder()
                    .idempotencyKey(idempotencyKey)
                    .requestMethod(methodName)
                    .requestPath(requestPath)
                    .responseStatus(responseStatus)
                    .responseBody(responseBody)
                    .createdAt(OffsetDateTime.now())
                    .build();

            idempotencyKeyRepository.save(key);
            log.debug("Stored response for idempotency key: {}", idempotencyKey);
        } catch (Exception e) {
            log.warn("Failed to store idempotency key response: {}", idempotencyKey, e);
            // Don't throw exception - idempotency is best effort
        }
    }
}

