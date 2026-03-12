package com.example.account.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 * 
 * @Generated: Template Project
 */
@Configuration
public class OpenAPIConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .version("1.0.0")
                        .description("API documentation for " + applicationName)
                        .contact(new Contact()
                                .name("Development Team")
                                .email("dev-team@example.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://example.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.example.com")
                                .description("Production Server")
                ));
    }
}

