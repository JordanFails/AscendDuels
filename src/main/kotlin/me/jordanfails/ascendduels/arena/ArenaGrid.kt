package me.jordanfails.ascendduels.arena

import com.sk89q.worldedit.CuboidClipboard
import com.sk89q.worldedit.Vector
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.utils.Cuboid
import me.jordanfails.ascendduels.utils.WorldEditUtils
import org.bukkit.scheduler.BukkitRunnable
import java.io.IOException

/**
 * Represents the grid on the world
 *
 *   Z ------------->
 *  X  (1,1) (1,2)
 *  |  (2,1) (2,2)
 *  |  (3,1) (3,2)
 *  |  (4,1) (4,2)
 *  V
 *
 *  X is per [ArenaSchematic] and is stored in [ArenaSchematic.gridIndex].
 *  Z is per [Arena] and is the [Arena.copy].
 *
 *  Each arena is allocated [GRID_SPACING_Z] by [GRID_SPACING_X] blocks
 */
class ArenaGrid {

    /**
     * 'Starting' point of the grid. Expands (+, +) from this point.
     */
    companion object {
        val STARTING_POINT: Vector = Vector(1000, 80, 1000)
        const val GRID_SPACING_X = 300
        const val GRID_SPACING_Z = 300
    }

    var busy: Boolean = false
        private set

    fun scaleCopies(schematic: ArenaSchematic, desiredCopies: Int, callback: () -> Unit) {
        check(!busy) { "Grid is busy!" }

        busy = true

        val arenaHandler = AscendDuels.instance.arenaHandler
        val currentCopies = arenaHandler.countArenas(schematic)

        val saveWrapper: () -> Unit = {
            try {
                arenaHandler.saveArenas()
            } catch (ex: IOException) {
                throw RuntimeException(ex)
            }
            busy = false
            callback()
        }

        when {
            currentCopies > desiredCopies ->
                deleteArenas(schematic, currentCopies, currentCopies - desiredCopies, saveWrapper)
            currentCopies < desiredCopies ->
                createArenas(schematic, currentCopies, desiredCopies - currentCopies, saveWrapper)
            else -> saveWrapper()
        }
    }

    private fun createArenas(
        schematic: ArenaSchematic,
        currentCopies: Int,
        toCreate: Int,
        callback: () -> Unit
    ) {
        val arenaHandler = AscendDuels.instance.arenaHandler

        object : BukkitRunnable() {
            var created = 0

            override fun run() {
                val copy: Int = currentCopies + created + 1 // 1-indexed arenas
                val xStart = STARTING_POINT.blockX + (GRID_SPACING_X * schematic.gridIndex)
                val zStart = STARTING_POINT.blockZ + (GRID_SPACING_Z * copy)

                try {
                    val createdArena = createArena(schematic, xStart, zStart, copy)
                    arenaHandler.registerArena(createdArena)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    callback()
                    cancel()
                    return
                }

                created++
                if (created == toCreate) {
                    callback()
                    cancel()
                }
            }
        }.runTaskTimer(AscendDuels.instance, 8L, 8L)
    }

    private fun deleteArenas(
        schematic: ArenaSchematic,
        currentCopies: Int,
        toDelete: Int,
        callback: () -> Unit
    ) {
        val arenaHandler = AscendDuels.instance.arenaHandler

        object : BukkitRunnable() {
            var deleted = 0

            override fun run() {
                val copy: Int = currentCopies - deleted
                val existing = arenaHandler.getArena(schematic, copy)

                if (existing != null) {
                    WorldEditUtils.clear(existing.bounds)
                    arenaHandler.unregisterArena(existing)
                }

                deleted++

                if (deleted == toDelete) {
                    callback()
                    cancel()
                }
            }
        }.runTaskTimer(AscendDuels.instance, 8L, 8L)
    }

    private fun createArena(schematic: ArenaSchematic, xStart: Int, zStart: Int, copy: Int): Arena {
        val pasteAt = Vector(xStart.toDouble(), STARTING_POINT.y, zStart.toDouble())
        val clipboard: CuboidClipboard = try {
            WorldEditUtils.paste(schematic, pasteAt)
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

        val lowerCorner = WorldEditUtils.vectorToLocation(pasteAt)
        val upperCorner = WorldEditUtils.vectorToLocation(pasteAt.add(clipboard.size))

        val arena = Arena(
            schematic.name,
            copy,
            Cuboid(lowerCorner, upperCorner)
        )

        // 🔑 IMPORTANT: initialize spawns after pasting schematic
        arena.initializeSpawns()

        return arena
    }

    fun free() {
        busy = false
    }
}