package me.jordanfails.ascendduels.utils.menu.protocol

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.utils.menu.Menu

object MenuPacketAdapter : PacketAdapter(AscendDuels.instance, PacketType.Play.Client.CLOSE_WINDOW) {

    override fun onPacketReceiving(event: PacketEvent) {
        if (Menu.currentlyOpenedMenus.containsKey(event.player.uniqueId)) {
            Menu.currentlyOpenedMenus[event.player.uniqueId]?.manualClose = true
        }
    }

}