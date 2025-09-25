package me.jordanfails.ascendduels.utils.menu.buttons

import me.jordanfails.ascendduels.utils.menu.Button
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryView

class BackButton(
    private val destination: String? = null,
    private val callback: (Player) -> Unit
) : Button() {

    override fun getName(player: Player): String {
        return "${ChatColor.GREEN}Go Back"
    }

    override fun getDescription(player: Player): List<String> {
        return if (destination == null) {
            emptyList()
        } else {
            listOf("${ChatColor.GRAY}To $destination")
        }
    }

    override fun getMaterial(player: Player): Material {
        return Material.ARROW
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
        playNeutral(player)
        player.closeInventory()

        callback.invoke(player)
    }

}