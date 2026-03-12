package com.example.account.constants;

/**
 * Common constants used across the application.
 */
public final class CommonConstants {

    private CommonConstants() {
        // Utility class
    }

    // API Endpoints
    public static final String API_V1_PREFIX = "/api/v1";
    public static final String ACCOUNT_API_ENDPOINT = "/accounts";
    
    // Account API Endpoints (relative to controller base path)
    public static final String ACCOUNT_CREATE_API_ENDPOINT = "";
    public static final String ACCOUNT_GET_API_ENDPOINT = "/{accountId}";
    public static final String ACCOUNT_UPDATE_API_ENDPOINT = "/{accountId}";
    public static final String ACCOUNT_DELETE_API_ENDPOINT = "/{accountId}";
    public static final String ACCOUNT_LIST_API_ENDPOINT = "";
    
    // HTTP Headers
    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
}

