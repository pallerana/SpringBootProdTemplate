package com.example.account.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Swagger/OpenAPI configuration for API documentation.
 * 
 * Swagger UI is accessible without authentication (like partner app).
 * Bearer token authentication scheme is defined but not required by default.
 * Users can optionally add Bearer token in Swagger UI if needed for testing authenticated endpoints.
 * 
 * @Generated: Template Project
 */
@Configuration
public class SwaggerConfiguration {

    @Autowired(required = false)
    private OpenAPI customOpenAPI;

    @Bean
    @Primary
    public OpenAPI openAPI() {
        // Create new OpenAPI to avoid mutating the injected bean
        OpenAPI openAPI = new OpenAPI();
        
        // Copy info from customOpenAPI if it exists
        if (customOpenAPI != null) {
            if (customOpenAPI.getInfo() != null) {
                openAPI.setInfo(customOpenAPI.getInfo());
            }
            if (customOpenAPI.getServers() != null) {
                openAPI.setServers(customOpenAPI.getServers());
            }
        }
        
        // Add security components
        Components components = new Components();
        components.addSecuritySchemes("Bearer Authentication", createAPIKeyScheme());
        openAPI.setComponents(components);
        
        // Note: Security requirement is NOT added by default - Swagger UI is accessible without auth
        // If you want to require auth for Swagger UI, uncomment the line below:
        // openAPI.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
        
        return openAPI;
    }

    /**
     * Creates Bearer token security scheme for optional use in Swagger UI.
     * This allows users to optionally add Bearer token when testing authenticated endpoints.
     * 
     * @return SecurityScheme for Bearer token authentication
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}

