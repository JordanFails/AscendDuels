package me.jordanfails.ascendduels.kit

import com.google.gson.JsonObject
import com.google.gson.JsonParser
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
            AscendDuels.instance.logger.warning("Failed to set NBT tag '$tagName' on item ${item.type}: ${e.message}")
            item // Return original item if NBT fails
        }
    }

    fun load(player: Player) = load(player, ignoreContents = false)

    fun load(player: Player, ignoreContents: Boolean = false) {
        // Reset XP and effects
        player.exp = 0.0f
        player.totalExperience = 0
        player.level = 0
        player.activePotionEffects.forEach { effect ->
            player.removePotionEffect(effect.type)
        }

        // Mark duel items in storage arrays safely
        if(contents.isNotEmpty()) {
            contents = contents.map { item ->
                if (item != null && item.type != null) {
                    addNBTTag(item, "duelItem", true)
                } else null
            }.toTypedArray()
        }

        if(armorContents.isNotEmpty()) {
            armorContents = armorContents.map { item ->
                if (item != null && item.type != null) {
                    addNBTTag(item, "duelItem", true)
                } else null
            }.toTypedArray()
        }

        if (!ignoreContents) {
            player.inventory.contents = contents
            player.inventory.armorContents = armorContents
            player.updateInventory() // acceptable if needed on Paper
        }

        // Run EEquip (will ensure correct armor equip handling)
        EEquip(player).runTaskLater(AscendDuels.instance, 1L)
    }
}