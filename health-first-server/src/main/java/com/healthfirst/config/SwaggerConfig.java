package com.healthfirst.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI healthFirstOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:" + serverPort + "/api/v1");
        devServer.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setEmail("admin@healthfirst.com");
        contact.setName("HealthFirst API Support");
        contact.setUrl("https://www.healthfirst.com");

        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("HealthFirst Provider Registration API")
                .version("1.0.0")
                .contact(contact)
                .description("RESTful API for healthcare provider registration, authentication, and management. " +
                           "This API provides secure endpoints for provider registration with comprehensive validation, " +
                           "email verification, and rate limiting.")
                .termsOfService("https://www.healthfirst.com/terms")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
} 