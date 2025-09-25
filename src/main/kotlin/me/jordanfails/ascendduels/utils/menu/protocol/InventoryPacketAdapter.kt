package me.jordanfails.ascendduels.utils.menu.protocol

import me.jordanfails.ascendduels.utils.menu.protocol.event.PlayerCloseInventoryEvent
import me.jordanfails.ascendduels.utils.menu.protocol.event.PlayerOpenInventoryEvent
import java.util.UUID
import java.util.HashSet
import org.bukkit.Bukkit
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.events.PacketAdapter
import me.jordanfails.ascendduels.AscendDuels

object InventoryPacketAdapter : PacketAdapter(AscendDuels.instance, PacketType.Play.Client.CLIENT_COMMAND, PacketType.Play.Client.CLOSE_WINDOW) {

    override fun onPacketReceiving(event: PacketEvent?) {
        val player = event!!.player
        val packet = event.packet

        if (packet.type === PacketType.Play.Client.CLIENT_COMMAND && packet.clientCommands.size() != 0 && packet.clientCommands.read(0) == EnumWrappers.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
            if (!currentlyOpen.contains(player.uniqueId)) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(AscendDuels.instance) { Bukkit.getPluginManager().callEvent(PlayerOpenInventoryEvent(player)) }
            }

            currentlyOpen.add(player.uniqueId)
        } else if (packet.type === PacketType.Play.Client.CLOSE_WINDOW) {
            if (currentlyOpen.contains(player.uniqueId)) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(AscendDuels.instance) { Bukkit.getPluginManager().callEvent(PlayerCloseInventoryEvent(player)) }
            }

            currentlyOpen.remove(player.uniqueId)
        }
    }

    @JvmStatic
    var currentlyOpen: HashSet<UUID> = HashSet()

}
