package com.example.account.util;

import com.example.account.enums.TransactionPrefixEnum;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccountUtil.
 * Tests unique ID generation logic.
 */
class AccountUtilTest {

    @Test
    void testGenerateAccountId_Format() {
        // Act
        String accountId = AccountUtil.generateAccountId();

        // Assert
        assertNotNull(accountId);
        assertTrue(accountId.startsWith("ACC-"), "Account ID should start with ACC-");
        assertTrue(accountId.length() > 4, "Account ID should have more than just prefix");
    }

    @Test
    void testGenerateAccountId_Uniqueness() {
        // Act
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            ids.add(AccountUtil.generateAccountId());
        }

        // Assert
        assertEquals(100, ids.size(), "All generated IDs should be unique");
    }

    @Test
    void testGenerateReferenceId_WithPrefix() {
        // Act
        String accountId = AccountUtil.generateReferenceId(TransactionPrefixEnum.ACCOUNT);

        // Assert
        assertNotNull(accountId);
        assertTrue(accountId.startsWith("ACC-"), "Account ID should start with ACC-");
    }

    @Test
    void testGenerateAccountId_ThreadSafety() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        int idsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<String> allIds = new HashSet<>();
        Object lock = new Object();

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    Set<String> threadIds = new HashSet<>();
                    for (int j = 0; j < idsPerThread; j++) {
                        threadIds.add(AccountUtil.generateAccountId());
                    }
                    synchronized (lock) {
                        allIds.addAll(threadIds);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertEquals(threadCount * idsPerThread, allIds.size(), 
                "All generated IDs should be unique across threads");
    }

    @Test
    void testGenerateAccountId_NotNull() {
        // Act
        String accountId = AccountUtil.generateAccountId();

        // Assert
        assertNotNull(accountId);
        assertFalse(accountId.isEmpty());
    }
}







