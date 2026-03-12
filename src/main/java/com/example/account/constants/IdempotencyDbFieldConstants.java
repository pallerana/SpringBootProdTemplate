package com.example.account.constants;

/**
 * IdempotencyKey database field constants.
 * Extends BaseDbFieldConstants with idempotency-specific fields.
 */
public final class IdempotencyDbFieldConstants extends BaseDbFieldConstants {

    private IdempotencyDbFieldConstants() {
        // Utility class
    }

    // IdempotencyKey-specific fields
    public static final String IDEMPOTENCY_KEY_COLUMN_NAME = "idempotency_key";
    public static final String REQUEST_METHOD_COLUMN_NAME = "request_method";
    public static final String REQUEST_PATH_COLUMN_NAME = "request_path";
    public static final String RESPONSE_STATUS_COLUMN_NAME = "response_status";
    public static final String RESPONSE_BODY_COLUMN_NAME = "response_body";
}

