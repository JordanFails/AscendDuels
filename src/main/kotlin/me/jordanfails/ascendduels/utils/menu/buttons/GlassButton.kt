package me.jordanfails.ascendduels.utils.menu.buttons

import me.jordanfails.ascendduels.utils.menu.Button
import org.bukkit.Material
import org.bukkit.entity.Player

class GlassButton(private val data: Byte) : Button() {

    override fun getName(player: Player): String {
        return " "
    }

    override fun getDescription(player: Player): List<String> {
        return emptyList()
    }

    override fun getMaterial(player: Player): Material {
        return Material.STAINED_GLASS_PANE
    }

    override fun getDamageValue(player: Player): Byte {
        return data
    }

}