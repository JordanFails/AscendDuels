package me.jordanfails.ascendduels.listener

import com.rit.sucy.enchanting.EEquip
import de.tr7zw.changeme.nbtapi.NBTItem
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.match.Match
import me.jordanfails.ascendduels.match.impl.match.player.RiskMatch
import net.pvpwars.core.Core
import net.pvpwars.core.game.extension.ClaimExtension
import net.pvpwars.core.game.extension.ExtensionSystem
import org.bukkit.World
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.metadata.FixedMetadataValue

/**
 * Teleport listener for duel arena transitions.
 *
 * (Ripped from net.pvpwars.minigames.listener.GeneralListener)
 */
class TeleportListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onTeleport(event: PlayerTeleportEvent) {
        val arenaHandler = AscendDuels.instance.arenaHandler
        val toWorld: World = event.to.world
        val fromWorld: World = event.from.world
        if (toWorld == fromWorld) return
        if(toWorld == null) return
        if(fromWorld == null) return

        val player = event.player

        if (arenaHandler.isWorld(toWorld)) {
            // Entering an arena world
            player.setMetadata("ignoreSector", FixedMetadataValue(AscendDuels.instance, true))
            player.setMetadata("friendlyFire", FixedMetadataValue(AscendDuels.instance, true))
            Core.getInstance().systemManager
                .get(ExtensionSystem::class.java)
                .getExtension(ClaimExtension::class.java)
                .markBusy(player, true)

            // Save player's current inventory before entering duel (only for non-risk matches)
            val match: Match<*, *>? = AscendDuels.instance.matchService.getByPlayer(player)
            if (match !is RiskMatch) {
                AscendDuels.instance.playerInventoryService.saveInventory(player)
            }
            pinInventory(player)
            player.removeMetadata("riskMatchConfirming", AscendDuels.instance)

        } else if (arenaHandler.isWorld(fromWorld)) {
            // Leaving an arena world - restore inventory
            restoreInventory(player)

            player.removeMetadata("ignoreSector", AscendDuels.instance)
            player.removeMetadata("friendlyFire", AscendDuels.instance)
            Core.getInstance().systemManager
                .get(ExtensionSystem::class.java)
                .getExtension(ClaimExtension::class.java)
                .markBusy(player, false)
        }
    }

    companion object {
        fun pinInventory(player: Player) {
            val match: Match<*, *>? = AscendDuels.instance.matchService.getByPlayer(player)

            if (match !is RiskMatch) {
                player.inventory.clear()
                player.inventory.armorContents = null
            }

            EEquip(player).run()
        }
        
        fun restoreInventory(player: Player) {
            // Clear any duel items from inventory first
            clearDuelItems(player)
            
            // Check if this was a risk match
            val match: Match<*, *>? = AscendDuels.instance.matchService.getByPlayer(player)
            val isRiskMatch = match is RiskMatch
            
            if (isRiskMatch) {
                // For risk matches, don't restore original inventory - players risked their items
                // Just clear the current inventory (duel items already cleared above)
                player.inventory.clear()
                player.inventory.armorContents = null
                player.updateInventory()
                
                // Clear any saved inventory for risk matches
                AscendDuels.instance.playerInventoryService.clearSavedInventory(player)
            } else {
                // For regular matches, restore the player's original inventory
                val restored = AscendDuels.instance.playerInventoryService.restoreInventory(player)
                
                if (!restored) {
                    // If no saved inventory exists, just clear the current one
                    player.inventory.clear()
                    player.inventory.armorContents = null
                    player.updateInventory()
                }
            }
        }
        
        fun clearDuelItems(player: Player) {
            // Clear main inventory of duel items
            for (i in 0 until player.inventory.size) {
                val item = player.inventory.getItem(i)
                if (item != null && isDuelItem(item)) {
                    player.inventory.setItem(i, null)
                }
            }
            
            // Clear armor slots of duel items
            for (i in 0 until 4) {
                val armor = player.inventory.armorContents[i]
                if (armor != null && isDuelItem(armor)) {
                    player.inventory.armorContents[i] = null
                }
            }
            
            player.updateInventory()
        }
        
        private fun isDuelItem(item: org.bukkit.inventory.ItemStack): Boolean {
            return try {
                val nbtItem = NBTItem(item)
                nbtItem.getBoolean("duelItem")
            } catch (e: Exception) {
                false
            }
        }
    }
}