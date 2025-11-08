package com.dofus.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity representing a combat engagement session.
 *
 * <p>This entity tracks a complete combat encounter from start to finish,
 * including result, duration, and rewards.
 *
 * <p>Key Features:
 * <ul>
 *   <li>Tracks combat lifecycle (start/end times, duration)</li>
 *   <li>Records combat outcome (WIN, LOSS, FLEE, etc.)</li>
 *   <li>Stores rewards (experience, kamas, items)</li>
 *   <li>One-to-Many relationship with CombatActions</li>
 *   <li>Many-to-One relationships with Player and GameMap</li>
 * </ul>
 *
 * @author Agent A2 - Database Architect
 * @version 1.0
 * @since 2025-11-08
 */
@Entity
@Table(
    name = "combat_sessions",
    indexes = {
        @Index(name = "idx_combat_player", columnList = "player_id"),
        @Index(name = "idx_combat_map", columnList = "map_id"),
        @Index(name = "idx_combat_start_time", columnList = "start_time"),
        @Index(name = "idx_combat_end_time", columnList = "end_time"),
        @Index(name = "idx_combat_result", columnList = "result"),
        @Index(name = "idx_combat_player_result", columnList = "player_id, result")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"actions", "player", "map"}) // Avoid lazy loading
@EqualsAndHashCode(of = {"id"})
public class CombatSession {

    /**
     * Auto-generated combat session ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Combat start timestamp.
     * Captured from GS (Game Start) packet.
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * Combat end timestamp.
     * NULL if combat is ongoing.
     * Set when GE (Game End) packet is received.
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * Combat result.
     * Updated when combat ends.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 20)
    @Builder.Default
    private CombatResult result = CombatResult.ONGOING;

    /**
     * Combat duration in seconds.
     * Automatically calculated from end_time - start_time.
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    /**
     * Experience points gained from combat.
     */
    @Column(name = "experience_gained")
    @Builder.Default
    private Long experienceGained = 0L;

    /**
     * Kamas (currency) gained from combat.
     */
    @Column(name = "kamas_gained")
    @Builder.Default
    private Long kamasGained = 0L;

    /**
     * Number of items looted.
     */
    @Column(name = "items_looted")
    @Builder.Default
    private Integer itemsLooted = 0;

    /**
     * Record creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ========================================================================
    // RELATIONSHIPS
    // ========================================================================

    /**
     * Player participating in this combat.
     * Many-to-One relationship.
     * Cascade: None (keep combat history if player is deleted).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    private Player player;

    /**
     * Map where combat occurred.
     * Many-to-One relationship.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_id", referencedColumnName = "map_id")
    private GameMap map;

    /**
     * All actions taken during this combat.
     * One-to-Many relationship with CombatAction.
     * Cascade ALL: deleting session deletes all actions.
     */
    @OneToMany(
        mappedBy = "combatSession",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("turnNumber ASC, timestamp ASC")
    @Builder.Default
    private List<CombatAction> actions = new ArrayList<>();

    // ========================================================================
    // ENUMS
    // ========================================================================

    /**
     * Combat result enum.
     */
    public enum CombatResult {
        /** Combat is still in progress */
        ONGOING,
        /** Player won the combat */
        WIN,
        /** Player lost the combat */
        LOSS,
        /** Player fled from combat */
        FLEE,
        /** Combat ended in a draw */
        DRAW
    }

    // ========================================================================
    // LIFECYCLE CALLBACKS
    // ========================================================================

    /**
     * Called before entity is persisted.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
    }

    /**
     * Called before entity is updated.
     * Automatically calculates duration if endTime is set.
     */
    @PreUpdate
    protected void onUpdate() {
        calculateDuration();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Ends the combat session with a result.
     *
     * @param finalResult combat outcome
     */
    public void endCombat(CombatResult finalResult) {
        this.endTime = LocalDateTime.now();
        this.result = finalResult;
        calculateDuration();
    }

    /**
     * Ends the combat session with a result and rewards.
     *
     * @param finalResult combat outcome
     * @param xpGained experience points gained
     * @param kamasReceived kamas gained
     * @param itemsReceived number of items looted
     */
    public void endCombat(CombatResult finalResult, Long xpGained, Long kamasReceived, Integer itemsReceived) {
        this.endTime = LocalDateTime.now();
        this.result = finalResult;
        this.experienceGained = xpGained;
        this.kamasGained = kamasReceived;
        this.itemsLooted = itemsReceived;
        calculateDuration();
    }

    /**
     * Calculates combat duration in seconds.
     * Sets durationSeconds field if both start and end times are present.
     */
    private void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationSeconds = (int) Duration.between(startTime, endTime).getSeconds();
        }
    }

    /**
     * Checks if combat is still ongoing.
     *
     * @return true if endTime is null or result is ONGOING
     */
    public boolean isOngoing() {
        return endTime == null || CombatResult.ONGOING.equals(result);
    }

    /**
     * Checks if combat was won.
     *
     * @return true if result is WIN
     */
    public boolean isWin() {
        return CombatResult.WIN.equals(result);
    }

    /**
     * Checks if combat was lost.
     *
     * @return true if result is LOSS
     */
    public boolean isLoss() {
        return CombatResult.LOSS.equals(result);
    }

    /**
     * Gets combat duration as a Duration object.
     *
     * @return Duration between start and end, or null if ongoing
     */
    public Duration getDuration() {
        if (startTime != null && endTime != null) {
            return Duration.between(startTime, endTime);
        }
        return null;
    }

    /**
     * Gets combat duration in minutes.
     *
     * @return duration in minutes, or 0 if ongoing
     */
    public long getDurationMinutes() {
        return durationSeconds != null ? durationSeconds / 60 : 0;
    }

    /**
     * Adds an action to this combat session.
     * Maintains bidirectional relationship.
     *
     * @param action combat action to add
     */
    public void addAction(CombatAction action) {
        actions.add(action);
        action.setCombatSession(this);
    }

    /**
     * Gets the number of actions in this combat.
     *
     * @return action count
     */
    public int getActionCount() {
        return actions.size();
    }

    /**
     * Gets the number of turns in this combat.
     *
     * @return maximum turn number, or 0 if no actions
     */
    public int getTurnCount() {
        return actions.stream()
            .mapToInt(CombatAction::getTurnNumber)
            .max()
            .orElse(0);
    }

    /**
     * Gets all actions for a specific turn.
     *
     * @param turnNumber turn to filter by
     * @return list of actions for that turn
     */
    public List<CombatAction> getActionsForTurn(int turnNumber) {
        return actions.stream()
            .filter(action -> action.getTurnNumber() == turnNumber)
            .toList();
    }

    /**
     * Gets a summary description of this combat.
     *
     * @return formatted combat summary
     */
    public String getSummary() {
        if (isOngoing()) {
            return String.format(
                "Combat[id=%d, ONGOING, started=%s, %d actions]",
                id,
                startTime,
                getActionCount()
            );
        } else {
            return String.format(
                "Combat[id=%d, %s, duration=%ds, xp=%d, kamas=%d, items=%d]",
                id,
                result,
                durationSeconds,
                experienceGained,
                kamasGained,
                itemsLooted
            );
        }
    }
}
