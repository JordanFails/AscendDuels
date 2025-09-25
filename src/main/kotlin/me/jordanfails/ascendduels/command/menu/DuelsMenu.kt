package me.jordanfails.ascendduels.command.menu

import me.jordanfails.ascendduels.utils.menu.Button
import me.jordanfails.ascendduels.utils.menu.Menu
import me.jordanfails.ascendduels.utils.menu.buttons.GlassButton
import net.atlantismc.menus.item.ItemBuilder
import net.pvpwars.core.util.CC
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class DuelsMenu: Menu() {
    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()
        for (i in 0 until 10) {
            if (i == 4) continue
            buttons[i] = object : Button() {
                override fun getButtonItem(player: Player): ItemStack {
                    return ItemBuilder(Material.STAINED_GLASS_PANE)
                        .data(7)
                        .name(CC.translate(" &7"))
                        .build()
                }
            }

            buttons[i] = GlassButton(7)
        }

        buttons[4] = object : Button() {
            override fun getButtonItem(player: Player): ItemStack {
                return ItemBuilder(Material.STAINED_GLASS_PANE)
                    .data(0)
                    .name(CC.translate("&c&lDuel Queue"))
                    .lore(
                        CC.translate(
                            mutableListOf(
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
                            )
                        )
                    )
                    .build()
            }
        }

        buttons[12] = object : Button() {
            override fun getButtonItem(player: Player): ItemStack {
                return ItemBuilder(Material.IRON_SWORD)
                    .name("&b&lUnranked Queue")
                    .lore(
                        CC.translate(
                            mutableListOf(
                                "",
                                "&7Info: ",
                                "&f\u2022 &7In-Queue: &f0",
                                "&f\u2022 &7In-Game: &f0",
                                "",
                                "&7Click to view the kits for this queue."
                            )
                        )
                    ).build()
            }

        }

        buttons[13] = object : Button() {
            override fun getButtonItem(player: Player): ItemStack {
                return ItemBuilder(Material.DIAMOND_SWORD)
                    .name("&c&lRanked Queue")
                    .lore(
                        CC.translate(
                            mutableListOf(
                                "",
                                "&7Info: ",
                                "&f\u2022 &7In-Queue: &f0",
                                "&f\u2022 &7In-Game: &f0",
                                "",
                                "&7Notes:",
                                "&f\u2022 &7Dependent upon individual player ELO.",
                                "",
                                "&7Click to view the kits for this queue."
                            )
                        )
                    ).build()

            }
        }

        for (i in 18 until 26) {
            buttons[i] = object : Button() {
                override fun getButtonItem(player: Player): ItemStack {
                    return ItemBuilder(Material.STAINED_GLASS_PANE)
                        .data(7)
                        .name(CC.translate(" &7"))
                        .build()
                }
            }
        }
        return buttons;
    }

    override fun getTitle(player: Player): String {
        return "Duel Queue"
    }
}