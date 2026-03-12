package com.example.account.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OpenAPIConfig.
 */
class OpenAPIConfigTest {

    @Test
    void testOpenAPIConfigBean() {
        // Arrange
        OpenAPIConfig config = new OpenAPIConfig();
        ReflectionTestUtils.setField(config, "applicationName", "Test API");
        ReflectionTestUtils.setField(config, "serverPort", 8080);

        // Act
        OpenAPI openAPI = config.customOpenAPI();

        // Assert
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("Test API API", openAPI.getInfo().getTitle());
        assertNotNull(openAPI.getServers());
        assertEquals(2, openAPI.getServers().size());
    }
}

