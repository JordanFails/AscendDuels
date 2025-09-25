package me.jordanfails.ascendduels.utils.menu.protocol

import me.jordanfails.ascendduels.utils.Reflection
import org.bukkit.Material

object MenuCompatibility {

    private val BARRIER = Reflection.getEnum(Material::class.java, "BARRIER") as Material?

    @JvmStatic
    fun getBarrierOrReplacement(): Material {
        return BARRIER ?: Material.REDSTONE
    }

}