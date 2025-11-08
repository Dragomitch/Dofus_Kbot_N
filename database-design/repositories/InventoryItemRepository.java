package com.dofus.persistence.repository;

import com.dofus.persistence.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for InventoryItem entity.
 *
 * <p>Provides CRUD operations and custom queries for inventory management.
 *
 * @author Agent A2 - Database Architect
 * @version 1.0
 * @since 2025-11-08
 */
@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    // ========================================================================
    // QUERY BY PLAYER
    // ========================================================================

    /**
     * Finds all inventory items for a player.
     *
     * @param playerId player identifier
     * @return list of inventory items ordered by position
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.player.id = :playerId ORDER BY i.position ASC")
    List<InventoryItem> findByPlayer(@Param("playerId") String playerId);

    /**
     * Finds all inventory items for a player with specific item ID.
     *
     * @param playerId player identifier
     * @param itemId game item ID
     * @return list of matching items
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.player.id = :playerId AND i.itemId = :itemId")
    List<InventoryItem> findByPlayerAndItemId(
        @Param("playerId") String playerId,
        @Param("itemId") Integer itemId
    );

    /**
     * Finds an item in a specific inventory slot.
     *
     * @param playerId player identifier
     * @param position inventory slot position
     * @return optional inventory item
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.player.id = :playerId AND i.position = :position")
    Optional<InventoryItem> findByPlayerAndPosition(
        @Param("playerId") String playerId,
        @Param("position") Integer position
    );

    /**
     * Counts inventory items for a player.
     *
     * @param playerId player identifier
     * @return item count
     */
    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.player.id = :playerId")
    Long countByPlayer(@Param("playerId") String playerId);

    // ========================================================================
    // QUERY BY EQUIPPED STATUS
    // ========================================================================

    /**
     * Finds all equipped items for a player.
     *
     * @param playerId player identifier
     * @return list of equipped items
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.player.id = :playerId AND i.equipped = TRUE " +
           "ORDER BY i.position ASC")
    List<InventoryItem> findEquippedItems(@Param("playerId") String playerId);

    /**
     * Finds all non-equipped items for a player.
     *
     * @param playerId player identifier
     * @return list of non-equipped items
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.player.id = :playerId AND i.equipped = FALSE " +
           "ORDER BY i.position ASC")
    List<InventoryItem> findNonEquippedItems(@Param("playerId") String playerId);

    /**
     * Counts equipped items for a player.
     *
     * @param playerId player identifier
     * @return equipped item count
     */
    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.player.id = :playerId AND i.equipped = TRUE")
    Long countEquippedItems(@Param("playerId") String playerId);

    // ========================================================================
    // QUERY BY ITEM TYPE
    // ========================================================================

    /**
     * Finds items by type for a player.
     *
     * @param playerId player identifier
     * @param itemType item type (weapon, armor, consumable, etc.)
     * @return list of items
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.player.id = :playerId AND i.itemType = :type " +
           "ORDER BY i.position ASC")
    List<InventoryItem> findByPlayerAndItemType(
        @Param("playerId") String playerId,
        @Param("type") String itemType
    );

    /**
     * Finds all weapons for a player.
     *
     * @param playerId player identifier
     * @return list of weapons
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.player.id = :playerId AND i.itemType = 'weapon'")
    List<InventoryItem> findWeapons(@Param("playerId") String playerId);

    /**
     * Finds all consumables for a player.
     *
     * @param playerId player identifier
     * @return list of consumables
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.player.id = :playerId AND i.itemType = 'consumable'")
    List<InventoryItem> findConsumables(@Param("playerId") String playerId);

    // ========================================================================
    // QUERY BY QUANTITY
    // ========================================================================

    /**
     * Finds stacked items (quantity > 1) for a player.
     *
     * @param playerId player identifier
     * @return list of stacked items
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.player.id = :playerId AND i.quantity > 1 " +
           "ORDER BY i.quantity DESC")
    List<InventoryItem> findStackedItems(@Param("playerId") String playerId);

    /**
     * Gets total quantity of a specific item across all stacks.
     *
     * @param playerId player identifier
     * @param itemId game item ID
     * @return total quantity
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM InventoryItem i " +
           "WHERE i.player.id = :playerId AND i.itemId = :itemId")
    Integer getTotalItemQuantity(
        @Param("playerId") String playerId,
        @Param("itemId") Integer itemId
    );

    // ========================================================================
    // SEARCH QUERIES
    // ========================================================================

    /**
     * Searches inventory items by name (case-insensitive partial match).
     *
     * @param playerId player identifier
     * @param namePart partial item name
     * @return list of matching items
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.player.id = :playerId " +
           "AND LOWER(i.itemName) LIKE LOWER(CONCAT('%', :namePart, '%'))")
    List<InventoryItem> searchByItemName(
        @Param("playerId") String playerId,
        @Param("namePart") String namePart
    );

    /**
     * Finds items with name matching exactly.
     *
     * @param playerId player identifier
     * @param itemName exact item name
     * @return list of items
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.player.id = :playerId AND i.itemName = :name")
    List<InventoryItem> findByPlayerAndItemName(
        @Param("playerId") String playerId,
        @Param("name") String itemName
    );

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Gets item type distribution for a player.
     *
     * @param playerId player identifier
     * @return list of Object[] with [itemType, count]
     */
    @Query("SELECT i.itemType, COUNT(i) FROM InventoryItem i " +
           "WHERE i.player.id = :playerId " +
           "GROUP BY i.itemType ORDER BY COUNT(i) DESC")
    List<Object[]> getItemTypeDistribution(@Param("playerId") String playerId);

    /**
     * Gets most common items in inventory.
     *
     * @param playerId player identifier
     * @return list of Object[] with [itemId, itemName, totalQuantity]
     */
    @Query("SELECT i.itemId, i.itemName, SUM(i.quantity) FROM InventoryItem i " +
           "WHERE i.player.id = :playerId " +
           "GROUP BY i.itemId, i.itemName ORDER BY SUM(i.quantity) DESC")
    List<Object[]> getMostCommonItems(@Param("playerId") String playerId);

    /**
     * Gets total inventory value (sum of all item quantities).
     *
     * @param playerId player identifier
     * @return total item count
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM InventoryItem i WHERE i.player.id = :playerId")
    Long getTotalInventorySize(@Param("playerId") String playerId);

    // ========================================================================
    // GLOBAL STATISTICS
    // ========================================================================

    /**
     * Gets most popular items across all players.
     *
     * @return list of Object[] with [itemId, itemName, playerCount]
     */
    @Query("SELECT i.itemId, i.itemName, COUNT(DISTINCT i.player.id) FROM InventoryItem i " +
           "GROUP BY i.itemId, i.itemName ORDER BY COUNT(DISTINCT i.player.id) DESC")
    List<Object[]> getMostPopularItems();

    /**
     * Gets items owned by the most players.
     *
     * @param limit maximum number of results
     * @return list of popular items
     */
    @Query("SELECT i.itemId, i.itemName, COUNT(DISTINCT i.player.id) FROM InventoryItem i " +
           "GROUP BY i.itemId, i.itemName ORDER BY COUNT(DISTINCT i.player.id) DESC")
    List<Object[]> getTopItemsByOwnership(@Param("limit") int limit);

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Updates item quantity.
     *
     * @param id inventory item ID
     * @param newQuantity new quantity
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE InventoryItem i SET i.quantity = :quantity WHERE i.id = :id")
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer newQuantity);

    /**
     * Updates item position.
     *
     * @param id inventory item ID
     * @param newPosition new position
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE InventoryItem i SET i.position = :position WHERE i.id = :id")
    int updatePosition(@Param("id") Long id, @Param("position") Integer newPosition);

    /**
     * Equips an item.
     *
     * @param id inventory item ID
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE InventoryItem i SET i.equipped = TRUE WHERE i.id = :id")
    int equipItem(@Param("id") Long id);

    /**
     * Unequips an item.
     *
     * @param id inventory item ID
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE InventoryItem i SET i.equipped = FALSE WHERE i.id = :id")
    int unequipItem(@Param("id") Long id);

    /**
     * Unequips all items for a player.
     *
     * @param playerId player identifier
     * @return number of items unequipped
     */
    @Modifying
    @Query("UPDATE InventoryItem i SET i.equipped = FALSE WHERE i.player.id = :playerId AND i.equipped = TRUE")
    int unequipAllItems(@Param("playerId") String playerId);

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Deletes all inventory items for a player.
     *
     * @param playerId player identifier
     * @return number of items deleted
     */
    @Modifying
    @Query("DELETE FROM InventoryItem i WHERE i.player.id = :playerId")
    int deleteAllByPlayer(@Param("playerId") String playerId);

    /**
     * Deletes an item from a specific slot.
     *
     * @param playerId player identifier
     * @param position inventory slot
     * @return number of items deleted
     */
    @Modifying
    @Query("DELETE FROM InventoryItem i WHERE i.player.id = :playerId AND i.position = :position")
    int deleteByPlayerAndPosition(@Param("playerId") String playerId, @Param("position") Integer position);

    /**
     * Deletes all items of a specific type for a player.
     *
     * @param playerId player identifier
     * @param itemId game item ID
     * @return number of items deleted
     */
    @Modifying
    @Query("DELETE FROM InventoryItem i WHERE i.player.id = :playerId AND i.itemId = :itemId")
    int deleteByPlayerAndItemId(@Param("playerId") String playerId, @Param("itemId") Integer itemId);

    // ========================================================================
    // EXISTENCE CHECKS
    // ========================================================================

    /**
     * Checks if a player has a specific item.
     *
     * @param playerId player identifier
     * @param itemId game item ID
     * @return true if player has the item
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN TRUE ELSE FALSE END FROM InventoryItem i " +
           "WHERE i.player.id = :playerId AND i.itemId = :itemId")
    Boolean hasItem(@Param("playerId") String playerId, @Param("itemId") Integer itemId);

    /**
     * Checks if a player has an equipped item.
     *
     * @param playerId player identifier
     * @return true if player has any equipped items
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN TRUE ELSE FALSE END FROM InventoryItem i " +
           "WHERE i.player.id = :playerId AND i.equipped = TRUE")
    Boolean hasEquippedItems(@Param("playerId") String playerId);

    /**
     * Checks if an inventory slot is occupied.
     *
     * @param playerId player identifier
     * @param position slot position
     * @return true if slot is occupied
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN TRUE ELSE FALSE END FROM InventoryItem i " +
           "WHERE i.player.id = :playerId AND i.position = :position")
    Boolean isSlotOccupied(@Param("playerId") String playerId, @Param("position") Integer position);
}
