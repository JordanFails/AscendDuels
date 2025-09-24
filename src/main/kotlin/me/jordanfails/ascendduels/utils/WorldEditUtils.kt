package me.jordanfails.ascendduels.utils

import com.sk89q.worldedit.*
import com.sk89q.worldedit.blocks.BaseBlock
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.schematic.SchematicFormat
import com.sk89q.worldedit.world.World
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.arena.ArenaSchematic
import org.bukkit.Location
import org.bukkit.Material
import java.io.File

/**
 * WorldEdit utilities for schematic handling, clearing, pasting, etc.
 *
 * A Kotlin translation of WorldEditUtils (static Java utility).
 */
object WorldEditUtils {

    private var editSession: EditSession? = null
    private var worldEditWorld: World? = null

    fun primeWorldEditApi() {
        if (editSession != null) return

        val esFactory: EditSessionFactory = WorldEdit.getInstance().editSessionFactory
        val arenaHandler = AscendDuels.instance.arenaHandler

        worldEditWorld = BukkitWorld(arenaHandler.getArenaWorld())
        editSession = esFactory.getEditSession(worldEditWorld, Int.MAX_VALUE)
    }

    @Throws(Exception::class)
    fun paste(schematic: ArenaSchematic, pasteAt: Vector): CuboidClipboard {
        primeWorldEditApi()

        val clipboard = SchematicFormat.MCEDIT.load(schematic.getSchematicFile())

        // Avoid offset from schematic copy position
        clipboard.offset = Vector(0, 0, 0)
        clipboard.paste(editSession, pasteAt, true)

        return clipboard
    }

    @Throws(Exception::class)
    fun save(schematic: ArenaSchematic, saveFrom: Vector) {
        primeWorldEditApi()

        val schematicSize = readSchematicSize(schematic)

        val newSchematic = CuboidClipboard(schematicSize, saveFrom)
        newSchematic.copy(editSession)

        SchematicFormat.MCEDIT.save(newSchematic, schematic.getSchematicFile())
    }

    fun clear(bounds: Cuboid?) {
        if( bounds == null) return
        clear(
            Vector(bounds.getLowerX(), bounds.getLowerY(), bounds.getLowerZ()),
            Vector(bounds.getUpperX(), bounds.getUpperY(), bounds.getUpperZ())
        )
    }

    fun clear(lower: Vector, upper: Vector) {
        primeWorldEditApi()

        val air = BaseBlock(Material.AIR.id)
        val region: Region = CuboidRegion(worldEditWorld, lower, upper)

        try {
            editSession?.setBlocks(region, air)
        } catch (ex: MaxChangedBlocksException) {
            // shouldn’t happen with Int.MAX_VALUE
            throw RuntimeException(ex)
        }
    }

    @Throws(Exception::class)
    fun readSchematicSize(schematic: ArenaSchematic): Vector {
        val schematicFile: File = schematic.getSchematicFile()
        val clipboard: CuboidClipboard = SchematicFormat.MCEDIT.load(schematicFile)
        return clipboard.size
    }

    fun vectorToLocation(vector: Vector): Location {
        val arenaHandler = AscendDuels.instance.arenaHandler
        return Location(
            arenaHandler.getArenaWorld(),
            vector.blockX.toDouble(),
            vector.blockY.toDouble(),
            vector.blockZ.toDouble()
        )
    }
}