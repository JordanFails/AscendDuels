package me.jordanfails.ascendduels.utils.menu.pagination

import me.jordanfails.ascendduels.utils.menu.Button
import net.atlantismc.menus.item.ItemBuilder
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

class PageButton(private val mod: Int, private val menu: PaginatedMenu) : Button() {

    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
        when {
            clickType == ClickType.RIGHT -> {
                ViewAllPagesMenu(menu).openMenu(player)
                playNeutral(player)
            }
            hasNext(player) -> {
                menu.modPage(player, mod)
                playNeutral(player)
            }
            else -> playFail(player)
        }
    }

    private fun hasNext(player: Player): Boolean {
        val pg = menu.page + mod
        return pg > 0 && menu.getPages(player) >= pg
    }

    override fun getName(player: Player): String {
        if (!this.hasNext(player)) {
            return if (this.mod > 0) {
                "§7Last Page"
            } else {
                "§7First Page"
            }
        }

        return if (this.mod > 0) {
            "${ChatColor.YELLOW}(${(menu.page + mod)}/${menu.getPages(player)}) ➜"
        } else {
            "⟵ ${ChatColor.YELLOW}(${(menu.page + mod)}/${menu.getPages(player)})"
        }
    }

    override fun getDescription(player: Player): List<String> {
        return listOf(
            ChatColor.GRAY.toString() + "Click to navigate pages."
        )
    }

    override fun getDamageValue(player: Player): Byte {
        return 0 // not relevant with ItemBuilder + Paper
    }

    override fun getMaterial(player: Player): Material {
        return Material.PAPER
    }

    override fun getButtonItem(player: Player): ItemStack {
        return ItemBuilder(Material.PAPER)
            .name(getName(player))
            .lore(getDescription(player))
            .build()
    }
}