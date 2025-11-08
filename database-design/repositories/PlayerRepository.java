package com.dofus.persistence.repository;

import com.dofus.persistence.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Player entity.
 *
 * <p>Provides CRUD operations and custom queries for player management.
 *
 * <p>Features:
 * <ul>
 *   <li>Standard CRUD operations (inherited from JpaRepository)</li>
 *   <li>Query methods using Spring Data naming conventions</li>
 *   <li>Custom @Query annotations for complex queries</li>
 *   <li>JOIN FETCH for optimized data loading</li>
 *   <li>Pagination support</li>
 * </ul>
 *
 * @author Agent A2 - Database Architect
 * @version 1.0
 * @since 2025-11-08
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, String> {

    // ========================================================================
    // QUERY BY SINGLE FIELD
    // ========================================================================

    /**
     * Finds a player by character name (case-sensitive).
     *
     * @param characterName character name to search
     * @return optional player
     */
    Optional<Player> findByCharacterName(String characterName);

    /**
     * Finds a player by character name (case-insensitive).
     *
     * @param characterName character name to search
     * @return optional player
     */
    Optional<Player> findByCharacterNameIgnoreCase(String characterName);

    /**
     * Finds all players of a specific breed/class.
     *
     * @param breed character class (Iop, Sadida, etc.)
     * @return list of players
     */
    List<Player> findByBreed(String breed);

    /**
     * Finds all players on a specific map.
     *
     * @param mapId map identifier
     * @return list of players
     */
    List<Player> findByMapId(Integer mapId);

    // ========================================================================
    // QUERY BY LEVEL
    // ========================================================================

    /**
     * Finds all players with level greater than specified.
     *
     * @param level minimum level (exclusive)
     * @return list of players
     */
    List<Player> findByLevelGreaterThan(Integer level);

    /**
     * Finds all players with level greater than or equal to specified.
     *
     * @param level minimum level (inclusive)
     * @return list of players
     */
    List<Player> findByLevelGreaterThanEqual(Integer level);

    /**
     * Finds all players within a level range.
     *
     * @param minLevel minimum level (inclusive)
     * @param maxLevel maximum level (inclusive)
     * @return list of players
     */
    List<Player> findByLevelBetween(Integer minLevel, Integer maxLevel);

    /**
     * Finds all players of a specific level.
     *
     * @param level exact level to match
     * @return list of players
     */
    List<Player> findByLevel(Integer level);

    // ========================================================================
    // QUERY BY ACTIVITY
    // ========================================================================

    /**
     * Finds all players last seen after a specific timestamp.
     * Useful for finding recently active players.
     *
     * @param since timestamp threshold
     * @return list of recently active players, ordered by lastSeen descending
     */
    @Query("SELECT p FROM Player p WHERE p.lastSeen > :since ORDER BY p.lastSeen DESC")
    List<Player> findRecentlyActive(@Param("since") LocalDateTime since);

    /**
     * Finds all players last seen within a time range.
     *
     * @param start start of time range
     * @param end end of time range
     * @return list of players
     */
    List<Player> findByLastSeenBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Finds players who haven't been seen since a specific time (inactive players).
     *
     * @param threshold inactivity threshold
     * @return list of inactive players
     */
    @Query("SELECT p FROM Player p WHERE p.lastSeen < :threshold ORDER BY p.lastSeen ASC")
    List<Player> findInactiveSince(@Param("threshold") LocalDateTime threshold);

    // ========================================================================
    // QUERY WITH JOINS (FETCH JOINS)
    // ========================================================================

    /**
     * Finds a player by ID with stats eagerly loaded.
     * Avoids N+1 query problem when accessing stats.
     *
     * @param id player ID
     * @return optional player with stats
     */
    @Query("SELECT p FROM Player p LEFT JOIN FETCH p.stats WHERE p.id = :id")
    Optional<Player> findByIdWithStats(@Param("id") String id);

    /**
     * Finds a player by ID with inventory items eagerly loaded.
     *
     * @param id player ID
     * @return optional player with inventory
     */
    @Query("SELECT p FROM Player p LEFT JOIN FETCH p.inventoryItems WHERE p.id = :id")
    Optional<Player> findByIdWithInventory(@Param("id") String id);

    /**
     * Finds a player by ID with all relationships eagerly loaded.
     * Use with caution - may fetch large amounts of data.
     *
     * @param id player ID
     * @return optional player with all relationships
     */
    @Query("SELECT DISTINCT p FROM Player p " +
           "LEFT JOIN FETCH p.stats " +
           "LEFT JOIN FETCH p.inventoryItems " +
           "WHERE p.id = :id")
    Optional<Player> findByIdWithAllRelationships(@Param("id") String id);

    /**
     * Finds a player by character name with stats.
     *
     * @param characterName character name
     * @return optional player with stats
     */
    @Query("SELECT p FROM Player p LEFT JOIN FETCH p.stats WHERE p.characterName = :name")
    Optional<Player> findByCharacterNameWithStats(@Param("name") String characterName);

    // ========================================================================
    // AGGREGATION QUERIES
    // ========================================================================

    /**
     * Counts players by breed.
     *
     * @return list of Object[] with [breed, count]
     */
    @Query("SELECT p.breed, COUNT(p) FROM Player p GROUP BY p.breed ORDER BY COUNT(p) DESC")
    List<Object[]> countByBreed();

    /**
     * Gets level distribution (count of players per level).
     *
     * @return list of Object[] with [level, count]
     */
    @Query("SELECT p.level, COUNT(p) FROM Player p GROUP BY p.level ORDER BY p.level ASC")
    List<Object[]> getLevelDistribution();

    /**
     * Counts players on each map.
     *
     * @return list of Object[] with [mapId, count]
     */
    @Query("SELECT p.mapId, COUNT(p) FROM Player p WHERE p.mapId IS NOT NULL GROUP BY p.mapId")
    List<Object[]> countPlayersByMap();

    /**
     * Gets average player level.
     *
     * @return average level as Double
     */
    @Query("SELECT AVG(p.level) FROM Player p")
    Double getAverageLevel();

    /**
     * Gets total number of players.
     * Note: count() is inherited from JpaRepository, this is just an example.
     *
     * @return total player count
     */
    @Query("SELECT COUNT(p) FROM Player p")
    Long getTotalPlayerCount();

    // ========================================================================
    // SEARCH QUERIES
    // ========================================================================

    /**
     * Searches players by partial character name match (case-insensitive).
     *
     * @param namePart partial name to search
     * @return list of matching players
     */
    @Query("SELECT p FROM Player p WHERE LOWER(p.characterName) LIKE LOWER(CONCAT('%', :namePart, '%'))")
    List<Player> searchByCharacterName(@Param("namePart") String namePart);

    /**
     * Finds players by multiple criteria (breed and level range).
     *
     * @param breed character class
     * @param minLevel minimum level
     * @param maxLevel maximum level
     * @return list of players matching criteria
     */
    @Query("SELECT p FROM Player p WHERE p.breed = :breed AND p.level BETWEEN :minLevel AND :maxLevel")
    List<Player> findByBreedAndLevelRange(
        @Param("breed") String breed,
        @Param("minLevel") Integer minLevel,
        @Param("maxLevel") Integer maxLevel
    );

    // ========================================================================
    // UPDATE QUERIES
    // ========================================================================

    /**
     * Updates player position (map and cell).
     *
     * @param playerId player ID
     * @param mapId new map ID
     * @param cellId new cell ID
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE Player p SET p.mapId = :mapId, p.cellId = :cellId, p.lastSeen = CURRENT_TIMESTAMP " +
           "WHERE p.id = :playerId")
    int updatePosition(
        @Param("playerId") String playerId,
        @Param("mapId") Integer mapId,
        @Param("cellId") Integer cellId
    );

    /**
     * Updates player level and experience.
     *
     * @param playerId player ID
     * @param level new level
     * @param experience new experience total
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE Player p SET p.level = :level, p.experience = :experience, p.lastSeen = CURRENT_TIMESTAMP " +
           "WHERE p.id = :playerId")
    int updateLevelAndExperience(
        @Param("playerId") String playerId,
        @Param("level") Integer level,
        @Param("experience") Long experience
    );

    /**
     * Updates last seen timestamp for a player.
     *
     * @param playerId player ID
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE Player p SET p.lastSeen = CURRENT_TIMESTAMP WHERE p.id = :playerId")
    int updateLastSeen(@Param("playerId") String playerId);

    // ========================================================================
    // DELETE QUERIES
    // ========================================================================

    /**
     * Deletes inactive players (not seen since threshold).
     *
     * @param threshold inactivity threshold
     * @return number of players deleted
     */
    @Modifying
    @Query("DELETE FROM Player p WHERE p.lastSeen < :threshold")
    int deleteInactivePlayers(@Param("threshold") LocalDateTime threshold);

    // ========================================================================
    // EXISTENCE CHECKS
    // ========================================================================

    /**
     * Checks if a player with given character name exists.
     *
     * @param characterName character name to check
     * @return true if exists
     */
    boolean existsByCharacterName(String characterName);

    /**
     * Checks if a player with given character name exists (case-insensitive).
     *
     * @param characterName character name to check
     * @return true if exists
     */
    boolean existsByCharacterNameIgnoreCase(String characterName);

    /**
     * Checks if any players exist on a specific map.
     *
     * @param mapId map ID to check
     * @return true if at least one player is on the map
     */
    boolean existsByMapId(Integer mapId);

    // ========================================================================
    // SORTING AND ORDERING
    // ========================================================================

    /**
     * Finds all players of a breed, ordered by level descending.
     *
     * @param breed character class
     * @return list of players ordered by level
     */
    List<Player> findByBreedOrderByLevelDesc(String breed);

    /**
     * Finds all players ordered by experience descending (leaderboard).
     *
     * @return list of players in leaderboard order
     */
    List<Player> findAllByOrderByExperienceDesc();

    /**
     * Finds top N players by experience.
     *
     * @param limit number of players to return
     * @return top players by experience
     */
    @Query("SELECT p FROM Player p ORDER BY p.experience DESC")
    List<Player> findTopPlayersByExperience(@Param("limit") int limit);
}
