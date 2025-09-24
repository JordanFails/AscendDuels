package me.jordanfails.ascendduels.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Description
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import net.pvpwars.core.util.CC

@CommandAlias("tppos")
class TPPosCommand : BaseCommand() {

    @Default
    @Syntax("<x> <y> <z> [yaw] [pitch] [world]")
    @CommandPermission("command.tppos")
    @Description("Teleport to given coordinates, with optional yaw, pitch, and world.")
    fun onTPPos(
        player: Player,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float? = null,
        pitch: Float? = null,
        worldName: String? = null
    ) {
        val world: World = if (worldName != null) {
            Bukkit.getWorld(worldName) ?: run {
                player.sendMessage(CC.translate("&cWorld '&e$worldName&c' not found!"))
                return
            }
        } else {
            player.world
        }

        val finalYaw = yaw ?: player.location.yaw
        val finalPitch = pitch ?: player.location.pitch

        val loc = Location(world, x, y, z, finalYaw, finalPitch)
        loc.chunk.load()
        player.teleport(loc)

        val worldDisplay = world.name
        player.sendMessage(
            CC.translate("&aTeleported to &e$x $y $z &ain &e$worldDisplay &a(yaw=$finalYaw, pitch=$finalPitch).")
        )
    }
}