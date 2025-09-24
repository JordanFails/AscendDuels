package me.jordanfails.ascendduels.inventory

import me.jordanfails.ascendduels.api.service.Service
import me.jordanfails.ascendduels.api.serializable.inventory.JsonInventory
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PlayerInventoryService : Service {
    
    private val savedInventories: MutableMap<UUID, JsonInventory> = ConcurrentHashMap()
    
    /**
     * Saves a player's current inventory before entering a duel
     */
    fun saveInventory(player: Player) {
        val contents = player.inventory.contents.clone()
        val armorContents = player.inventory.armorContents.clone()
        
        savedInventories[player.uniqueId] = JsonInventory(contents, armorContents)
    }
    
    /**
     * Restores a player's saved inventory when returning to spawn
     */
    fun restoreInventory(player: Player): Boolean {
        val savedInventory = savedInventories.remove(player.uniqueId) ?: return false
        
        // Clear current inventory first
        player.inventory.clear()
        player.inventory.armorContents = null
        
        // Restore saved inventory
        player.inventory.contents = savedInventory.contents
        player.inventory.armorContents = savedInventory.armorContents
        player.updateInventory()
        
        return true
    }
    
    /**
     * Clears any saved inventory for a player (cleanup)
     */
    fun clearSavedInventory(player: Player) {
        savedInventories.remove(player.uniqueId)
    }
    
    /**
     * Checks if a player has a saved inventory
     */
    fun hasSavedInventory(player: Player): Boolean {
        return savedInventories.containsKey(player.uniqueId)
    }
    
    override fun load() {
        // Service initialization
    }
    
    override fun unload() {
        savedInventories.clear()
    }
}
