package com.dofus.persistence.repository;

import com.dofus.persistence.entity.PacketLog;
import com.dofus.persistence.entity.PacketLog.PacketDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA Repository for PacketLog entity.
 *
 * <p>Handles queries for the partitioned packets table. All queries should
 * include timestamp filters when possible to leverage partition pruning.
 *
 * <p>Features:
 * <ul>
 *   <li>Time-range queries for partition efficiency</li>
 *   <li>Packet type filtering</li>
 *   <li>Session-based packet retrieval</li>
 *   <li>Pagination support for large result sets</li>
 *   <li>Statistics aggregation queries</li>
 * </ul>
 *
 * @author Agent A2 - Database Architect
 * @version 1.0
 * @since 2025-11-08
 */
@Repository
public interface PacketLogRepository extends JpaRepository<PacketLog, Long> {

    // ========================================================================
    // QUERY BY PACKET TYPE
    // ========================================================================

    /**
     * Finds all packets of a specific type.
     * Note: Can be slow without timestamp filter. Use paginated version.
     *
     * @param packetType packet type ID (e.g., "GDM", "AA")
     * @return list of packets
     */
    List<PacketLog> findByPacketType(String packetType);

    /**
     * Finds packets of a specific type within a time range (paginated).
     * Recommended for production use due to partition pruning.
     *
     * @param packetType packet type ID
     * @param start start of time range
     * @param end end of time range
     * @param pageable pagination parameters
     * @return page of packets
     */
    Page<PacketLog> findByPacketTypeAndTimestampBetween(
        String packetType,
        LocalDateTime start,
        LocalDateTime end,
        Pageable pageable
    );

    /**
     * Finds packets of a specific type after a timestamp.
     *
     * @param packetType packet type ID
     * @param since minimum timestamp
     * @return list of packets
     */
    List<PacketLog> findByPacketTypeAndTimestampAfter(String packetType, LocalDateTime since);

    // ========================================================================
    // QUERY BY DIRECTION
    // ========================================================================

    /**
     * Finds all inbound packets (server -> client) within time range.
     *
     * @param start start of time range
     * @param end end of time range
     * @param pageable pagination parameters
     * @return page of inbound packets
     */
    Page<PacketLog> findByDirectionAndTimestampBetween(
        PacketDirection direction,
        LocalDateTime start,
        LocalDateTime end,
        Pageable pageable
    );

    /**
     * Finds packets by type and direction within time range.
     *
     * @param packetType packet type ID
     * @param direction packet direction
     * @param start start of time range
     * @param end end of time range
     * @return list of packets
     */
    List<PacketLog> findByPacketTypeAndDirectionAndTimestampBetween(
        String packetType,
        PacketDirection direction,
        LocalDateTime start,
        LocalDateTime end
    );

    // ========================================================================
    // QUERY BY SESSION
    // ========================================================================

    /**
     * Finds all packets for a specific session, ordered by timestamp.
     * Useful for replaying session packet sequence.
     *
     * @param sessionId session identifier
     * @return list of packets in chronological order
     */
    List<PacketLog> findBySessionIdOrderByTimestampAsc(String sessionId);

    /**
     * Finds packets for a session within a time range.
     *
     * @param sessionId session identifier
     * @param start start of time range
     * @param end end of time range
     * @return list of packets
     */
    List<PacketLog> findBySessionIdAndTimestampBetween(
        String sessionId,
        LocalDateTime start,
        LocalDateTime end
    );

    /**
     * Counts packets in a session.
     *
     * @param sessionId session identifier
     * @return packet count
     */
    Long countBySessionId(String sessionId);

    // ========================================================================
    // QUERY BY PLAYER
    // ========================================================================

    /**
     * Finds recent packets for a specific player.
     * Uses timestamp filter for partition efficiency.
     *
     * @param playerId player identifier
     * @param since minimum timestamp
     * @return list of packets ordered by timestamp descending
     */
    @Query("SELECT p FROM PacketLog p WHERE p.playerId = :playerId AND p.timestamp > :since " +
           "ORDER BY p.timestamp DESC")
    List<PacketLog> findRecentByPlayer(@Param("playerId") String playerId, @Param("since") LocalDateTime since);

    /**
     * Finds packets for a player within time range (paginated).
     *
     * @param playerId player identifier
     * @param start start of time range
     * @param end end of time range
     * @param pageable pagination parameters
     * @return page of packets
     */
    Page<PacketLog> findByPlayerIdAndTimestampBetween(
        String playerId,
        LocalDateTime start,
        LocalDateTime end,
        Pageable pageable
    );

    /**
     * Counts packets for a player within time range.
     *
     * @param playerId player identifier
     * @param start start of time range
     * @param end end of time range
     * @return packet count
     */
    Long countByPlayerIdAndTimestampBetween(String playerId, LocalDateTime start, LocalDateTime end);

    // ========================================================================
    // TIME-BASED QUERIES
    // ========================================================================

    /**
     * Finds all packets within a time range (paginated).
     * Essential for efficient partition pruning.
     *
     * @param start start of time range
     * @param end end of time range
     * @param pageable pagination parameters
     * @return page of packets
     */
    Page<PacketLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Finds recent packets (last N hours).
     *
     * @param since minimum timestamp
     * @param pageable pagination parameters
     * @return page of packets
     */
    @Query("SELECT p FROM PacketLog p WHERE p.timestamp > :since ORDER BY p.timestamp DESC")
    Page<PacketLog> findRecent(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Finds oldest packets in database.
     *
     * @param limit maximum number of results
     * @return list of oldest packets
     */
    @Query("SELECT p FROM PacketLog p ORDER BY p.timestamp ASC")
    List<PacketLog> findOldestPackets(@Param("limit") int limit);

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Gets packet type distribution (count by type).
     *
     * @return list of Object[] with [packetType, count]
     */
    @Query("SELECT p.packetType, COUNT(p) FROM PacketLog p GROUP BY p.packetType ORDER BY COUNT(p) DESC")
    List<Object[]> getPacketTypeStatistics();

    /**
     * Gets packet type distribution within time range.
     *
     * @param start start of time range
     * @param end end of time range
     * @return list of Object[] with [packetType, count]
     */
    @Query("SELECT p.packetType, COUNT(p) FROM PacketLog p " +
           "WHERE p.timestamp BETWEEN :start AND :end " +
           "GROUP BY p.packetType ORDER BY COUNT(p) DESC")
    List<Object[]> getPacketTypeStatisticsByTimeRange(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * Gets direction distribution (inbound vs outbound counts).
     *
     * @param start start of time range
     * @param end end of time range
     * @return list of Object[] with [direction, count]
     */
    @Query("SELECT p.direction, COUNT(p) FROM PacketLog p " +
           "WHERE p.timestamp BETWEEN :start AND :end " +
           "GROUP BY p.direction")
    List<Object[]> getDirectionStatistics(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * Counts packets by type within time range.
     *
     * @param packetType packet type ID
     * @param start start of time range
     * @param end end of time range
     * @return packet count
     */
    Long countByPacketTypeAndTimestampBetween(String packetType, LocalDateTime start, LocalDateTime end);

    /**
     * Gets hourly packet count for the last 24 hours.
     *
     * @param since start time (24 hours ago)
     * @return list of Object[] with [hour, count]
     */
    @Query("SELECT FUNCTION('DATE_TRUNC', 'hour', p.timestamp), COUNT(p) FROM PacketLog p " +
           "WHERE p.timestamp > :since " +
           "GROUP BY FUNCTION('DATE_TRUNC', 'hour', p.timestamp) " +
           "ORDER BY FUNCTION('DATE_TRUNC', 'hour', p.timestamp) ASC")
    List<Object[]> getHourlyPacketCounts(@Param("since") LocalDateTime since);

    /**
     * Gets average packet size by type.
     *
     * @param start start of time range
     * @param end end of time range
     * @return list of Object[] with [packetType, avgSize]
     */
    @Query("SELECT p.packetType, AVG(LENGTH(p.rawData)) FROM PacketLog p " +
           "WHERE p.timestamp BETWEEN :start AND :end " +
           "GROUP BY p.packetType ORDER BY AVG(LENGTH(p.rawData)) DESC")
    List<Object[]> getAveragePacketSizeByType(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // ========================================================================
    // PARSED DATA QUERIES (JSONB)
    // ========================================================================

    /**
     * Finds packets with specific JSON field value.
     * Uses JSONB operators for efficient querying.
     *
     * Example: Find all GDM packets for map ID 432
     * findByParsedDataContaining("mapId", 432)
     *
     * @param jsonPath JSONB path expression
     * @return list of matching packets
     */
    @Query(value = "SELECT * FROM packets WHERE parsed_data @> CAST(:jsonPath AS jsonb) " +
                   "ORDER BY timestamp DESC LIMIT 100",
           nativeQuery = true)
    List<PacketLog> findByParsedDataContaining(@Param("jsonPath") String jsonPath);

    /**
     * Checks if any packet with specific parsed data exists.
     *
     * @param jsonPath JSONB path expression
     * @return true if exists
     */
    @Query(value = "SELECT EXISTS(SELECT 1 FROM packets WHERE parsed_data @> CAST(:jsonPath AS jsonb))",
           nativeQuery = true)
    Boolean existsByParsedData(@Param("jsonPath") String jsonPath);

    // ========================================================================
    // DELETE QUERIES
    // ========================================================================

    /**
     * Deletes packets older than a specified timestamp.
     * Used for data retention policies.
     *
     * @param threshold delete packets before this time
     * @return number of packets deleted
     */
    @Modifying
    @Query("DELETE FROM PacketLog p WHERE p.timestamp < :threshold")
    int deleteOldPackets(@Param("threshold") LocalDateTime threshold);

    /**
     * Deletes packets for a specific session.
     *
     * @param sessionId session identifier
     * @return number of packets deleted
     */
    @Modifying
    int deleteBySessionId(String sessionId);

    /**
     * Deletes packets by type within time range.
     *
     * @param packetType packet type ID
     * @param start start of time range
     * @param end end of time range
     * @return number of packets deleted
     */
    @Modifying
    @Query("DELETE FROM PacketLog p WHERE p.packetType = :type " +
           "AND p.timestamp BETWEEN :start AND :end")
    int deleteByTypeAndTimeRange(
        @Param("type") String packetType,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // ========================================================================
    // SPECIAL QUERIES
    // ========================================================================

    /**
     * Finds the first packet of each type in a session.
     * Useful for session initialization analysis.
     *
     * @param sessionId session identifier
     * @return list of first occurrence packets
     */
    @Query("SELECT p FROM PacketLog p WHERE p.sessionId = :sessionId " +
           "AND p.timestamp = (SELECT MIN(p2.timestamp) FROM PacketLog p2 " +
           "WHERE p2.sessionId = :sessionId AND p2.packetType = p.packetType)")
    List<PacketLog> findFirstPacketOfEachType(@Param("sessionId") String sessionId);

    /**
     * Finds packets with raw data size exceeding threshold.
     * Useful for identifying large packets.
     *
     * @param minSize minimum packet size in bytes
     * @param start start of time range
     * @param end end of time range
     * @return list of large packets
     */
    @Query("SELECT p FROM PacketLog p WHERE LENGTH(p.rawData) > :minSize " +
           "AND p.timestamp BETWEEN :start AND :end " +
           "ORDER BY LENGTH(p.rawData) DESC")
    List<PacketLog> findLargePackets(
        @Param("minSize") int minSize,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * Gets total storage size of packets in bytes.
     *
     * @param start start of time range
     * @param end end of time range
     * @return total size in bytes
     */
    @Query("SELECT SUM(LENGTH(p.rawData)) FROM PacketLog p " +
           "WHERE p.timestamp BETWEEN :start AND :end")
    Long getTotalPacketSize(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
