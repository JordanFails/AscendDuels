package net.pvpwars.duels.listeners

import com.rit.sucy.enchanting.EEquip
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.arena.ArenaService
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
        val arenaService: ArenaService = AscendDuels.instance.arenaService
        val toWorld: World = event.to.world
        val fromWorld: World = event.from.world
        if (toWorld == fromWorld) return

        val player = event.player

        if (arenaService.isWorld(toWorld)) {
            // Entering an arena world
            player.setMetadata("ignoreSector", FixedMetadataValue(AscendDuels.instance, true))
            player.setMetadata("friendlyFire", FixedMetadataValue(AscendDuels.instance, true))
            Core.getInstance().systemManager
                .get(ExtensionSystem::class.java)
                .getExtension(ClaimExtension::class.java)
                .markBusy(player, true)

            pinInventory(player)
            player.removeMetadata("riskMatchConfirming", AscendDuels.instance)

        } else if (arenaService.isWorld(fromWorld)) {
            // Leaving an arena world
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
            val craftPlayer = player as CraftPlayer

            // Spigot-Patches: SimplyTrash (disabled in this port)
            // craftPlayer.handle.extInventoryPin()
            craftPlayer.saveData()

            if (match !is RiskMatch) {
                player.inventory.clear()
                player.inventory.armorContents = null
                player.updateInventory()
            }

            EEquip(player).run()
        }

        fun restoreInventory(player: Player) {
            val craftPlayer = player as CraftPlayer
            // Spigot-Patches: SimplyTrash (disabled in this port)
            // craftPlayer.handle.extInventoryPinRestore()
            craftPlayer.saveData()
            EEquip(player).run()
        }
    }
}