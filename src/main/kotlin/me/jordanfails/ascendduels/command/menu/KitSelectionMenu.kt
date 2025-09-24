package me.jordanfails.ascendduels.command.menu

import me.jordanfails.ascendduels.kit.Kit
import net.atlantismc.menus.menu.Menu
import net.atlantismc.menus.menu.button.Button
import net.pvpwars.core.util.item.ItemBuilder
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class KitSelectionMenu : Menu() {
    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()
        return buttons
    }

    override fun getTitle(player: Player): String {
        return "Select a Kit - 1/1"
    }

    inner class KitButton(
        val kit: Kit
    ): Button() {
        override fun getItem(player: Player): ItemStack {
            return ItemBuilder(kit.displayItem)
                .lore(kit.displayItem.itemMeta.lore)
        }
    }
}