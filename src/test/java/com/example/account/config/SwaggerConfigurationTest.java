package com.example.account.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SwaggerConfiguration.
 */
class SwaggerConfigurationTest {

    @Test
    void testSwaggerConfiguration_WithCustomOpenAPI() {
        // Arrange
        OpenAPIConfig openAPIConfig = new OpenAPIConfig();
        ReflectionTestUtils.setField(openAPIConfig, "applicationName", "Test API");
        ReflectionTestUtils.setField(openAPIConfig, "serverPort", 8080);
        OpenAPI customOpenAPI = openAPIConfig.customOpenAPI();

        SwaggerConfiguration config = new SwaggerConfiguration();
        ReflectionTestUtils.setField(config, "customOpenAPI", customOpenAPI);

        // Act
        OpenAPI result = config.openAPI();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getInfo());
        assertNotNull(result.getComponents());
        assertNotNull(result.getComponents().getSecuritySchemes());
        assertTrue(result.getComponents().getSecuritySchemes().containsKey("Bearer Authentication"));
    }

    @Test
    void testSwaggerConfiguration_WithoutCustomOpenAPI() {
        // Arrange
        SwaggerConfiguration config = new SwaggerConfiguration();
        ReflectionTestUtils.setField(config, "customOpenAPI", null);

        // Act
        OpenAPI result = config.openAPI();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getComponents());
        assertNotNull(result.getComponents().getSecuritySchemes());
        assertTrue(result.getComponents().getSecuritySchemes().containsKey("Bearer Authentication"));
    }
}

