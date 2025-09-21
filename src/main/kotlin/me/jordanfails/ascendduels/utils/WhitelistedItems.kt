package me.jordanfails.ascendduels.utils

import net.pvpwars.core.Core
import net.pvpwars.core.game.features.pets.PetSystem
import net.pvpwars.core.game.features.pets.impl.InventoryPet
import net.pvpwars.core.game.features.pets.impl.cursed.WizardPet
import net.pvpwars.core.util.material.MaterialUtil
import net.pvpwars.core.util.nbt.NBTEditor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

object WhitelistedItems {
    fun getDisallowedItems(player: Player): MutableSet<ItemStack?> {
        val inventory = player.inventory
        return Stream.concat(
            Arrays.stream(inventory.contents),
            Arrays.stream(inventory.armorContents)
        )
            .filter { itemStack: ItemStack? -> !isWhitelistedItem(itemStack) }
            .collect(Collectors.toSet())
    }

    private fun isWhitelistedItem(itemStack: ItemStack?): Boolean {
        if (itemStack == null) return true

        if ((itemStack.hasItemMeta() && NBTEditor.contains(itemStack, "casino_merchant"))
            || (itemStack.hasItemMeta()
                    && NBTEditor.contains(itemStack, "clickItem")
                    && NBTEditor.getString(itemStack, "clickItem") == "mystery_pet_level_100")
        ) {
            return false
        }

        val type = itemStack.type

        val pet: InventoryPet? = Core.getInstance().systemManager.get(PetSystem::class.java).petRegistry.getByItem(itemStack)

        if (pet is WizardPet) {
            return false
        }

        if (type == Material.AIR || type == Material.POTION || type == Material.EMERALD || type == Material.ARROW || type == Material.ENDER_PEARL || type.isEdible
            || type == Material.FISHING_ROD || type == Material.SKULL || type == Material.SKULL_ITEM || MaterialUtil.isEquipment(
                itemStack
            )
            || Core.getInstance().systemManager.get(PetSystem::class.java).petRegistry.isPet(itemStack) //                || Core.getInstance().getSystemManager().get(AbilityItemsSystem.class).getByItem(itemStack) != null
        ) {
            return true
        }

        return false
    }
}