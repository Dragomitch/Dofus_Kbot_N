package com.dofus.persistence.repository;

import com.dofus.persistence.entity.CombatAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA Repository for CombatAction entity.
 *
 * <p>Provides CRUD operations and custom queries for combat action tracking.
 *
 * @author Agent A2 - Database Architect
 * @version 1.0
 * @since 2025-11-08
 */
@Repository
public interface CombatActionRepository extends JpaRepository<CombatAction, Long> {

    // ========================================================================
    // QUERY BY COMBAT SESSION
    // ========================================================================

    /**
     * Finds all actions for a specific combat session.
     *
     * @param combatSessionId combat session ID
     * @return list of actions ordered by turn and timestamp
     */
    @Query("SELECT a FROM CombatAction a WHERE a.combatSession.id = :sessionId " +
           "ORDER BY a.turnNumber ASC, a.timestamp ASC")
    List<CombatAction> findByCombatSession(@Param("sessionId") Long combatSessionId);

    /**
     * Finds actions for a combat session by turn number.
     *
     * @param combatSessionId combat session ID
     * @param turnNumber turn number
     * @return list of actions for that turn
     */
    @Query("SELECT a FROM CombatAction a WHERE a.combatSession.id = :sessionId AND a.turnNumber = :turn " +
           "ORDER BY a.timestamp ASC")
    List<CombatAction> findByCombatSessionAndTurn(
        @Param("sessionId") Long combatSessionId,
        @Param("turn") Integer turnNumber
    );

    /**
     * Counts actions in a combat session.
     *
     * @param combatSessionId combat session ID
     * @return action count
     */
    @Query("SELECT COUNT(a) FROM CombatAction a WHERE a.combatSession.id = :sessionId")
    Long countByCombatSession(@Param("sessionId") Long combatSessionId);

    /**
     * Gets the maximum turn number for a combat session.
     *
     * @param combatSessionId combat session ID
     * @return maximum turn number, or 0 if no actions
     */
    @Query("SELECT COALESCE(MAX(a.turnNumber), 0) FROM CombatAction a WHERE a.combatSession.id = :sessionId")
    Integer getMaxTurnNumber(@Param("sessionId") Long combatSessionId);

    // ========================================================================
    // QUERY BY ACTION TYPE
    // ========================================================================

    /**
     * Finds actions by type for a combat session.
     *
     * @param combatSessionId combat session ID
     * @param actionType action type (CAST_SPELL, MOVE, etc.)
     * @return list of actions
     */
    @Query("SELECT a FROM CombatAction a WHERE a.combatSession.id = :sessionId AND a.actionType = :type " +
           "ORDER BY a.turnNumber ASC")
    List<CombatAction> findByCombatSessionAndActionType(
        @Param("sessionId") Long combatSessionId,
        @Param("type") String actionType
    );

    /**
     * Finds all spell cast actions for a combat session.
     *
     * @param combatSessionId combat session ID
     * @return list of spell cast actions
     */
    @Query("SELECT a FROM CombatAction a WHERE a.combatSession.id = :sessionId AND a.actionType = 'CAST_SPELL' " +
           "ORDER BY a.turnNumber ASC")
    List<CombatAction> findSpellCastActions(@Param("sessionId") Long combatSessionId);

    /**
     * Finds all movement actions for a combat session.
     *
     * @param combatSessionId combat session ID
     * @return list of movement actions
     */
    @Query("SELECT a FROM CombatAction a WHERE a.combatSession.id = :sessionId AND a.actionType = 'MOVE' " +
           "ORDER BY a.turnNumber ASC")
    List<CombatAction> findMovementActions(@Param("sessionId") Long combatSessionId);

    // ========================================================================
    // QUERY BY SPELL
    // ========================================================================

    /**
     * Finds actions for a specific spell.
     *
     * @param spellId spell ID
     * @return list of actions
     */
    List<CombatAction> findBySpellId(Integer spellId);

    /**
     * Finds actions for a specific spell in a combat session.
     *
     * @param combatSessionId combat session ID
     * @param spellId spell ID
     * @return list of actions
     */
    @Query("SELECT a FROM CombatAction a WHERE a.combatSession.id = :sessionId AND a.spellId = :spellId " +
           "ORDER BY a.turnNumber ASC")
    List<CombatAction> findByCombatSessionAndSpell(
        @Param("sessionId") Long combatSessionId,
        @Param("spellId") Integer spellId
    );

    /**
     * Counts how many times a spell was cast in a combat.
     *
     * @param combatSessionId combat session ID
     * @param spellId spell ID
     * @return cast count
     */
    @Query("SELECT COUNT(a) FROM CombatAction a " +
           "WHERE a.combatSession.id = :sessionId AND a.spellId = :spellId")
    Long countSpellCasts(@Param("sessionId") Long combatSessionId, @Param("spellId") Integer spellId);

    // ========================================================================
    // DAMAGE QUERIES
    // ========================================================================

    /**
     * Finds all actions that dealt damage in a combat.
     *
     * @param combatSessionId combat session ID
     * @return list of damage-dealing actions
     */
    @Query("SELECT a FROM CombatAction a WHERE a.combatSession.id = :sessionId AND a.damageDealt > 0 " +
           "ORDER BY a.damageDealt DESC")
    List<CombatAction> findDamageActions(@Param("sessionId") Long combatSessionId);

    /**
     * Gets total damage dealt in a combat session.
     *
     * @param combatSessionId combat session ID
     * @return total damage
     */
    @Query("SELECT COALESCE(SUM(a.damageDealt), 0) FROM CombatAction a WHERE a.combatSession.id = :sessionId")
    Long getTotalDamageDealt(@Param("sessionId") Long combatSessionId);

    /**
     * Gets highest damage dealt in a single action.
     *
     * @param combatSessionId combat session ID
     * @return maximum damage value
     */
    @Query("SELECT COALESCE(MAX(a.damageDealt), 0) FROM CombatAction a WHERE a.combatSession.id = :sessionId")
    Integer getMaxDamageDealt(@Param("sessionId") Long combatSessionId);

    /**
     * Finds critical hit actions.
     *
     * @param combatSessionId combat session ID
     * @return list of critical hits
     */
    @Query("SELECT a FROM CombatAction a WHERE a.combatSession.id = :sessionId AND a.criticalHit = TRUE " +
           "ORDER BY a.damageDealt DESC")
    List<CombatAction> findCriticalHits(@Param("sessionId") Long combatSessionId);

    /**
     * Counts critical hits in a combat.
     *
     * @param combatSessionId combat session ID
     * @return critical hit count
     */
    @Query("SELECT COUNT(a) FROM CombatAction a WHERE a.combatSession.id = :sessionId AND a.criticalHit = TRUE")
    Long countCriticalHits(@Param("sessionId") Long combatSessionId);

    // ========================================================================
    // RESOURCE COST QUERIES
    // ========================================================================

    /**
     * Gets total AP spent in a combat.
     *
     * @param combatSessionId combat session ID
     * @return total AP cost
     */
    @Query("SELECT COALESCE(SUM(a.apCost), 0) FROM CombatAction a WHERE a.combatSession.id = :sessionId")
    Integer getTotalApSpent(@Param("sessionId") Long combatSessionId);

    /**
     * Gets total MP spent in a combat.
     *
     * @param combatSessionId combat session ID
     * @return total MP cost
     */
    @Query("SELECT COALESCE(SUM(a.mpCost), 0) FROM CombatAction a WHERE a.combatSession.id = :sessionId")
    Integer getTotalMpSpent(@Param("sessionId") Long combatSessionId);

    /**
     * Gets average AP cost per action.
     *
     * @param combatSessionId combat session ID
     * @return average AP cost
     */
    @Query("SELECT AVG(a.apCost) FROM CombatAction a " +
           "WHERE a.combatSession.id = :sessionId AND a.apCost > 0")
    Double getAverageApCost(@Param("sessionId") Long combatSessionId);

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Gets action type distribution for a combat.
     *
     * @param combatSessionId combat session ID
     * @return list of Object[] with [actionType, count]
     */
    @Query("SELECT a.actionType, COUNT(a) FROM CombatAction a " +
           "WHERE a.combatSession.id = :sessionId " +
           "GROUP BY a.actionType ORDER BY COUNT(a) DESC")
    List<Object[]> getActionTypeDistribution(@Param("sessionId") Long combatSessionId);

    /**
     * Gets spell usage statistics for a combat.
     *
     * @param combatSessionId combat session ID
     * @return list of Object[] with [spellId, spellName, count]
     */
    @Query("SELECT a.spellId, a.spellName, COUNT(a) FROM CombatAction a " +
           "WHERE a.combatSession.id = :sessionId AND a.spellId IS NOT NULL " +
           "GROUP BY a.spellId, a.spellName ORDER BY COUNT(a) DESC")
    List<Object[]> getSpellUsageStatistics(@Param("sessionId") Long combatSessionId);

    /**
     * Gets damage per turn.
     *
     * @param combatSessionId combat session ID
     * @return list of Object[] with [turnNumber, totalDamage]
     */
    @Query("SELECT a.turnNumber, SUM(a.damageDealt) FROM CombatAction a " +
           "WHERE a.combatSession.id = :sessionId " +
           "GROUP BY a.turnNumber ORDER BY a.turnNumber ASC")
    List<Object[]> getDamagePerTurn(@Param("sessionId") Long combatSessionId);

    // ========================================================================
    // TIME-BASED QUERIES
    // ========================================================================

    /**
     * Finds recent actions across all combats.
     *
     * @param since minimum timestamp
     * @return list of recent actions
     */
    @Query("SELECT a FROM CombatAction a WHERE a.timestamp > :since ORDER BY a.timestamp DESC")
    List<CombatAction> findRecentActions(@Param("since") LocalDateTime since);

    /**
     * Finds actions within time range.
     *
     * @param start start of time range
     * @param end end of time range
     * @return list of actions
     */
    List<CombatAction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // ========================================================================
    // GLOBAL SPELL STATISTICS
    // ========================================================================

    /**
     * Gets global spell usage statistics (across all combats).
     *
     * @return list of Object[] with [spellId, spellName, count, avgDamage]
     */
    @Query("SELECT a.spellId, a.spellName, COUNT(a), AVG(a.damageDealt) FROM CombatAction a " +
           "WHERE a.spellId IS NOT NULL " +
           "GROUP BY a.spellId, a.spellName ORDER BY COUNT(a) DESC")
    List<Object[]> getGlobalSpellStatistics();

    /**
     * Gets most damaging spell (by total damage).
     *
     * @return list of Object[] with [spellId, spellName, totalDamage]
     */
    @Query("SELECT a.spellId, a.spellName, SUM(a.damageDealt) FROM CombatAction a " +
           "WHERE a.spellId IS NOT NULL " +
           "GROUP BY a.spellId, a.spellName ORDER BY SUM(a.damageDealt) DESC")
    List<Object[]> getMostDamagingSpells();

    /**
     * Gets spell critical hit rates.
     *
     * @return list of Object[] with [spellId, spellName, critRate]
     */
    @Query("SELECT a.spellId, a.spellName, " +
           "CAST(COUNT(CASE WHEN a.criticalHit = TRUE THEN 1 END) AS DOUBLE) * 100.0 / COUNT(*) " +
           "FROM CombatAction a WHERE a.spellId IS NOT NULL " +
           "GROUP BY a.spellId, a.spellName HAVING COUNT(*) >= 10 " +
           "ORDER BY CAST(COUNT(CASE WHEN a.criticalHit = TRUE THEN 1 END) AS DOUBLE) * 100.0 / COUNT(*) DESC")
    List<Object[]> getSpellCriticalHitRates();
}
