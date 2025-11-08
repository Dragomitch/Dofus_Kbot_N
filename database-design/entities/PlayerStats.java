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
 * JPA Entity representing player statistics.
 *
 * <p>This entity stores all player stats including health, action/movement points,
 * core attributes, resistances, and currency. It has a one-to-one relationship
 * with the Player entity.
 *
 * <p>Key Features:
 * <ul>
 *   <li>Health points (HP) - current and maximum</li>
 *   <li>Action points (AP) - used for spells and attacks</li>
 *   <li>Movement points (MP) - used for moving</li>
 *   <li>Core stats: Strength, Intelligence, Agility, Vitality, Wisdom, Chance</li>
 *   <li>Elemental resistances</li>
 *   <li>Currency (Kamas)</li>
 *   <li>One-to-One relationship with Player</li>
 * </ul>
 *
 * <p>Denormalized Design:
 * Stats are kept in a separate table from Player to:
 * <ul>
 *   <li>Reduce UPDATE contention on the players table</li>
 *   <li>Allow for future stat history tracking</li>
 *   <li>Improve query performance for stat-heavy operations</li>
 * </ul>
 *
 * @author Agent A2 - Database Architect
 * @version 1.0
 * @since 2025-11-08
 */
@Entity
@Table(
    name = "player_stats",
    indexes = {
        @Index(name = "idx_player_stats_updated", columnList = "updated_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"player"}) // Avoid lazy loading
@EqualsAndHashCode(of = {"id"})
public class PlayerStats {

    /**
     * Auto-generated stats ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ========================================================================
    // HEALTH
    // ========================================================================

    /**
     * Current health points.
     * Cannot exceed maxHp.
     */
    @Column(name = "hp")
    @Builder.Default
    private Integer hp = 0;

    /**
     * Maximum health points.
     * Determined by Vitality and level.
     */
    @Column(name = "max_hp")
    @Builder.Default
    private Integer maxHp = 0;

    // ========================================================================
    // ACTION/MOVEMENT POINTS
    // ========================================================================

    /**
     * Current movement points.
     * Used for moving during combat and on maps.
     */
    @Column(name = "mp")
    @Builder.Default
    private Integer mp = 0;

    /**
     * Maximum movement points.
     * Base value + equipment bonuses.
     */
    @Column(name = "max_mp")
    @Builder.Default
    private Integer maxMp = 0;

    /**
     * Current action points.
     * Used for casting spells and attacking.
     */
    @Column(name = "ap")
    @Builder.Default
    private Integer ap = 0;

    /**
     * Maximum action points.
     * Base value + equipment bonuses.
     */
    @Column(name = "max_ap")
    @Builder.Default
    private Integer maxAp = 0;

    // ========================================================================
    // CORE STATS
    // ========================================================================

    /**
     * Strength characteristic.
     * Affects earth damage and carrying capacity.
     */
    @Column(name = "strength")
    @Builder.Default
    private Integer strength = 0;

    /**
     * Intelligence characteristic.
     * Affects fire damage and spell power.
     */
    @Column(name = "intelligence")
    @Builder.Default
    private Integer intelligence = 0;

    /**
     * Agility characteristic.
     * Affects air damage and dodge.
     */
    @Column(name = "agility")
    @Builder.Default
    private Integer agility = 0;

    /**
     * Vitality characteristic.
     * Affects maximum health points.
     */
    @Column(name = "vitality")
    @Builder.Default
    private Integer vitality = 0;

    /**
     * Wisdom characteristic.
     * Affects resistance and experience gain.
     */
    @Column(name = "wisdom")
    @Builder.Default
    private Integer wisdom = 0;

    /**
     * Chance characteristic.
     * Affects water damage and critical hit rate.
     */
    @Column(name = "chance")
    @Builder.Default
    private Integer chance = 0;

    // ========================================================================
    // RESISTANCES
    // ========================================================================

    /**
     * Neutral element resistance.
     */
    @Column(name = "neutral_resistance")
    @Builder.Default
    private Integer neutralResistance = 0;

    /**
     * Earth element resistance.
     */
    @Column(name = "earth_resistance")
    @Builder.Default
    private Integer earthResistance = 0;

    /**
     * Fire element resistance.
     */
    @Column(name = "fire_resistance")
    @Builder.Default
    private Integer fireResistance = 0;

    /**
     * Water element resistance.
     */
    @Column(name = "water_resistance")
    @Builder.Default
    private Integer waterResistance = 0;

    /**
     * Air element resistance.
     */
    @Column(name = "air_resistance")
    @Builder.Default
    private Integer airResistance = 0;

    // ========================================================================
    // CURRENCY
    // ========================================================================

    /**
     * Kamas (Dofus currency).
     * Used for trading, purchasing items, etc.
     */
    @Column(name = "kamas")
    @Builder.Default
    private Long kamas = 0L;

    // ========================================================================
    // TIMESTAMPS
    // ========================================================================

    /**
     * Record creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last record update timestamp.
     * Indexed for "recently updated stats" queries.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========================================================================
    // RELATIONSHIPS
    // ========================================================================

    /**
     * Player owning these stats.
     * One-to-One relationship.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", referencedColumnName = "id", unique = true, nullable = false)
    private Player player;

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
     * Checks if player is at full health.
     *
     * @return true if hp equals maxHp
     */
    public boolean isFullHealth() {
        return hp != null && maxHp != null && hp.equals(maxHp);
    }

    /**
     * Gets health as a percentage.
     *
     * @return health percentage (0-100), or 0 if maxHp is 0
     */
    public double getHealthPercentage() {
        if (maxHp == null || maxHp == 0) {
            return 0.0;
        }
        return (hp != null ? hp : 0) * 100.0 / maxHp;
    }

    /**
     * Heals the player by a specified amount.
     * Cannot exceed maxHp.
     *
     * @param amount health to restore
     */
    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Heal amount must be non-negative");
        }
        this.hp = Math.min((this.hp != null ? this.hp : 0) + amount, this.maxHp != null ? this.maxHp : 0);
    }

    /**
     * Damages the player by a specified amount.
     * Cannot go below 0.
     *
     * @param amount damage to take
     */
    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage amount must be non-negative");
        }
        this.hp = Math.max((this.hp != null ? this.hp : 0) - amount, 0);
    }

    /**
     * Checks if player is alive (hp > 0).
     *
     * @return true if hp is greater than 0
     */
    public boolean isAlive() {
        return hp != null && hp > 0;
    }

    /**
     * Adds or removes kamas.
     *
     * @param amount kamas to add (positive) or remove (negative)
     */
    public void adjustKamas(long amount) {
        this.kamas = Math.max((this.kamas != null ? this.kamas : 0L) + amount, 0L);
    }

    /**
     * Gets total stat points (sum of all characteristics).
     *
     * @return sum of str, int, agi, vit, wis, cha
     */
    public int getTotalStatPoints() {
        int str = strength != null ? strength : 0;
        int intel = intelligence != null ? intelligence : 0;
        int agi = agility != null ? agility : 0;
        int vit = vitality != null ? vitality : 0;
        int wis = wisdom != null ? wisdom : 0;
        int cha = chance != null ? chance : 0;
        return str + intel + agi + vit + wis + cha;
    }

    /**
     * Gets average elemental resistance.
     *
     * @return average of all elemental resistances
     */
    public double getAverageResistance() {
        int neutral = neutralResistance != null ? neutralResistance : 0;
        int earth = earthResistance != null ? earthResistance : 0;
        int fire = fireResistance != null ? fireResistance : 0;
        int water = waterResistance != null ? waterResistance : 0;
        int air = airResistance != null ? airResistance : 0;
        return (neutral + earth + fire + water + air) / 5.0;
    }

    /**
     * Gets a summary of player stats.
     *
     * @return formatted stats string
     */
    public String getSummary() {
        return String.format(
            "HP: %d/%d | AP: %d/%d | MP: %d/%d | Stats: %d total | Kamas: %d",
            hp, maxHp,
            ap, maxAp,
            mp, maxMp,
            getTotalStatPoints(),
            kamas
        );
    }

    /**
     * Checks if player has enough AP for an action.
     *
     * @param required required AP amount
     * @return true if current AP >= required
     */
    public boolean hasEnoughAp(int required) {
        return ap != null && ap >= required;
    }

    /**
     * Checks if player has enough MP for movement.
     *
     * @param required required MP amount
     * @return true if current MP >= required
     */
    public boolean hasEnoughMp(int required) {
        return mp != null && mp >= required;
    }

    /**
     * Spends action points.
     *
     * @param amount AP to spend
     * @throws IllegalArgumentException if not enough AP
     */
    public void spendAp(int amount) {
        if (!hasEnoughAp(amount)) {
            throw new IllegalArgumentException(
                String.format("Not enough AP (has: %d, needs: %d)", ap, amount)
            );
        }
        this.ap -= amount;
    }

    /**
     * Spends movement points.
     *
     * @param amount MP to spend
     * @throws IllegalArgumentException if not enough MP
     */
    public void spendMp(int amount) {
        if (!hasEnoughMp(amount)) {
            throw new IllegalArgumentException(
                String.format("Not enough MP (has: %d, needs: %d)", mp, amount)
            );
        }
        this.mp -= amount;
    }

    /**
     * Restores AP and MP to maximum.
     * Typically called at start of combat turn.
     */
    public void restorePoints() {
        this.ap = this.maxAp;
        this.mp = this.maxMp;
    }
}
