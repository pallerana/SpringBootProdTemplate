package com.example.account.util;

import com.example.account.enums.TransactionPrefixEnum;
import lombok.experimental.UtilityClass;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for Account operations.
 * Uses AtomicReference pattern for thread-safe unique ID generation.
 */
@UtilityClass
public class AccountUtil {

    /**
     * AtomicReference to track the last generated timestamp.
     * Ensures thread-safe unique ID generation even when multiple objects
     * are created in the same millisecond.
     */
    private static final AtomicReference<Long> currentTime = new AtomicReference<>(System.currentTimeMillis());

    /**
     * Generate a unique timestamp using AtomicReference pattern.
     * 
     * @return Unique timestamp-based ID
     */
    private static Long nextUniqueTimestamp() {
        return currentTime.accumulateAndGet(
            System.currentTimeMillis(),
            (prev, next) -> next > prev ? next : prev + 1
        );
    }

    /**
     * Generate a unique reference ID with the specified prefix.
     * 
     * @param prefix Transaction prefix (e.g., "ACC-")
     * @return Unique reference ID with the specified prefix
     */
    private static String generateUniqueReferenceId(String prefix) {
        Long uniqueTimestamp = nextUniqueTimestamp();
        // Format: prefix + timestamp
        return prefix + uniqueTimestamp;
    }

    /**
     * Generate a unique account ID.
     * 
     * @return Unique account ID (e.g., "ACC-1234567890123")
     */
    public static String generateAccountId() {
        return generateUniqueReferenceId(TransactionPrefixEnum.ACCOUNT.getValue());
    }

    /**
     * Generate a unique reference ID for the specified prefix enum.
     * 
     * @param prefix Transaction prefix enum
     * @return Unique reference ID with the specified prefix
     */
    public static String generateReferenceId(TransactionPrefixEnum prefix) {
        return generateUniqueReferenceId(prefix.getValue());
    }
}
