package com.dofus.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * JPA Entity representing captured network packets.
 *
 * <p>This entity stores both raw and parsed packet data from Dofus Retro network traffic.
 * Packets are partitioned by timestamp for performance optimization.
 *
 * <p>Key Features:
 * <ul>
 *   <li>Stores raw byte data and parsed JSON representation</li>
 *   <li>Tracks packet direction (INBOUND/OUTBOUND)</li>
 *   <li>Groups packets by session ID</li>
 *   <li>Partitioned by timestamp (monthly partitions)</li>
 *   <li>JSONB column for flexible packet schema</li>
 * </ul>
 *
 * <p>Note: This table is partitioned in PostgreSQL. The partition key (timestamp)
 * must be included in the primary key as a composite key.
 *
 * @author Agent A2 - Database Architect
 * @version 1.0
 * @since 2025-11-08
 */
@Entity
@Table(
    name = "packets",
    indexes = {
        @Index(name = "idx_packets_type", columnList = "packet_type"),
        @Index(name = "idx_packets_timestamp", columnList = "timestamp"),
        @Index(name = "idx_packets_session", columnList = "session_id"),
        @Index(name = "idx_packets_player", columnList = "player_id"),
        @Index(name = "idx_packets_direction", columnList = "direction"),
        @Index(name = "idx_packets_type_timestamp", columnList = "packet_type, timestamp")
        // Note: GIN index on parsed_data created via SQL migration
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"rawData"}) // Exclude large byte arrays from toString
@EqualsAndHashCode(of = {"id", "timestamp"}) // Composite key for partitioned table
public class PacketLog {

    /**
     * Auto-generated packet log ID.
     * Part of composite primary key (required for partitioning).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Packet type identifier (1-3 characters).
     * Examples: AA (auth), GDM (map data), GA (game action), GT (turn).
     */
    @Column(name = "packet_type", length = 10, nullable = false)
    private String packetType;

    /**
     * Packet direction.
     * INBOUND: Server -> Client
     * OUTBOUND: Client -> Server
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "direction", length = 10, nullable = false)
    private PacketDirection direction;

    /**
     * Raw packet bytes as captured from network.
     * Stored as BYTEA in PostgreSQL.
     */
    @Lob
    @Column(name = "raw_data", nullable = false)
    private byte[] rawData;

    /**
     * Parsed packet data as JSON.
     * Stored as JSONB in PostgreSQL for efficient querying.
     * Structure varies by packet type.
     */
    @Column(name = "parsed_data", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> parsedData;

    /**
     * Packet capture timestamp.
     * Part of composite primary key (partition key).
     * Indexed descending for "recent packets" queries.
     */
    @Column(name = "timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Game session identifier.
     * Groups related packets from a single game session.
     * Format: UUID string.
     */
    @Column(name = "session_id", length = 36)
    private String sessionId;

    /**
     * Associated player ID.
     * Soft reference (no FK constraint due to partitioning).
     */
    @Column(name = "player_id", length = 36)
    private String playerId;

    // ========================================================================
    // ENUMS
    // ========================================================================

    /**
     * Packet direction enum.
     */
    public enum PacketDirection {
        /** Server to Client */
        INBOUND,
        /** Client to Server */
        OUTBOUND
    }

    // ========================================================================
    // LIFECYCLE CALLBACKS
    // ========================================================================

    /**
     * Called before entity is persisted.
     * Ensures timestamp is set.
     */
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Gets the size of raw data in bytes.
     *
     * @return byte array length, or 0 if null
     */
    public int getRawDataSize() {
        return rawData != null ? rawData.length : 0;
    }

    /**
     * Checks if packet has been parsed.
     *
     * @return true if parsed data exists
     */
    public boolean isParsed() {
        return parsedData != null && !parsedData.isEmpty();
    }

    /**
     * Gets a specific field from parsed data.
     *
     * @param key field name
     * @return field value, or null if not found
     */
    public Object getParsedField(String key) {
        return parsedData != null ? parsedData.get(key) : null;
    }

    /**
     * Checks if this is an inbound packet.
     *
     * @return true if direction is INBOUND
     */
    public boolean isInbound() {
        return PacketDirection.INBOUND.equals(direction);
    }

    /**
     * Checks if this is an outbound packet.
     *
     * @return true if direction is OUTBOUND
     */
    public boolean isOutbound() {
        return PacketDirection.OUTBOUND.equals(direction);
    }

    /**
     * Gets a human-readable description of this packet.
     *
     * @return formatted string with packet details
     */
    public String getDescription() {
        return String.format(
            "[%s] %s %s - %d bytes %s",
            timestamp,
            direction,
            packetType,
            getRawDataSize(),
            isParsed() ? "(parsed)" : "(raw only)"
        );
    }
}

/**
 * JPA Converter for JSONB column.
 * Converts between Map<String, Object> and PostgreSQL JSONB.
 */
@Converter
class JsonbConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
        new com.fasterxml.jackson.databind.ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting map to JSON", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            return objectMapper.readValue(dbData, Map.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to map", e);
        }
    }
}
