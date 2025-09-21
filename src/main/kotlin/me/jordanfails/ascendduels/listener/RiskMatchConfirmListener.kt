package me.jordanfails.ascendduels.listener

import me.jordanfails.ascendduels.AscendDuels
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*

class RiskMatchConfirmListener : Listener {

    private fun Player.isConfirmingRiskMatch(): Boolean =
        this.hasMetadata("riskMatchConfirming")

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInventoryInteract(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (player.isConfirmingRiskMatch()) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPickupItem(event: PlayerPickupItemEvent) {
        if (event.player.isConfirmingRiskMatch()) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDropItem(event: PlayerDropItemEvent) {
        if (event.player.isConfirmingRiskMatch()) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInteract(event: PlayerInteractEvent) {
        if (event.player.isConfirmingRiskMatch()) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onCommandPreProcess(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        if (player.isConfirmingRiskMatch()) {
            val messageLower = event.message.lowercase()
            if (!messageLower.startsWith("/hub")
                && !messageLower.startsWith("/lobby")
                && !messageLower.startsWith("/server")
                && !messageLower.startsWith("/list")
            ) {
                player.sendMessage(
                    AscendDuels.prefix("&cYou are currently pending for a risk match!")
                )
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBucketFill(event: PlayerBucketFillEvent) {
        if (event.player.isConfirmingRiskMatch()) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBucketEmpty(event: PlayerBucketEmptyEvent) {
        if (event.player.isConfirmingRiskMatch()) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onItemConsume(event: PlayerItemConsumeEvent) {
        if (event.player.isConfirmingRiskMatch()) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEditBook(event: PlayerEditBookEvent) {
        if (event.player.isConfirmingRiskMatch()) {
            event.isCancelled = true
        }
    }
}