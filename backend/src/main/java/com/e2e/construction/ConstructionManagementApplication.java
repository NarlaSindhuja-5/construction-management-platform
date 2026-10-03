package com.e2e.construction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the E2E Construction Management System Backend.
 * Standard Spring Boot application enabling JPA and Web auto-configuration.
 */
@SpringBootApplication
public class ConstructionManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConstructionManagementApplication.class, args);
    }
}
