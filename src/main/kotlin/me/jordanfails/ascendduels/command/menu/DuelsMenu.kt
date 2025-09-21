package me.jordanfails.ascendduels.command.menu

import net.atlantismc.bukkit.menu.Menu
import net.atlantismc.bukkit.menu.button.Button
import net.pvpwars.core.util.CC
import net.pvpwars.core.util.item.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class DuelsMenu: Menu() {
    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()
        for(i in 0 until 10) {
            if(i == 4) continue
            buttons[i] = object : Button() {
                override fun getItem(player: Player): ItemStack {
                    return ItemBuilder(Material.STAINED_GLASS_PANE)
                        .data(7)
                        .name(CC.translate(" &7"))
                }
            }

            buttons[i] = object : Button() {
                override fun getItem(player: Player): ItemStack {
                    return ItemBuilder(Material.STAINED_GLASS_PANE)
                        .data(7)
                        .name(CC.translate(" &7"))
                }
            }

            buttons[4] = object : Button() {
                override fun getItem(player: Player): ItemStack {
                    return ItemBuilder(Material.STAINED_GLASS_PANE)
                        .data(0)
                        .name(CC.translate("&c&lDuel Queue"))
                        .lore(CC.translate(mutableListOf(
                            "",
                            "&fBelow you'll find two separate queues",
                            "&fthat anyone can join. Unranked vs. Ranked.",
                            "&fUnranked provides practice while Ranked",
                            "&fprovides a competitive space to fight",
                            "&fagainst other players. Ranked will have",
                            "&fleaderboards and the winner of each leader-",
                            "&fboard when it closes will receive rewards.",
                            "&f",
                            "&fTo get started, simply make sure that:",
                            "&f\u2022 Your inventory is empty (for non-Arcade).",
                            "&f\u2022 You're currently standing in a Safe-Zone.",
                            "&f",
                            "&fClick one of th queues below to begin!"
                        )))
                }

            }
        }

        return buttons;
    }

    override fun getTitle(player: Player): String {
        return "Duel Queue"
    }
}