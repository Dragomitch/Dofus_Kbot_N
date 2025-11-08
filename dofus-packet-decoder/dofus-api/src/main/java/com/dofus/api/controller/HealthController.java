package com.dofus.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Health check and status endpoints
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Health check and status endpoints")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check application health", description = "Returns the health status of the application")
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = new HealthResponse();
        response.setStatus("UP");
        response.setTimestamp(LocalDateTime.now());
        response.setVersion("1.0.0");
        response.setMessage("Dofus Packet Decoder is running");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    @Operation(summary = "Ping endpoint", description = "Simple ping endpoint to verify API is reachable")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @Data
    public static class HealthResponse {
        private String status;
        private LocalDateTime timestamp;
        private String version;
        private String message;
    }
}
