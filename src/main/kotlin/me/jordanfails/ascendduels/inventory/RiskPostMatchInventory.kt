package me.jordanfails.ascendduels.inventory

import net.atlantismc.bukkit.menu.Menu
import net.atlantismc.bukkit.menu.button.Button
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

class RiskPostMatchInventory(
    target: Player // the player whose inventory is being shown
) : Menu() {

    val uuid: UUID = UUID.randomUUID()
    private val displayName: String = target.displayName
    private val contents: Array<ItemStack?> = target.inventory.contents
    private val armorContents: Array<ItemStack?> = target.inventory.armorContents

    override fun getTitle(player: Player): String {
        return "$displayName${ChatColor.GRAY}'s Inventory"
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        // Hotbar (0–8) → row 4 (slots 27–35)
        for (i in 0 until 9) {
            buttons[27 + i] = itemButton(contents.getOrNull(i))
        }

        // Main inventory (9–35) → slots 0–26
        for (i in 9 until contents.size) {
            buttons[i - 9] = itemButton(contents.getOrNull(i))
        }

        // Armor (helmet=39, chest=38, legs=37, boots=36)
        for (i in armorContents.indices) {
            buttons[39 - i] = itemButton(armorContents.getOrNull(i))
        }

        return buttons
    }

    private fun itemButton(item: ItemStack?): Button {
        return object : Button() {
            override fun getItem(player: Player): ItemStack {
                return item ?: ItemStack(0) // Material.AIR equivalent
            }
        }
    }
}