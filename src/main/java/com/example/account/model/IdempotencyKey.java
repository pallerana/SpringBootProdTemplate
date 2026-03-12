package com.example.account.model;

import com.example.account.config.OffsetDateTimeConverter;
import com.example.account.constants.BaseDbFieldConstants;
import com.example.account.constants.IdempotencyDbFieldConstants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Entity to store idempotency keys and their associated responses.
 * Prevents duplicate processing of the same request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "idempotency_keys", indexes = {
    @Index(name = "idx_idempotency_key", columnList = IdempotencyDbFieldConstants.IDEMPOTENCY_KEY_COLUMN_NAME, unique = true)
})
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = IdempotencyDbFieldConstants.IDEMPOTENCY_KEY_COLUMN_NAME, nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @Column(name = IdempotencyDbFieldConstants.REQUEST_METHOD_COLUMN_NAME, nullable = false, length = 10)
    private String requestMethod;

    @Column(name = IdempotencyDbFieldConstants.REQUEST_PATH_COLUMN_NAME, nullable = false, length = 500)
    private String requestPath;

    @Column(name = IdempotencyDbFieldConstants.RESPONSE_STATUS_COLUMN_NAME, nullable = false)
    private Integer responseStatus;

    @Column(name = IdempotencyDbFieldConstants.RESPONSE_BODY_COLUMN_NAME, columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = BaseDbFieldConstants.CREATED_AT_COLUMN_NAME, nullable = false, updatable = false)
    @Convert(converter = OffsetDateTimeConverter.class)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}

