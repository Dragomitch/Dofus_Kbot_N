package com.dofus.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Main configuration properties for Dofus application.
 * Maps to 'dofus.*' properties in application.yml
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dofus")
public class DofusConfiguration {

    private NetworkConfig network = new NetworkConfig();
    private ProtocolConfig protocol = new ProtocolConfig();
    private GameConfig game = new GameConfig();
    private CombatConfig combat = new CombatConfig();
    private NavigationConfig navigation = new NavigationConfig();
    private PersistenceConfig persistence = new PersistenceConfig();

    @Data
    public static class NetworkConfig {
        private ProxyConfig proxy = new ProxyConfig();
        private CaptureConfig capture = new CaptureConfig();

        @Data
        public static class ProxyConfig {
            private boolean enabled = true;
            private int port = 5555;
            private String targetHost = "34.251.172.139";
            private int targetPort = 443;
        }

        @Data
        public static class CaptureConfig {
            private int bufferSize = 8192;
            private int maxConnections = 100;
            private int timeoutSeconds = 30;
        }
    }

    @Data
    public static class ProtocolConfig {
        private String version = "1.30.0";
        private EncryptionConfig encryption = new EncryptionConfig();

        @Data
        public static class EncryptionConfig {
            private boolean enabled = false;
            private String algorithm = "XOR";
            private String key = "default-key";
        }
    }

    @Data
    public static class GameConfig {
        private boolean autoReconnect = true;
        private boolean packetLogging = true;
        private boolean statePersistence = true;
        private int maxPacketHistory = 10000;
    }

    @Data
    public static class CombatConfig {
        private String strategy = "BALANCED";
        private int turnTimeoutSeconds = 45;
        private boolean autoPassTurn = false;
        private List<SpellPriority> spellPriorities = new ArrayList<>();

        @Data
        public static class SpellPriority {
            private int id;
            private int priority;
            private int minRange;
            private int maxRange;
        }
    }

    @Data
    public static class NavigationConfig {
        private String algorithm = "A_STAR";
        private int maxPathLength = 50;
        private boolean avoidAggressiveMobs = true;
        private boolean recalculateOnObstacle = true;
    }

    @Data
    public static class PersistenceConfig {
        private int packetRetentionDays = 7;
        private int combatSessionRetentionDays = 30;
        private boolean cleanupEnabled = true;
        private String cleanupSchedule = "0 0 3 * * ?";
    }
}
