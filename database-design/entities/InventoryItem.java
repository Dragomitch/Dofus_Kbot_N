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
 * JPA Entity representing an item in a player's inventory.
 *
 * <p>This entity tracks inventory state including item details, quantity,
 * position, and equipped status.
 *
 * <p>Key Features:
 * <ul>
 *   <li>Tracks item ID, name, and type</li>
 *   <li>Stores quantity for stackable items</li>
 *   <li>Records inventory slot position</li>
 *   <li>Flags equipped items</li>
 *   <li>Many-to-One relationship with Player</li>
 *   <li>Unique constraint prevents duplicate items in same slot</li>
 * </ul>
 *
 * @author Agent A2 - Database Architect
 * @version 1.0
 * @since 2025-11-08
 */
@Entity
@Table(
    name = "inventory_items",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_inventory_player_item_position",
            columnNames = {"player_id", "item_id", "position"}
        )
    },
    indexes = {
        @Index(name = "idx_inventory_player", columnList = "player_id"),
        @Index(name = "idx_inventory_item", columnList = "item_id"),
        @Index(name = "idx_inventory_position", columnList = "player_id, position")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"player"}) // Avoid lazy loading
@EqualsAndHashCode(of = {"id"})
public class InventoryItem {

    /**
     * Auto-generated inventory item ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Game item identifier.
     * References the game's item database.
     */
    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    /**
     * Item display name.
     * Examples: "Adventurer's Sword", "Health Potion", "Wheat"
     */
    @Column(name = "item_name", length = 200)
    private String itemName;

    /**
     * Item quantity.
     * For stackable items (resources, consumables).
     * Non-stackable items always have quantity = 1.
     */
    @Column(name = "quantity")
    @Builder.Default
    private Integer quantity = 1;

    /**
     * Inventory slot position (0-based index).
     * NULL for bank items or unpositioned items.
     */
    @Column(name = "position")
    private Integer position;

    /**
     * Whether this item is currently equipped.
     * TRUE for items in equipment slots (weapon, armor, etc.).
     * FALSE for items in regular inventory.
     */
    @Column(name = "equipped")
    @Builder.Default
    private Boolean equipped = false;

    /**
     * Item type category.
     * Examples: weapon, armor, consumable, resource, quest, pet, mount
     */
    @Column(name = "item_type", length = 50)
    private String itemType;

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
     * Owner of this inventory item.
     * Many-to-One relationship with Player.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", referencedColumnName = "id", nullable = false)
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
     * Checks if this is an equipped item.
     *
     * @return true if equipped flag is TRUE
     */
    public boolean isEquipped() {
        return Boolean.TRUE.equals(equipped);
    }

    /**
     * Checks if this is a stackable item (quantity > 1).
     *
     * @return true if quantity is greater than 1
     */
    public boolean isStacked() {
        return quantity != null && quantity > 1;
    }

    /**
     * Increments item quantity by a specified amount.
     *
     * @param amount amount to add
     */
    public void addQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        this.quantity = (this.quantity != null ? this.quantity : 0) + amount;
    }

    /**
     * Decrements item quantity by a specified amount.
     *
     * @param amount amount to remove
     * @throws IllegalArgumentException if amount is negative or exceeds current quantity
     */
    public void removeQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        if (this.quantity == null || amount > this.quantity) {
            throw new IllegalArgumentException(
                String.format("Cannot remove %d items (current quantity: %d)", amount, this.quantity)
            );
        }
        this.quantity -= amount;
    }

    /**
     * Equips this item.
     * Sets equipped flag to TRUE.
     */
    public void equip() {
        this.equipped = true;
    }

    /**
     * Unequips this item.
     * Sets equipped flag to FALSE.
     */
    public void unequip() {
        this.equipped = false;
    }

    /**
     * Moves item to a new inventory position.
     *
     * @param newPosition new slot position
     */
    public void moveTo(Integer newPosition) {
        this.position = newPosition;
    }

    /**
     * Checks if item has a valid position.
     *
     * @return true if position is not null
     */
    public boolean hasPosition() {
        return position != null;
    }

    /**
     * Gets item type or "unknown" if not set.
     *
     * @return item type string
     */
    public String getItemTypeOrDefault() {
        return itemType != null ? itemType : "unknown";
    }

    /**
     * Gets a display description of this inventory item.
     *
     * @return formatted item information
     */
    public String getDescription() {
        String equippedText = isEquipped() ? "[EQUIPPED] " : "";
        String quantityText = isStacked() ? String.format(" x%d", quantity) : "";
        String positionText = hasPosition() ? String.format(" (slot %d)", position) : "";

        return String.format(
            "%s%s%s%s",
            equippedText,
            itemName != null ? itemName : "Item#" + itemId,
            quantityText,
            positionText
        );
    }

    /**
     * Checks if this item matches a given item ID.
     *
     * @param targetItemId item ID to compare
     * @return true if itemId matches
     */
    public boolean isItemId(Integer targetItemId) {
        return itemId != null && itemId.equals(targetItemId);
    }

    /**
     * Checks if this item is in a specific slot.
     *
     * @param slotPosition position to check
     * @return true if position matches
     */
    public boolean isInSlot(Integer slotPosition) {
        return position != null && position.equals(slotPosition);
    }
}
