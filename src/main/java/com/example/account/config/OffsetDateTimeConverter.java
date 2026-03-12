package com.example.account.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.sql.Timestamp;

/**
 * JPA AttributeConverter for OffsetDateTime to handle database persistence.
 * Converts OffsetDateTime to/from Timestamp for database storage.
 * This is needed because H2 and some other databases don't natively support OffsetDateTime.
 * Handles both Timestamp and LocalDateTime (H2 may convert Timestamp to LocalDateTime).
 */
@Converter
public class OffsetDateTimeConverter implements AttributeConverter<OffsetDateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return Timestamp.from(offsetDateTime.toInstant());
    }

    @Override
    public OffsetDateTime convertToEntityAttribute(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        // H2 may return LocalDateTime wrapped in Timestamp, so we handle it via Instant
        return OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneOffset.UTC);
    }
}

