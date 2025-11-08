package com.dofus.persistence.repository;

import com.dofus.persistence.entity.CombatSession;
import com.dofus.persistence.entity.CombatSession.CombatResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for CombatSession entity.
 *
 * <p>Provides CRUD operations and custom queries for combat session management.
 *
 * @author Agent A2 - Database Architect
 * @version 1.0
 * @since 2025-11-08
 */
@Repository
public interface CombatSessionRepository extends JpaRepository<CombatSession, Long> {

    // ========================================================================
    // QUERY BY PLAYER
    // ========================================================================

    /**
     * Finds all combat sessions for a player.
     *
     * @param playerId player identifier
     * @return list of combat sessions
     */
    @Query("SELECT c FROM CombatSession c JOIN c.player p WHERE p.id = :playerId ORDER BY c.startTime DESC")
    List<CombatSession> findByPlayer(@Param("playerId") String playerId);

    /**
     * Finds combat sessions for a player (paginated).
     *
     * @param playerId player identifier
     * @param pageable pagination parameters
     * @return page of combat sessions
     */
    @Query("SELECT c FROM CombatSession c JOIN c.player p WHERE p.id = :playerId ORDER BY c.startTime DESC")
    Page<CombatSession> findByPlayer(@Param("playerId") String playerId, Pageable pageable);

    /**
     * Finds combat sessions for a player within time range.
     *
     * @param playerId player identifier
     * @param start start of time range
     * @param end end of time range
     * @return list of combat sessions
     */
    @Query("SELECT c FROM CombatSession c JOIN c.player p " +
           "WHERE p.id = :playerId AND c.startTime BETWEEN :start AND :end " +
           "ORDER BY c.startTime DESC")
    List<CombatSession> findByPlayerAndTimeRange(
        @Param("playerId") String playerId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // ========================================================================
    // QUERY BY RESULT
    // ========================================================================

    /**
     * Finds combat sessions by player and result.
     *
     * @param playerId player identifier
     * @param result combat result
     * @return list of combat sessions
     */
    @Query("SELECT c FROM CombatSession c JOIN c.player p " +
           "WHERE p.id = :playerId AND c.result = :result " +
           "ORDER BY c.startTime DESC")
    List<CombatSession> findByPlayerAndResult(
        @Param("playerId") String playerId,
        @Param("result") CombatResult result
    );

    /**
     * Finds all wins for a player.
     *
     * @param playerId player identifier
     * @return list of winning combat sessions
     */
    @Query("SELECT c FROM CombatSession c JOIN c.player p " +
           "WHERE p.id = :playerId AND c.result = 'WIN' " +
           "ORDER BY c.startTime DESC")
    List<CombatSession> findWinsByPlayer(@Param("playerId") String playerId);

    /**
     * Finds all losses for a player.
     *
     * @param playerId player identifier
     * @return list of losing combat sessions
     */
    @Query("SELECT c FROM CombatSession c JOIN c.player p " +
           "WHERE p.id = :playerId AND c.result = 'LOSS' " +
           "ORDER BY c.startTime DESC")
    List<CombatSession> findLossesByPlayer(@Param("playerId") String playerId);

    // ========================================================================
    // QUERY BY MAP
    // ========================================================================

    /**
     * Finds all combat sessions on a specific map.
     *
     * @param mapId map identifier
     * @return list of combat sessions
     */
    @Query("SELECT c FROM CombatSession c JOIN c.map m WHERE m.mapId = :mapId ORDER BY c.startTime DESC")
    List<CombatSession> findByMap(@Param("mapId") Integer mapId);

    /**
     * Finds combat sessions on a map within time range.
     *
     * @param mapId map identifier
     * @param start start of time range
     * @param end end of time range
     * @return list of combat sessions
     */
    @Query("SELECT c FROM CombatSession c JOIN c.map m " +
           "WHERE m.mapId = :mapId AND c.startTime BETWEEN :start AND :end " +
           "ORDER BY c.startTime DESC")
    List<CombatSession> findByMapAndTimeRange(
        @Param("mapId") Integer mapId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // ========================================================================
    // ONGOING COMBATS
    // ========================================================================

    /**
     * Finds all ongoing combat sessions.
     *
     * @return list of ongoing combats
     */
    @Query("SELECT c FROM CombatSession c WHERE c.endTime IS NULL ORDER BY c.startTime DESC")
    List<CombatSession> findOngoingCombats();

    /**
     * Finds ongoing combat for a specific player.
     *
     * @param playerId player identifier
     * @return optional ongoing combat session
     */
    @Query("SELECT c FROM CombatSession c JOIN c.player p " +
           "WHERE p.id = :playerId AND c.endTime IS NULL")
    Optional<CombatSession> findOngoingCombatByPlayer(@Param("playerId") String playerId);

    /**
     * Checks if a player is currently in combat.
     *
     * @param playerId player identifier
     * @return true if player has an ongoing combat
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM CombatSession c JOIN c.player p " +
           "WHERE p.id = :playerId AND c.endTime IS NULL")
    Boolean isPlayerInCombat(@Param("playerId") String playerId);

    /**
     * Counts ongoing combats.
     *
     * @return number of ongoing combats
     */
    @Query("SELECT COUNT(c) FROM CombatSession c WHERE c.endTime IS NULL")
    Long countOngoingCombats();

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Gets win/loss statistics for a player.
     * Returns Object[] with [result, count]
     *
     * @param playerId player identifier
     * @return list of Object[] with combat result statistics
     */
    @Query("SELECT c.result, COUNT(c) FROM CombatSession c JOIN c.player p " +
           "WHERE p.id = :playerId AND c.endTime IS NOT NULL " +
           "GROUP BY c.result")
    List<Object[]> getPlayerCombatStatistics(@Param("playerId") String playerId);

    /**
     * Calculates player win rate.
     *
     * @param playerId player identifier
     * @return win rate as percentage (0-100), or null if no combats
     */
    @Query("SELECT CAST(COUNT(CASE WHEN c.result = 'WIN' THEN 1 END) AS DOUBLE) * 100.0 / " +
           "NULLIF(COUNT(*), 0) FROM CombatSession c JOIN c.player p " +
           "WHERE p.id = :playerId AND c.endTime IS NOT NULL")
    Double getPlayerWinRate(@Param("playerId") String playerId);

    /**
     * Gets average combat duration for a player (in seconds).
     *
     * @param playerId player identifier
     * @return average duration in seconds
     */
    @Query("SELECT AVG(c.durationSeconds) FROM CombatSession c JOIN c.player p " +
           "WHERE p.id = :playerId AND c.durationSeconds IS NOT NULL")
    Double getAverageCombatDuration(@Param("playerId") String playerId);

    /**
     * Gets total experience gained from combats.
     *
     * @param playerId player identifier
     * @return total experience gained
     */
    @Query("SELECT SUM(c.experienceGained) FROM CombatSession c JOIN c.player p WHERE p.id = :playerId")
    Long getTotalExperienceGained(@Param("playerId") String playerId);

    /**
     * Gets total kamas gained from combats.
     *
     * @param playerId player identifier
     * @return total kamas gained
     */
    @Query("SELECT SUM(c.kamasGained) FROM CombatSession c JOIN c.player p WHERE p.id = :playerId")
    Long getTotalKamasGained(@Param("playerId") String playerId);

    /**
     * Gets total items looted from combats.
     *
     * @param playerId player identifier
     * @return total items looted
     */
    @Query("SELECT SUM(c.itemsLooted) FROM CombatSession c JOIN c.player p WHERE p.id = :playerId")
    Long getTotalItemsLooted(@Param("playerId") String playerId);

    // ========================================================================
    // TIME-BASED QUERIES
    // ========================================================================

    /**
     * Finds combat sessions within time range (paginated).
     *
     * @param start start of time range
     * @param end end of time range
     * @param pageable pagination parameters
     * @return page of combat sessions
     */
    Page<CombatSession> findByStartTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Finds recent combat sessions.
     *
     * @param since minimum start time
     * @param pageable pagination parameters
     * @return page of recent combats
     */
    @Query("SELECT c FROM CombatSession c WHERE c.startTime > :since ORDER BY c.startTime DESC")
    Page<CombatSession> findRecentCombats(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Finds longest combats by duration.
     *
     * @param limit maximum number of results
     * @return list of longest combats
     */
    @Query("SELECT c FROM CombatSession c WHERE c.durationSeconds IS NOT NULL " +
           "ORDER BY c.durationSeconds DESC")
    List<CombatSession> findLongestCombats(@Param("limit") int limit);

    // ========================================================================
    // JOINS WITH ACTIONS
    // ========================================================================

    /**
     * Finds a combat session with actions eagerly loaded.
     *
     * @param id combat session ID
     * @return optional combat session with actions
     */
    @Query("SELECT c FROM CombatSession c LEFT JOIN FETCH c.actions WHERE c.id = :id")
    Optional<CombatSession> findByIdWithActions(@Param("id") Long id);

    /**
     * Finds a combat session with all relationships loaded.
     *
     * @param id combat session ID
     * @return optional combat session with player, map, and actions
     */
    @Query("SELECT DISTINCT c FROM CombatSession c " +
           "LEFT JOIN FETCH c.player " +
           "LEFT JOIN FETCH c.map " +
           "LEFT JOIN FETCH c.actions " +
           "WHERE c.id = :id")
    Optional<CombatSession> findByIdWithAllRelationships(@Param("id") Long id);

    // ========================================================================
    // COUNTING
    // ========================================================================

    /**
     * Counts combat sessions for a player.
     *
     * @param playerId player identifier
     * @return combat count
     */
    @Query("SELECT COUNT(c) FROM CombatSession c JOIN c.player p WHERE p.id = :playerId")
    Long countByPlayer(@Param("playerId") String playerId);

    /**
     * Counts wins for a player.
     *
     * @param playerId player identifier
     * @return win count
     */
    @Query("SELECT COUNT(c) FROM CombatSession c JOIN c.player p " +
           "WHERE p.id = :playerId AND c.result = 'WIN'")
    Long countWinsByPlayer(@Param("playerId") String playerId);

    /**
     * Counts losses for a player.
     *
     * @param playerId player identifier
     * @return loss count
     */
    @Query("SELECT COUNT(c) FROM CombatSession c JOIN c.player p " +
           "WHERE p.id = :playerId AND c.result = 'LOSS'")
    Long countLossesByPlayer(@Param("playerId") String playerId);

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Ends all ongoing combats for a player (force end).
     *
     * @param playerId player identifier
     * @param endTime end timestamp
     * @param result final result
     * @return number of combats updated
     */
    @Modifying
    @Query("UPDATE CombatSession c SET c.endTime = :endTime, c.result = :result " +
           "WHERE c.player.id = :playerId AND c.endTime IS NULL")
    int endOngoingCombats(
        @Param("playerId") String playerId,
        @Param("endTime") LocalDateTime endTime,
        @Param("result") CombatResult result
    );

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Deletes combat sessions older than threshold.
     *
     * @param threshold delete combats before this time
     * @return number of combats deleted
     */
    @Modifying
    @Query("DELETE FROM CombatSession c WHERE c.startTime < :threshold")
    int deleteOldCombats(@Param("threshold") LocalDateTime threshold);
}
