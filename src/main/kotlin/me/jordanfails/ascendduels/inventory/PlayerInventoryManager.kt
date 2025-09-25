package me.jordanfails.ascendduels.inventory

import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.api.service.Service
import me.jordanfails.ascendduels.match.impl.match.player.RiskMatch
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Centralized inventory management system for duels.
 * Handles saving, restoring, and managing player inventories throughout the duel lifecycle.
 */
class PlayerInventoryManager : Service {
    
    private val savedInventories: MutableMap<UUID, SavedInventory> = ConcurrentHashMap()
    private val debug = true // TODO: Make configurable
    
    /**
     * Represents a saved player inventory with metadata
     */
    private data class SavedInventory(
        val contents: Array<ItemStack?>,
        val armorContents: Array<ItemStack?>,
        val level: Int,
        val exp: Float,
        val totalExperience: Int,
        val foodLevel: Int,
        val saturation: Float,
        val gameMode: GameMode,
        val savedAt: Long = System.currentTimeMillis()
    ) {
        fun getItemCount(): Int = contents.filterNotNull().size + armorContents.filterNotNull().size
        
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as SavedInventory
            return contents.contentEquals(other.contents) && 
                   armorContents.contentEquals(other.armorContents)
        }
        
        override fun hashCode(): Int {
            var result = contents.contentHashCode()
            result = 31 * result + armorContents.contentHashCode()
            return result
        }
    }
    
    /**
     * Saves a player's complete state before entering a duel.
     * This should be called when a match starts, not when teleporting.
     */
    fun savePlayerState(player: Player) {
        val savedInventory = SavedInventory(
            contents = player.inventory.contents.clone(),
            armorContents = player.inventory.armorContents.clone(),
            level = player.level,
            exp = player.exp,
            totalExperience = player.totalExperience,
            foodLevel = player.foodLevel,
            saturation = player.saturation,
            gameMode = player.gameMode
        )
        
        savedInventories[player.uniqueId] = savedInventory
        
        if (debug) {
            player.sendMessage("§7[Inventory] Saved ${savedInventory.getItemCount()} items")
        }
    }
    
    /**
     * Restores a player's complete state after a duel ends.
     * This should be called when a match ends, not when teleporting.
     */
    fun restorePlayerState(player: Player): Boolean {
        val savedInventory = savedInventories.remove(player.uniqueId)
        if (savedInventory == null) {
            if (debug) {
                player.sendMessage("§c[Inventory] No saved state found for ${player.name}")
            }
            return false
        }
        
        // Clear current state
        player.inventory.clear()
        player.inventory.armorContents = null
        
        // Restore saved state
        player.inventory.contents = savedInventory.contents
        player.inventory.armorContents = savedInventory.armorContents
        player.level = savedInventory.level
        player.exp = savedInventory.exp
        player.totalExperience = savedInventory.totalExperience
        player.foodLevel = savedInventory.foodLevel
        player.saturation = savedInventory.saturation
        player.gameMode = savedInventory.gameMode
        
        player.updateInventory()
        
        if (debug) {
            player.sendMessage("§a[Inventory] Restored ${savedInventory.getItemCount()} items")
        }
        
        return true
    }
    
    /**
     * Prepares a player for a duel by clearing their inventory and applying kit items.
     * For risk matches, keeps their current inventory.
     */
    fun prepareDuelInventory(player: Player, isRiskMatch: Boolean) {
        if (!isRiskMatch) {
            // Clear inventory for regular matches
            player.inventory.clear()
            player.inventory.armorContents = null
            player.updateInventory()
            
            if (debug) {
                player.sendMessage("§7[Inventory] Cleared for regular duel")
            }
        } else {
            if (debug) {
                player.sendMessage("§7[Inventory] Keeping items for risk duel")
            }
        }
    }
    
    /**
     * Handles inventory restoration after match ends based on match type.
     */
    fun handleMatchEnd(player: Player, isRiskMatch: Boolean, isWinner: Boolean) {
        if (isRiskMatch) {
            handleRiskMatchEnd(player, isWinner)
        } else {
            handleRegularMatchEnd(player)
        }
    }
    
    private fun handleRegularMatchEnd(player: Player) {
        // For regular matches, always restore original inventory
        val restored = restorePlayerState(player)
        if (!restored) {
            // Fallback: clear inventory if restoration fails
            player.inventory.clear()
            player.inventory.armorContents = null
            player.updateInventory()
            player.sendMessage("§c[Inventory] Warning: Could not restore your inventory!")
        }
    }
    
    private fun handleRiskMatchEnd(player: Player, isWinner: Boolean) {
        // Clear any saved inventory for risk matches (they risked it)
        clearSavedState(player)
        
        if (isWinner) {
            // Winner keeps their current inventory + gets loser's items
            // TODO: Implement winner getting loser's items logic here
            if (debug) {
                player.sendMessage("§a[Inventory] You won! Keeping your items")
            }
        } else {
            // Loser loses everything
            player.inventory.clear()
            player.inventory.armorContents = null
            player.updateInventory()
            
            if (debug) {
                player.sendMessage("§c[Inventory] You lost! Items were risked")
            }
        }
    }
    
    /**
     * Clears any saved state for a player (cleanup)
     */
    fun clearSavedState(player: Player) {
        val removed = savedInventories.remove(player.uniqueId)
        if (debug && removed != null) {
            player.sendMessage("§7[Inventory] Cleared saved state")
        }
    }
    
    /**
     * Checks if a player has saved state
     */
    fun hasSavedState(player: Player): Boolean {
        return savedInventories.containsKey(player.uniqueId)
    }
    
    /**
     * Emergency cleanup - restores all saved inventories (for plugin disable)
     */
    fun emergencyRestoreAll() {
        savedInventories.entries.forEach { (uuid, savedInventory) ->
            val player = AscendDuels.instance.server.getPlayer(uuid)
            if (player != null && player.isOnline) {
                restorePlayerState(player)
            }
        }
        savedInventories.clear()
    }
    
    /**
     * Gets statistics about saved inventories
     */
    fun getStats(): String {
        val count = savedInventories.size
        val oldestTime = savedInventories.values.minByOrNull { it.savedAt }?.savedAt ?: 0L
        val age = if (oldestTime > 0) (System.currentTimeMillis() - oldestTime) / 1000 else 0
        return "Saved inventories: $count (oldest: ${age}s ago)"
    }
    
    override fun load() {
        if (debug) {
            AscendDuels.instance.logger.info("PlayerInventoryManager loaded")
        }
    }
    
    override fun unload() {
        emergencyRestoreAll()
        if (debug) {
            AscendDuels.instance.logger.info("PlayerInventoryManager unloaded")
        }
    }
}
