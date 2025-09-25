package me.jordanfails.ascendduels.utils.menu.menus

import me.jordanfails.ascendduels.utils.menu.Button
import me.jordanfails.ascendduels.utils.menu.protocol.MenuCompatibility
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class ExitButton : Button() {

    override fun getName(player: Player): String {
        return "${ChatColor.RED}${ChatColor.BOLD}Exit"
    }

    override fun getMaterial(player: Player): Material {
        return MenuCompatibility.getBarrierOrReplacement()
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
        playNeutral(player)
        player.closeInventory()
    }

}