package me.jordanfails.ascendduels.utils

import com.infusedpvp.commons.lombok.experimental.UtilityClass
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@UtilityClass
class AngleUtils {
    fun yawDiff(a: Double, b: Double): Double {
        val mi = min(a, b)
        val mx = max(a, b)
        return min(mx - mi, mi + 360 - mx)
    }

    fun faceTo(a: Location, b: Location): Boolean {
        val dx = b.getX() - a.getX()
        val dz = b.getZ() - a.getZ()
        var ang = Math.toDegrees(acos(dz / sqrt(dx * dx + dz * dz)))
        if (dx > 0) {
            ang = -ang
        }
        return yawDiff(a.getYaw().toDouble(), ang) <= 90
    }

    fun isInRange(player: Player, target: Player, range: Double): Boolean {
        return player.getEyeLocation().distance(target.getLocation()) <= range || player.getLocation()
            .distance(target.getLocation()) <= range
    }

    companion object {
        private val NOTCHES: MutableMap<BlockFace?, Int?> = EnumMap<BlockFace?, Int?>(BlockFace::class.java)

        init {
            val radials = arrayOf<BlockFace?>(
                BlockFace.WEST,
                BlockFace.NORTH_WEST,
                BlockFace.NORTH,
                BlockFace.NORTH_EAST,
                BlockFace.EAST,
                BlockFace.SOUTH_EAST,
                BlockFace.SOUTH,
                BlockFace.SOUTH_WEST
            )

            for (i in radials.indices) {
                NOTCHES[radials[i]] = i
            }
        }

        fun faceToYaw(face: BlockFace?): Int {
            return wrapAngle(45 * NOTCHES.getOrDefault(face, 0)!!)
        }

        private fun wrapAngle(angle: Int): Int {
            var wrappedAngle = angle

            while (wrappedAngle <= -180) {
                wrappedAngle += 360
            }

            while (wrappedAngle > 180) {
                wrappedAngle -= 360
            }

            return wrappedAngle
        }
    }
}