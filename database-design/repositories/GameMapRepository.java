package com.dofus.persistence.repository;

import com.dofus.persistence.entity.GameMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for GameMap entity.
 *
 * <p>Provides CRUD operations and custom queries for map management.
 *
 * @author Agent A2 - Database Architect
 * @version 1.0
 * @since 2025-11-08
 */
@Repository
public interface GameMapRepository extends JpaRepository<GameMap, Integer> {

    // ========================================================================
    // QUERY BY NAME
    // ========================================================================

    /**
     * Finds a map by exact name.
     *
     * @param mapName map name to search
     * @return optional map
     */
    Optional<GameMap> findByMapName(String mapName);

    /**
     * Finds maps by name (case-insensitive).
     *
     * @param mapName map name to search
     * @return list of maps
     */
    List<GameMap> findByMapNameIgnoreCase(String mapName);

    /**
     * Searches maps by partial name match (case-insensitive).
     *
     * @param namePart partial name to search
     * @return list of matching maps
     */
    @Query("SELECT m FROM GameMap m WHERE LOWER(m.mapName) LIKE LOWER(CONCAT('%', :namePart, '%'))")
    List<GameMap> searchByMapName(@Param("namePart") String namePart);

    // ========================================================================
    // QUERY BY DIMENSIONS
    // ========================================================================

    /**
     * Finds all maps with specific dimensions.
     *
     * @param width map width
     * @param height map height
     * @return list of maps
     */
    List<GameMap> findByWidthAndHeight(Integer width, Integer height);

    /**
     * Finds all non-standard sized maps (not 14x20).
     *
     * @return list of non-standard maps
     */
    @Query("SELECT m FROM GameMap m WHERE m.width != 14 OR m.height != 20")
    List<GameMap> findNonStandardMaps();

    // ========================================================================
    // CELL DATA QUERIES
    // ========================================================================

    /**
     * Finds maps that have cell data loaded.
     *
     * @return list of maps with cell data
     */
    @Query("SELECT m FROM GameMap m WHERE m.cellData IS NOT NULL AND m.cellData != ''")
    List<GameMap> findMapsWithCellData();

    /**
     * Finds maps missing cell data.
     *
     * @return list of maps without cell data
     */
    @Query("SELECT m FROM GameMap m WHERE m.cellData IS NULL OR m.cellData = ''")
    List<GameMap> findMapsWithoutCellData();

    /**
     * Counts maps with cell data.
     *
     * @return count of maps with cell data
     */
    @Query("SELECT COUNT(m) FROM GameMap m WHERE m.cellData IS NOT NULL AND m.cellData != ''")
    Long countMapsWithCellData();

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Gets total number of maps.
     *
     * @return map count
     */
    @Query("SELECT COUNT(m) FROM GameMap m")
    Long getTotalMapCount();

    /**
     * Gets map dimension distribution.
     *
     * @return list of Object[] with [width, height, count]
     */
    @Query("SELECT m.width, m.height, COUNT(m) FROM GameMap m " +
           "GROUP BY m.width, m.height ORDER BY COUNT(m) DESC")
    List<Object[]> getMapDimensionDistribution();

    // ========================================================================
    // JOINS WITH OTHER ENTITIES
    // ========================================================================

    /**
     * Finds a map with its combat sessions eagerly loaded.
     *
     * @param mapId map ID
     * @return optional map with combat sessions
     */
    @Query("SELECT m FROM GameMap m LEFT JOIN FETCH m.combatSessions WHERE m.mapId = :mapId")
    Optional<GameMap> findByIdWithCombatSessions(@Param("mapId") Integer mapId);

    /**
     * Finds maps that have had combat sessions.
     *
     * @return list of maps with combat history
     */
    @Query("SELECT DISTINCT m FROM GameMap m JOIN m.combatSessions")
    List<GameMap> findMapsWithCombatHistory();

    /**
     * Gets map IDs that are currently occupied by players.
     * Note: This requires a join with Player entity (cross-repository query).
     *
     * @return list of occupied map IDs
     */
    @Query(value = "SELECT DISTINCT map_id FROM players WHERE map_id IS NOT NULL",
           nativeQuery = true)
    List<Integer> findOccupiedMapIds();

    // ========================================================================
    // EXISTENCE CHECKS
    // ========================================================================

    /**
     * Checks if a map exists by name.
     *
     * @param mapName map name to check
     * @return true if exists
     */
    boolean existsByMapName(String mapName);

    /**
     * Checks if a map has cell data.
     *
     * @param mapId map ID
     * @return true if cell data exists
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END FROM GameMap m " +
           "WHERE m.mapId = :mapId AND m.cellData IS NOT NULL AND m.cellData != ''")
    Boolean hasCellData(@Param("mapId") Integer mapId);

    // ========================================================================
    // ORDERING
    // ========================================================================

    /**
     * Finds all maps ordered by name.
     *
     * @return list of maps sorted alphabetically
     */
    List<GameMap> findAllByOrderByMapNameAsc();

    /**
     * Finds all maps ordered by creation date.
     *
     * @return list of maps sorted by creation date
     */
    List<GameMap> findAllByOrderByCreatedAtDesc();

    /**
     * Finds recently updated maps.
     *
     * @param limit maximum number of results
     * @return list of recently updated maps
     */
    @Query("SELECT m FROM GameMap m ORDER BY m.updatedAt DESC")
    List<GameMap> findRecentlyUpdatedMaps(@Param("limit") int limit);
}
