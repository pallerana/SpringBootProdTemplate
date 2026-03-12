package com.example.account.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests.
 * Provides common configuration for all integration tests.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
}

