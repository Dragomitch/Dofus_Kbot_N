package com.dofus.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity representing a Dofus Retro game map.
 *
 * <p>This entity stores map metadata including dimensions, walkability data,
 * and relationships to players currently on the map and combat sessions.
 *
 * <p>Key Features:
 * <ul>
 *   <li>Stores map dimensions (width x height grid)</li>
 *   <li>Cell data in JSON format (walkability matrix)</li>
 *   <li>One-to-Many relationship with Players (current occupants)</li>
 *   <li>One-to-Many relationship with CombatSessions (historical combats)</li>
 * </ul>
 *
 * <p>Map Structure:
 * Dofus maps use a cell-based grid system (typically 14x20 = 560 cells).
 * Each cell has properties: walkable, line of sight, interactive, etc.
 *
 * @author Agent A2 - Database Architect
 * @version 1.0
 * @since 2025-11-08
 */
@Entity
@Table(
    name = "maps",
    indexes = {
        @Index(name = "idx_maps_name", columnList = "map_name")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"cellData"}) // Exclude large JSON data from toString
@EqualsAndHashCode(of = {"mapId"})
public class GameMap {

    /**
     * Game-assigned map identifier.
     * Extracted from GDM (Game Data Map) packets.
     */
    @Id
    @Column(name = "map_id", nullable = false)
    private Integer mapId;

    /**
     * Map display name.
     * Examples: "Amakna Village", "Bonta City", "Bwork Camp"
     */
    @Column(name = "map_name", length = 200)
    private String mapName;

    /**
     * Map width in cells.
     * Standard Dofus maps: 14 cells wide.
     */
    @Column(name = "width", nullable = false)
    @Builder.Default
    private Integer width = 14;

    /**
     * Map height in cells.
     * Standard Dofus maps: 20 cells tall (14 x 20 = 560 total cells).
     */
    @Column(name = "height", nullable = false)
    @Builder.Default
    private Integer height = 20;

    /**
     * Cell properties as JSON text.
     * Structure: Array of cell objects with properties.
     * Example:
     * <pre>
     * [
     *   {"cellId": 0, "walkable": true, "lineOfSight": true, "interactive": false},
     *   {"cellId": 1, "walkable": false, "lineOfSight": true, "interactive": false},
     *   ...
     * ]
     * </pre>
     *
     * Stored as TEXT for flexibility. Can be parsed as JSON when needed.
     */
    @Lob
    @Column(name = "cell_data", columnDefinition = "TEXT")
    private String cellData;

    /**
     * Record creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last record update timestamp.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========================================================================
    // RELATIONSHIPS
    // ========================================================================

    /**
     * Players currently on this map.
     * Transient relationship (not persisted as FK in players table).
     * Loaded via query when needed.
     */
    @Transient
    private List<Player> currentPlayers = new ArrayList<>();

    /**
     * Combat sessions that occurred on this map.
     * Lazy loaded for performance.
     */
    @OneToMany(
        mappedBy = "map",
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<CombatSession> combatSessions = new ArrayList<>();

    // ========================================================================
    // LIFECYCLE CALLBACKS
    // ========================================================================

    /**
     * Called before entity is persisted.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    /**
     * Called before entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Gets the total number of cells on this map.
     *
     * @return width * height
     */
    public int getTotalCells() {
        return width * height;
    }

    /**
     * Checks if a cell ID is valid for this map.
     *
     * @param cellId cell identifier (0-based)
     * @return true if cellId is within valid range
     */
    public boolean isValidCellId(int cellId) {
        return cellId >= 0 && cellId < getTotalCells();
    }

    /**
     * Gets the row number for a given cell ID.
     * Dofus uses row-major order: cellId = row * width + column
     *
     * @param cellId cell identifier
     * @return row number (0-based)
     */
    public int getCellRow(int cellId) {
        if (!isValidCellId(cellId)) {
            throw new IllegalArgumentException("Invalid cell ID: " + cellId);
        }
        return cellId / width;
    }

    /**
     * Gets the column number for a given cell ID.
     * Dofus uses row-major order: cellId = row * width + column
     *
     * @param cellId cell identifier
     * @return column number (0-based)
     */
    public int getCellColumn(int cellId) {
        if (!isValidCellId(cellId)) {
            throw new IllegalArgumentException("Invalid cell ID: " + cellId);
        }
        return cellId % width;
    }

    /**
     * Calculates cell ID from row and column coordinates.
     *
     * @param row row number (0-based)
     * @param column column number (0-based)
     * @return cell ID
     * @throws IllegalArgumentException if coordinates are out of bounds
     */
    public int getCellId(int row, int column) {
        if (row < 0 || row >= height || column < 0 || column >= width) {
            throw new IllegalArgumentException(
                String.format("Invalid coordinates: (%d, %d) for map %dx%d", row, column, width, height)
            );
        }
        return row * width + column;
    }

    /**
     * Checks if cell data has been loaded.
     *
     * @return true if cellData is not null and not empty
     */
    public boolean hasCellData() {
        return cellData != null && !cellData.trim().isEmpty();
    }

    /**
     * Gets a display string for this map.
     *
     * @return formatted map information
     */
    public String getDisplayInfo() {
        return String.format(
            "Map[id=%d, name='%s', dimensions=%dx%d (%d cells)]",
            mapId,
            mapName != null ? mapName : "Unknown",
            width,
            height,
            getTotalCells()
        );
    }

    /**
     * Adds a combat session to this map's history.
     *
     * @param session combat session to add
     */
    public void addCombatSession(CombatSession session) {
        combatSessions.add(session);
        session.setMap(this);
    }

    /**
     * Gets the number of combat sessions on this map.
     *
     * @return combat session count
     */
    public int getCombatSessionCount() {
        return combatSessions.size();
    }
}
