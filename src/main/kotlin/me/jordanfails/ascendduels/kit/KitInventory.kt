package me.jordanfails.ascendduels.kit

import com.rit.sucy.enchanting.EEquip
import de.tr7zw.changeme.nbtapi.NBTItem
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.api.serializable.inventory.JsonInventory
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class KitInventory : JsonInventory {

    constructor() : super()

    constructor(contents: Array<ItemStack?>, armorContents: Array<ItemStack?>)
            : super(contents, armorContents)

    /**
     * Safely adds NBT tags to an ItemStack
     * @param item The ItemStack to add tags to
     * @param tagName The name of the NBT tag
     * @param value The boolean value to set
     * @return The ItemStack with NBT tags, or original item if tagging fails
     */
    private fun addNBTTag(item: ItemStack, tagName: String, value: Boolean): ItemStack {
        return try {
            val nbtItem = NBTItem(item)
            nbtItem.setBoolean(tagName, value)
            nbtItem.item
        } catch (e: Exception) {
            AscendDuels.instance.logger.warning(
                "Failed to set NBT tag '$tagName' on item ${item.type}: ${e.message}"
            )
            item // Return original item if NBT fails
        }
    }

    fun load(player: Player) = load(player, ignoreContents = false)

    fun load(player: Player, ignoreContents: Boolean = false) {
        player.exp = 0.0f
        player.totalExperience = 0
        player.level = 0
        player.activePotionEffects.forEach { effect ->
            player.removePotionEffect(effect.type)
        }

        if (contents.isNotEmpty()) {
            // Ensure duelItem NBT tags are applied
            contents = contents.map { item ->
                val safeItem = item ?: return@map null
                addNBTTag(safeItem, "duelItem", true)
            }.toTypedArray()

            // --- NEW LOGIC: Check item count ---
            val nonNullItems = contents.filterNotNull()
            if (nonNullItems.isEmpty()) {
                // No items at all → log warning (or could give a default)
                AscendDuels.instance.logger.warning(
                    "KitInventory for ${player.name} had no valid items in contents!"
                )
            } else if (nonNullItems.size > 1) {
                // More than one item → mark them with a custom tag as multiple
                contents = contents.map { item ->
                    val safeItem = item ?: return@map null
                    addNBTTag(safeItem, "hasMultiple", true)
                }.toTypedArray()
            }
        }

        if (armorContents.isNotEmpty()) {
            armorContents = armorContents.map { item ->
                if (item == null) return@map null
                item.amount = 1
                addNBTTag(item, "duelItem", true)
            }.toTypedArray()
        }

        if (!ignoreContents) {
            player.inventory.contents = contents
            player.inventory.armorContents = armorContents
            player.updateInventory()
        }

        EEquip(player).runTaskLater(AscendDuels.instance, 1L)
    }
}