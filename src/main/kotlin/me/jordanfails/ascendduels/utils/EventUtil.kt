package me.jordanfails.ascendduels.utils

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile

object EventUtil {
    fun linkProjectile(damager: Entity?): Player? {
        if (damager is Player) {
            return damager
        } else if (damager is Projectile) {
            val projectileSource = damager.shooter
            if (projectileSource is Player) return projectileSource
        }

        return null
    }
}