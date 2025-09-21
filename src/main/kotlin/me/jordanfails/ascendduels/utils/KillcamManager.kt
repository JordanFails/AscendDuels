package me.jordanfails.ascendduels.utils

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.match.Match
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import java.util.UUID

object KillcamManager : Listener {

    private val activeReplays = mutableSetOf<UUID>()

    fun startKillcam(victim: Player, killer: Player, match: Match<*, *>) {
        activeReplays += victim.uniqueId

        // Freeze victim (1.8 trick: fly + stop speed)
        victim.allowFlight = true
        victim.isFlying = true
        victim.walkSpeed = 0f
        victim.flySpeed = 0f

        val manager = ProtocolLibrary.getProtocolManager()

        // Loop task to make victim look at killer
        val task = Bukkit.getScheduler().runTaskTimer(
            AscendDuels.instance, Runnable {
                if (!victim.isOnline || !killer.isOnline) return@Runnable

                val loc = victim.location
                val target = killer.location.clone()
                    .add(0.0, victim.eyeHeight, 0.0)

                val dir = target.subtract(loc).toVector().normalize()
                loc.direction = dir

                val packet = manager.createPacket(PacketType.Play.Server.POSITION)
                packet.doubles.write(0, loc.x)
                packet.doubles.write(1, loc.y)
                packet.doubles.write(2, loc.z)
                packet.float.write(0, loc.yaw)
                packet.float.write(1, loc.pitch)

                manager.sendServerPacket(victim, packet)
            },
            0L, 3L
        )

        // After 3s -> stop replay and finish death
        Bukkit.getScheduler().runTaskLater(
            AscendDuels.instance, Runnable {
                task.cancel()
                activeReplays -= victim.uniqueId

                // restore movement
                victim.walkSpeed = 0.2f
                victim.flySpeed = 0.1f
                victim.allowFlight = false
                victim.isFlying = false

                // Finish death in match system
                match.onDeath(victim, Match.DeathReason.OTHER)

                // Force respawn (1.8 trick)
                victim.spigot().respawn()
            }, 60L // 3 seconds
        )
    }

    fun isInKillcam(uuid: UUID): Boolean = uuid in activeReplays

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        if (isInKillcam(player.uniqueId)) {
            event.isCancelled = true
        }
    }
}