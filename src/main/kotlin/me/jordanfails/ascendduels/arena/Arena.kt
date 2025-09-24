package me.jordanfails.ascendduels.arena

import com.google.common.base.Preconditions
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.utils.AngleUtils
import me.jordanfails.ascendduels.utils.Cuboid
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Skull
import java.util.*
import kotlin.math.abs

/**
 * Represents a pasted instance of an [ArenaSchematic].
 */
class Arena(
    val schematic: String?,
    val copy: Int,
    val bounds: Cuboid?
) {

    var team1Spawn: Location? = null
    var team2Spawn: Location? = null
    var spectatorSpawn: Location? = null
    var eventSpawns: MutableList<Location>? = null

    @get:JvmName("isInUse")
    internal var inUse: Boolean = false

    /** No-arg constructor for Gson */
    constructor() : this(null, 0, null)

    /**
     * Call this after pasting a schematic world edit to initialize spawn markers.
     */
    fun initializeSpawns() {
        if (bounds != null) {
            Preconditions.checkNotNull(bounds, "Bounds must not be null before scanning")
        }
        scanLocations()
    }

    fun getSpectatorSpawnLocation(): Location {
        if (spectatorSpawn != null) {
            return spectatorSpawn!!
        }
        val xDiff = abs(team1Spawn!!.blockX - team2Spawn!!.blockX)
        val yDiff = abs(team1Spawn!!.blockY - team2Spawn!!.blockY)
        val zDiff = abs(team1Spawn!!.blockZ - team2Spawn!!.blockZ)

        val newX = minOf(team1Spawn!!.blockX, team2Spawn!!.blockX) + (xDiff / 2)
        val newY = minOf(team1Spawn!!.blockY, team2Spawn!!.blockY) + (yDiff / 2)
        val newZ = minOf(team1Spawn!!.blockZ, team2Spawn!!.blockZ) + (zDiff / 2)

        val arenaHandler = AscendDuels.instance.arenaHandler
        spectatorSpawn = Location(arenaHandler.getArenaWorld(), newX.toDouble(), newY.toDouble(), newZ.toDouble())

        while (spectatorSpawn!!.block.type.isSolid) {
            spectatorSpawn!!.add(0.0, 1.0, 0.0)
        }
        return spectatorSpawn!!
    }

    private fun scanLocations() {
        forEachBlock { block ->
            if (block.type != Material.SKULL) return@forEachBlock

            val skull = block.state as Skull
            val below = block.getRelative(BlockFace.DOWN)

            val skullLocation =
                block.location.clone().add(0.5, 1.5, 0.5).apply {
                    yaw = AngleUtils.faceToYaw(skull.rotation) + 90f
                }

            when (skull.skullType) {
                SkullType.SKELETON -> {
                    spectatorSpawn = skullLocation
                    block.type = Material.AIR
                    if (below.type == Material.FENCE) below.type = Material.AIR
                }
                SkullType.PLAYER -> {
                    if (team1Spawn == null) {
                        team1Spawn = skullLocation
                    } else {
                        team2Spawn = skullLocation
                    }
                    block.type = Material.AIR
                    if (below.type == Material.FENCE) below.type = Material.AIR
                }
                SkullType.CREEPER -> {
                    block.type = Material.AIR
                    if (below.type == Material.FENCE) below.type = Material.AIR
                    if (eventSpawns == null) eventSpawns = mutableListOf()
                    if (!eventSpawns!!.contains(skullLocation)) {
                        eventSpawns!!.add(skullLocation)
                    }
                }
                else -> {}
            }
        }

        Preconditions.checkNotNull(team1Spawn, "Team 1 spawn (player skull) cannot be null.")
        Preconditions.checkNotNull(team2Spawn, "Team 2 spawn (player skull) cannot be null.")
    }

    private fun forEachBlock(callback: (Block) -> Unit) {
        val start = bounds!!.getLowerNE()
        val end = bounds.getUpperSW()
        val world = bounds.getWorld()

        for (x in start.blockX until end.blockX) {
            for (y in start.blockY until end.blockY) {
                for (z in start.blockZ until end.blockZ) {
                    callback(world.getBlockAt(x, y, z))
                }
            }
        }
    }

    private fun forEachChunk(callback: (Chunk) -> Unit) {
        val lowerX = bounds!!.getLowerX() shr 4
        val lowerZ = bounds.getLowerZ() shr 4
        val upperX = bounds.getUpperX() shr 4
        val upperZ = bounds.getUpperZ() shr 4
        val world = bounds.getWorld()

        for (x in lowerX..upperX) {
            for (z in lowerZ..upperZ) {
                callback(world.getChunkAt(x, z))
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Arena) return false
        return other.schematic == schematic && other.copy == copy
    }

    override fun hashCode(): Int {
        return Objects.hash(schematic, copy)
    }

    fun getSpawnLocation(index: Int): Location = when (index) {
        0 -> team1Spawn!!
        1 -> team2Spawn!!
        else -> throw IllegalArgumentException("Invalid spawn index: $index")
    }
}