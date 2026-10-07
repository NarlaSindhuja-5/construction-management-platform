package com.e2e.construction.controller;

import com.e2e.construction.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthCheckController {

    private final DataSource dataSource;
    private final RoleRepository roleRepository;

    public HealthCheckController(DataSource dataSource, RoleRepository roleRepository) {
        this.dataSource = dataSource;
        this.roleRepository = roleRepository;
    }

    /**
     * Health check endpoint confirming Spring Boot status and MySQL connectivity.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("message", "E2E Construction Management Backend is running");
        response.put("application", "E2E Construction Management System");
        response.put("timestamp", Instant.now().toString());

        Map<String, Object> dbHealth = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(2);
            DatabaseMetaData metaData = connection.getMetaData();

            dbHealth.put("status", isValid ? "UP" : "DOWN");
            dbHealth.put("databaseProductName", metaData.getDatabaseProductName());
            dbHealth.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            dbHealth.put("databaseName", connection.getCatalog());
            dbHealth.put("connectionValid", isValid);

            // Verify JPA repository execution
            long totalRoles = roleRepository.count();
            dbHealth.put("rolesRegistered", totalRoles);
            dbHealth.put("jpaRepositoryWorking", true);

            response.put("database", dbHealth);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", ex.getMessage());
            response.put("database", dbHealth);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Basic status endpoint.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("application", "E2E Construction Management System");
        response.put("status", "UP");
        return ResponseEntity.ok(response);
    }
}
