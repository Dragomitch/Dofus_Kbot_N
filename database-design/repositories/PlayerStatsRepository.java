package com.dofus.persistence.repository;

import com.dofus.persistence.entity.PlayerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for PlayerStats entity.
 *
 * <p>Provides CRUD operations and custom queries for player statistics.
 *
 * @author Agent A2 - Database Architect
 * @version 1.0
 * @since 2025-11-08
 */
@Repository
public interface PlayerStatsRepository extends JpaRepository<PlayerStats, Long> {

    // ========================================================================
    // QUERY BY PLAYER
    // ========================================================================

    /**
     * Finds stats for a specific player.
     *
     * @param playerId player identifier
     * @return optional player stats
     */
    @Query("SELECT s FROM PlayerStats s WHERE s.player.id = :playerId")
    Optional<PlayerStats> findByPlayer(@Param("playerId") String playerId);

    /**
     * Finds stats for a player with player entity eagerly loaded.
     *
     * @param playerId player identifier
     * @return optional player stats with player
     */
    @Query("SELECT s FROM PlayerStats s JOIN FETCH s.player WHERE s.player.id = :playerId")
    Optional<PlayerStats> findByPlayerWithPlayer(@Param("playerId") String playerId);

    /**
     * Checks if stats exist for a player.
     *
     * @param playerId player identifier
     * @return true if stats exist
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM PlayerStats s " +
           "WHERE s.player.id = :playerId")
    Boolean existsByPlayer(@Param("playerId") String playerId);

    // ========================================================================
    // QUERY BY HEALTH
    // ========================================================================

    /**
     * Finds players with full health.
     *
     * @return list of stats with full HP
     */
    @Query("SELECT s FROM PlayerStats s WHERE s.hp = s.maxHp AND s.maxHp > 0")
    List<PlayerStats> findPlayersWithFullHealth();

    /**
     * Finds players with low health (below percentage threshold).
     *
     * @param threshold health percentage threshold (0-100)
     * @return list of stats with low HP
     */
    @Query("SELECT s FROM PlayerStats s WHERE " +
           "CAST(s.hp AS DOUBLE) * 100.0 / NULLIF(s.maxHp, 0) < :threshold AND s.maxHp > 0")
    List<PlayerStats> findPlayersWithLowHealth(@Param("threshold") double threshold);

    /**
     * Finds dead players (HP = 0).
     *
     * @return list of stats with 0 HP
     */
    @Query("SELECT s FROM PlayerStats s WHERE s.hp = 0")
    List<PlayerStats> findDeadPlayers();

    // ========================================================================
    // QUERY BY STATS
    // ========================================================================

    /**
     * Finds players with strength above threshold.
     *
     * @param minStrength minimum strength
     * @return list of stats
     */
    @Query("SELECT s FROM PlayerStats s WHERE s.strength >= :min ORDER BY s.strength DESC")
    List<PlayerStats> findByMinStrength(@Param("min") Integer minStrength);

    /**
     * Finds players with intelligence above threshold.
     *
     * @param minIntelligence minimum intelligence
     * @return list of stats
     */
    @Query("SELECT s FROM PlayerStats s WHERE s.intelligence >= :min ORDER BY s.intelligence DESC")
    List<PlayerStats> findByMinIntelligence(@Param("min") Integer minIntelligence);

    /**
     * Finds players with highest total stats.
     *
     * @param limit maximum number of results
     * @return list of top players by total stats
     */
    @Query("SELECT s FROM PlayerStats s ORDER BY " +
           "(s.strength + s.intelligence + s.agility + s.vitality + s.wisdom + s.chance) DESC")
    List<PlayerStats> findTopPlayersByTotalStats(@Param("limit") int limit);

    // ========================================================================
    // QUERY BY KAMAS
    // ========================================================================

    /**
     * Finds richest players by kamas.
     *
     * @param limit maximum number of results
     * @return list of richest players
     */
    @Query("SELECT s FROM PlayerStats s ORDER BY s.kamas DESC")
    List<PlayerStats> findRichestPlayers(@Param("limit") int limit);

    /**
     * Finds players with kamas above threshold.
     *
     * @param minKamas minimum kamas
     * @return list of stats
     */
    @Query("SELECT s FROM PlayerStats s WHERE s.kamas >= :min ORDER BY s.kamas DESC")
    List<PlayerStats> findByMinKamas(@Param("min") Long minKamas);

    /**
     * Gets total kamas across all players.
     *
     * @return total kamas in circulation
     */
    @Query("SELECT COALESCE(SUM(s.kamas), 0) FROM PlayerStats s")
    Long getTotalKamasInCirculation();

    /**
     * Gets average kamas per player.
     *
     * @return average kamas
     */
    @Query("SELECT AVG(s.kamas) FROM PlayerStats s")
    Double getAverageKamas();

    // ========================================================================
    // QUERY BY ACTION/MOVEMENT POINTS
    // ========================================================================

    /**
     * Finds players with maximum AP.
     *
     * @return list of stats
     */
    @Query("SELECT s FROM PlayerStats s WHERE s.ap = s.maxAp AND s.maxAp > 0")
    List<PlayerStats> findPlayersWithFullAp();

    /**
     * Finds players with maximum MP.
     *
     * @return list of stats
     */
    @Query("SELECT s FROM PlayerStats s WHERE s.mp = s.maxMp AND s.maxMp > 0")
    List<PlayerStats> findPlayersWithFullMp();

    /**
     * Finds players with no AP remaining.
     *
     * @return list of stats
     */
    @Query("SELECT s FROM PlayerStats s WHERE s.ap = 0")
    List<PlayerStats> findPlayersWithNoAp();

    // ========================================================================
    // QUERY BY RESISTANCES
    // ========================================================================

    /**
     * Finds players with highest average resistance.
     *
     * @param limit maximum number of results
     * @return list of top players by average resistance
     */
    @Query("SELECT s FROM PlayerStats s ORDER BY " +
           "(s.neutralResistance + s.earthResistance + s.fireResistance + " +
           "s.waterResistance + s.airResistance) / 5.0 DESC")
    List<PlayerStats> findTopPlayersByResistance(@Param("limit") int limit);

    /**
     * Gets average resistance across all players.
     *
     * @return average resistance value
     */
    @Query("SELECT AVG((s.neutralResistance + s.earthResistance + s.fireResistance + " +
           "s.waterResistance + s.airResistance) / 5.0) FROM PlayerStats s")
    Double getAverageResistance();

    // ========================================================================
    // TIME-BASED QUERIES
    // ========================================================================

    /**
     * Finds recently updated stats.
     *
     * @param since minimum update time
     * @return list of recently updated stats
     */
    @Query("SELECT s FROM PlayerStats s WHERE s.updatedAt > :since ORDER BY s.updatedAt DESC")
    List<PlayerStats> findRecentlyUpdated(@Param("since") LocalDateTime since);

    /**
     * Finds stats not updated since threshold (stale data).
     *
     * @param threshold staleness threshold
     * @return list of stale stats
     */
    @Query("SELECT s FROM PlayerStats s WHERE s.updatedAt < :threshold ORDER BY s.updatedAt ASC")
    List<PlayerStats> findStaleStats(@Param("threshold") LocalDateTime threshold);

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Gets stat distribution averages.
     *
     * @return Object[] with [avgStr, avgInt, avgAgi, avgVit, avgWis, avgCha]
     */
    @Query("SELECT AVG(s.strength), AVG(s.intelligence), AVG(s.agility), " +
           "AVG(s.vitality), AVG(s.wisdom), AVG(s.chance) FROM PlayerStats s")
    Object[] getAverageStatDistribution();

    /**
     * Gets health statistics.
     *
     * @return Object[] with [avgHp, avgMaxHp, maxHp]
     */
    @Query("SELECT AVG(s.hp), AVG(s.maxHp), MAX(s.maxHp) FROM PlayerStats s")
    Object[] getHealthStatistics();

    /**
     * Gets action point statistics.
     *
     * @return Object[] with [avgAp, avgMaxAp, maxMaxAp]
     */
    @Query("SELECT AVG(s.ap), AVG(s.maxAp), MAX(s.maxAp) FROM PlayerStats s")
    Object[] getApStatistics();

    /**
     * Counts players by primary stat (highest stat).
     *
     * @return list of Object[] with [statName, count]
     */
    @Query(value = "SELECT " +
           "CASE " +
           "  WHEN strength >= GREATEST(intelligence, agility, chance) THEN 'STRENGTH' " +
           "  WHEN intelligence >= GREATEST(strength, agility, chance) THEN 'INTELLIGENCE' " +
           "  WHEN agility >= GREATEST(strength, intelligence, chance) THEN 'AGILITY' " +
           "  ELSE 'CHANCE' " +
           "END as primary_stat, " +
           "COUNT(*) " +
           "FROM player_stats " +
           "GROUP BY primary_stat " +
           "ORDER BY COUNT(*) DESC",
           nativeQuery = true)
    List<Object[]> getPrimaryStatDistribution();

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Updates health points for a player.
     *
     * @param playerId player identifier
     * @param hp new HP value
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE PlayerStats s SET s.hp = :hp WHERE s.player.id = :playerId")
    int updateHp(@Param("playerId") String playerId, @Param("hp") Integer hp);

    /**
     * Updates action points for a player.
     *
     * @param playerId player identifier
     * @param ap new AP value
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE PlayerStats s SET s.ap = :ap WHERE s.player.id = :playerId")
    int updateAp(@Param("playerId") String playerId, @Param("ap") Integer ap);

    /**
     * Updates movement points for a player.
     *
     * @param playerId player identifier
     * @param mp new MP value
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE PlayerStats s SET s.mp = :mp WHERE s.player.id = :playerId")
    int updateMp(@Param("playerId") String playerId, @Param("mp") Integer mp);

    /**
     * Updates kamas for a player.
     *
     * @param playerId player identifier
     * @param kamas new kamas value
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE PlayerStats s SET s.kamas = :kamas WHERE s.player.id = :playerId")
    int updateKamas(@Param("playerId") String playerId, @Param("kamas") Long kamas);

    /**
     * Restores AP and MP to maximum for a player.
     *
     * @param playerId player identifier
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE PlayerStats s SET s.ap = s.maxAp, s.mp = s.maxMp WHERE s.player.id = :playerId")
    int restoreApMp(@Param("playerId") String playerId);

    /**
     * Fully heals a player.
     *
     * @param playerId player identifier
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE PlayerStats s SET s.hp = s.maxHp WHERE s.player.id = :playerId")
    int fullyHeal(@Param("playerId") String playerId);

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Deletes stats for a specific player.
     *
     * @param playerId player identifier
     * @return number of rows deleted
     */
    @Modifying
    @Query("DELETE FROM PlayerStats s WHERE s.player.id = :playerId")
    int deleteByPlayer(@Param("playerId") String playerId);
}
