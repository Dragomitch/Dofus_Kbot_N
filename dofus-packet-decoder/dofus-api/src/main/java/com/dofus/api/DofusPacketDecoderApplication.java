package com.dofus.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application for Dofus Packet Decoder.
 * <p>
 * This application provides:
 * - Network packet interception and decoding
 * - Real-time game state tracking
 * - Pathfinding and navigation
 * - Combat logic and strategies
 * - REST API and WebSocket endpoints
 * </p>
 *
 * @author Dofus Packet Decoder Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.dofus")
@EnableCaching
@EnableAsync
@EnableScheduling
public class DofusPacketDecoderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DofusPacketDecoderApplication.class, args);
    }
}
