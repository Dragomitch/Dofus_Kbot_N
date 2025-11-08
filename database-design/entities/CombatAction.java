package com.dofus.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a single action within a combat session.
 *
 * <p>This entity captures individual actions taken during combat, such as
 * spell casts, movements, item uses, and turn passes.
 *
 * <p>Key Features:
 * <ul>
 *   <li>Tracks action type (spell, movement, etc.)</li>
 *   <li>Records turn number and timestamp</li>
 *   <li>Stores spell details and costs</li>
 *   <li>Captures damage and critical hits</li>
 *   <li>Many-to-One relationship with CombatSession</li>
 * </ul>
 *
 * @author Agent A2 - Database Architect
 * @version 1.0
 * @since 2025-11-08
 */
@Entity
@Table(
    name = "combat_actions",
    indexes = {
        @Index(name = "idx_combat_actions_session", columnList = "combat_session_id"),
        @Index(name = "idx_combat_actions_turn", columnList = "combat_session_id, turn_number"),
        @Index(name = "idx_combat_actions_spell", columnList = "spell_id"),
        @Index(name = "idx_combat_actions_type", columnList = "action_type"),
        @Index(name = "idx_combat_actions_timestamp", columnList = "timestamp")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"combatSession"}) // Avoid lazy loading
@EqualsAndHashCode(of = {"id"})
public class CombatAction {

    /**
     * Auto-generated action ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Turn number within the combat session.
     * Starts at 1 for first turn.
     */
    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    /**
     * Action type.
     * Examples: CAST_SPELL, MOVE, PASS_TURN, USE_ITEM, FLEE_ATTEMPT
     */
    @Column(name = "action_type", length = 50, nullable = false)
    private String actionType;

    /**
     * Spell ID if action is CAST_SPELL.
     * NULL for non-spell actions.
     */
    @Column(name = "spell_id")
    private Integer spellId;

    /**
     * Spell name for display purposes.
     */
    @Column(name = "spell_name", length = 100)
    private String spellName;

    /**
     * Target cell ID (0-559 for standard maps).
     * Used for spell targeting and movement.
     */
    @Column(name = "target_cell")
    private Integer targetCell;

    /**
     * Target entity ID.
     * NULL if targeting a cell rather than an entity.
     */
    @Column(name = "target_entity_id")
    private Integer targetEntityId;

    /**
     * Damage dealt by this action.
     * 0 for non-damage actions (movement, buffs, etc.).
     */
    @Column(name = "damage_dealt")
    @Builder.Default
    private Integer damageDealt = 0;

    /**
     * Action Point cost.
     * 0 for free actions or actions that don't use AP.
     */
    @Column(name = "ap_cost")
    @Builder.Default
    private Integer apCost = 0;

    /**
     * Movement Point cost.
     * Used for movement actions.
     */
    @Column(name = "mp_cost")
    @Builder.Default
    private Integer mpCost = 0;

    /**
     * Whether this action was a critical hit.
     */
    @Column(name = "critical_hit")
    @Builder.Default
    private Boolean criticalHit = false;

    /**
     * Action timestamp.
     * When the action was executed.
     */
    @Column(name = "timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Record creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ========================================================================
    // RELATIONSHIPS
    // ========================================================================

    /**
     * Parent combat session.
     * Many-to-One relationship.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combat_session_id", referencedColumnName = "id", nullable = false)
    private CombatSession combatSession;

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
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Checks if this is a spell cast action.
     *
     * @return true if actionType is CAST_SPELL
     */
    public boolean isSpellCast() {
        return "CAST_SPELL".equals(actionType);
    }

    /**
     * Checks if this is a movement action.
     *
     * @return true if actionType is MOVE
     */
    public boolean isMovement() {
        return "MOVE".equals(actionType);
    }

    /**
     * Checks if this is a pass turn action.
     *
     * @return true if actionType is PASS_TURN
     */
    public boolean isPassTurn() {
        return "PASS_TURN".equals(actionType);
    }

    /**
     * Checks if this action dealt damage.
     *
     * @return true if damageDealt > 0
     */
    public boolean didDamage() {
        return damageDealt != null && damageDealt > 0;
    }

    /**
     * Checks if this action had a cost (AP or MP).
     *
     * @return true if apCost > 0 or mpCost > 0
     */
    public boolean hadCost() {
        return (apCost != null && apCost > 0) || (mpCost != null && mpCost > 0);
    }

    /**
     * Gets a formatted description of this action.
     *
     * @return human-readable action description
     */
    public String getDescription() {
        if (isSpellCast()) {
            String critText = Boolean.TRUE.equals(criticalHit) ? " [CRIT]" : "";
            String damageText = didDamage() ? String.format(" (%d damage)", damageDealt) : "";
            return String.format(
                "Turn %d: Cast %s (AP: %d)%s%s",
                turnNumber,
                spellName != null ? spellName : "Spell#" + spellId,
                apCost,
                damageText,
                critText
            );
        } else if (isMovement()) {
            return String.format(
                "Turn %d: Move to cell %d (MP: %d)",
                turnNumber,
                targetCell,
                mpCost
            );
        } else if (isPassTurn()) {
            return String.format("Turn %d: Pass turn", turnNumber);
        } else {
            return String.format("Turn %d: %s", turnNumber, actionType);
        }
    }

    /**
     * Gets the total resource cost (AP + MP).
     *
     * @return sum of AP and MP costs
     */
    public int getTotalCost() {
        int ap = apCost != null ? apCost : 0;
        int mp = mpCost != null ? mpCost : 0;
        return ap + mp;
    }
}
