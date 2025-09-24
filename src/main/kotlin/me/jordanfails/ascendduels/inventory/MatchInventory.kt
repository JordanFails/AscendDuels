package me.jordanfails.ascendduels.inventory

import com.google.common.collect.Lists
import com.rit.sucy.service.ERomanNumeral
import me.jordanfails.ascendduels.match.MatchStatistics
import net.atlantismc.menus.menu.Menu
import net.atlantismc.menus.menu.button.Button
import net.pvpwars.core.util.item.ItemBuilder
import org.apache.commons.lang.time.DurationFormatUtils
import org.apache.commons.lang3.text.WordUtils
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.SkullType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.Potion
import java.util.concurrent.TimeUnit

class MatchInventory(
    private val statsPlayer: Player,
    private val statistics: MatchStatistics
) : Menu() {

    override fun getTitle(player: Player): String {
        return "${statsPlayer.displayName}${ChatColor.GRAY}'s Inventory"
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        val contents = statsPlayer.inventory.contents
        val armor = statsPlayer.inventory.armorContents

        // Hotbar (0–8) into slots 27–35
        for (i in 0 until 9) {
            buttons[27 + i] = simpleButton(contents.getOrNull(i))
        }

        // Main inventory (9–35) into slots 0–26
        for (i in 9 until contents.size) {
            buttons[i - 9] = simpleButton(contents.getOrNull(i))
        }

        // Armor: helmet=39, chest=38, legs=37, boots=36
        for (i in armor.indices) {
            buttons[39 - i] = simpleButton(armor.getOrNull(i))
        }

        // PvP Info (slot 48)
        buttons[48] = object : Button() {
            override fun getItem(player: Player): ItemStack {
                return ItemBuilder(Material.DIAMOND_SWORD)
                    .name("&b&lPvP Info")
                    .lore(
                        " ",
                        "&fHits:",
                        " &8• &bThrown: &f${statistics.hitsThrown}",
                        " &8• &bLanded: &f${statistics.hitsLanded}",
                        " &8• &bMissed: &f${statistics.hitsMissed}",
                        " ",
                        "&fDamage Dealt: &b${statistics.damageDealt}",
                        "&fLongest Combo: &b${statistics.longestCombo}",
                        " "
                    )
                    .flag(ItemFlag.HIDE_ATTRIBUTES)
                    .flag(ItemFlag.HIDE_DESTROYS)
            }
        }

        // Heal Info (slot 49)
        when (statistics.healType) {
            MatchStatistics.HealType.POT -> {
                val accuracy =
                    if (statistics.healsUsed > 0)
                        (((statistics.healsUsed - statistics.healsMissed).toDouble()) /
                                statistics.healsUsed) * 100.0
                    else 0.0
                buttons[49] = object : Button() {
                    override fun getItem(player: Player): ItemStack {
                        return ItemBuilder(
                            Potion.fromDamage(16421)
                                .toItemStack(maxOf(statistics.healsRemaining, 1))
                        )
                            .name("&b&lHeal Info")
                            .lore(
                                " ",
                                "&fPotions:",
                                " &8• &bUsed: &f${statistics.healsUsed}",
                                " &8• &bRemaining: &f${statistics.healsRemaining}",
                                " &8• &bMissed: &f${statistics.healsMissed}",
                                " &8• &bAccuracy: &f${"%.2f".format(accuracy)}%",
                                " "
                            )
                            .flag(ItemFlag.HIDE_ATTRIBUTES)
                            .flag(ItemFlag.HIDE_DESTROYS)
                    }
                }
            }

            MatchStatistics.HealType.SOUP -> {
                buttons[49] = object : Button() {
                    override fun getItem(player: Player): ItemStack {
                        return ItemBuilder(Material.MUSHROOM_SOUP)
                            .amount(maxOf(statistics.healsRemaining, 1))
                            .name("&b&lHeal Info")
                            .lore(
                                " ",
                                "&fSoups:",
                                " &8• &bUsed: &f${statistics.healsUsed}",
                                " &8• &bRemaining: &f${statistics.healsRemaining}",
                                " "
                            )
                            .flag(ItemFlag.HIDE_ATTRIBUTES)
                            .flag(ItemFlag.HIDE_DESTROYS)
                    }
                }
            }

            else -> {}
        }

        // Player Info (slot 50)
        val healthRounded = Math.round(statistics.health * 2.0f) / 2.0f
        val effectsLore = if (statistics.effects!!.isEmpty()) {
            Lists.newArrayList(" &8• &c&oNone")
        } else {
            statistics.effects!!.map { potionEffect ->
                val name = WordUtils.capitalizeFully(potionEffect!!.type.name.replace("_", " "))
                val roman = ERomanNumeral.numeralOf(potionEffect.amplifier + 1)

                val duration = if (potionEffect.duration >= 999999) {
                    "∞"
                } else {
                    DurationFormatUtils.formatDuration(
                        TimeUnit.SECONDS.toMillis(potionEffect.duration / 20L),
                        "m:ss"
                    )
                }
                " &8• &b$name $roman: &f$duration"
            }.toMutableList()
        }
        effectsLore.add(" ")

        buttons[50] = object : Button() {
            override fun getItem(player: Player): ItemStack {
                return ItemBuilder(Material.SKULL_ITEM, SkullType.PLAYER.ordinal.toByte())
                    .owner(statsPlayer.name)
                    .name("&b&lPlayer Info")
                    .lore(
                        " ",
                        "&fBase:",
                        " &8• &bHealth: &f${healthRounded}&c♥",
                        " &8• &bHunger: &f${statistics.hunger}",
                        " ",
                        "&fEffects:"
                    )
                    .lore(effectsLore)
                    .flag(ItemFlag.HIDE_ATTRIBUTES)
                    .flag(ItemFlag.HIDE_DESTROYS)
            }
        }

        return buttons
    }

    private fun simpleButton(stack: ItemStack?): Button {
        return object : Button() {
            override fun getItem(player: Player) = stack ?: ItemStack(0)
        }
    }
}