package com.example.account.model;

import com.example.account.config.OffsetDateTimeConverter;
import com.example.account.constants.BaseDbFieldConstants;
import com.example.account.enums.AccountStatusEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Base entity class with default fields for all entities.
 * All entities should extend this class.
 * 
 * Note: Date auditing is handled manually via @PrePersist and @PreUpdate
 * instead of Spring Data JPA auditing to avoid conflicts with H2's LocalDateTime.
 */
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
@SuperBuilder
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = BaseDbFieldConstants.CREATED_AT_COLUMN_NAME, nullable = false, updatable = false)
    @Convert(converter = OffsetDateTimeConverter.class)
    private OffsetDateTime createdAt;

    @Column(name = BaseDbFieldConstants.UPDATED_AT_COLUMN_NAME)
    @Convert(converter = OffsetDateTimeConverter.class)
    private OffsetDateTime updatedAt;

    @Column(name = BaseDbFieldConstants.STATUS_COLUMN_NAME)
    private String status;

    @Column(name = BaseDbFieldConstants.PREVIOUS_STATUS_COLUMN_NAME)
    private String previousStatus;

    /**
     * Initialize default values for the entity.
     * Called before saving to ensure required fields have default values.
     */
    @PrePersist
    public void initializeDefaults() {
        if (this.status == null) {
            this.status = AccountStatusEnum.PENDING.getValue();
        }
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Update the timestamp fields.
     * Called before saving to ensure timestamps are current.
     */
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = OffsetDateTime.now();
    }
}

