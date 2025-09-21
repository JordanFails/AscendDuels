package me.jordanfails.ascendduels.kit

import com.rit.sucy.enchanting.EEquip
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.api.serializable.inventory.JsonInventory
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class KitInventory : JsonInventory {

    constructor() : super()

    constructor(contents: Array<ItemStack?>, armorContents: Array<ItemStack?>)
            : super(contents, armorContents)

    fun load(player: Player) = load(player, ignoreContents = false)

    fun load(player: Player, ignoreContents: Boolean) {
        player.exp = 0.0f
        player.totalExperience = 0

        // remove all active potion effects
        player.activePotionEffects.forEach { effect ->
            player.removePotionEffect(effect.type)
        }

        if (!ignoreContents) {
            player.inventory.contents = contents
            player.inventory.armorContents = armorContents
            player.updateInventory()
        }

        // Bukkit scheduler mirror of Java: run EEquip slightly delayed
        EEquip(player).runTaskLater(AscendDuels.instance, 1L)
    }
}