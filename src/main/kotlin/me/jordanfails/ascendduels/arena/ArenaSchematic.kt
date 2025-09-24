package me.jordanfails.ascendduels.arena

import com.sk89q.worldedit.Vector
import me.jordanfails.ascendduels.arena.ArenaGrid.Companion.GRID_SPACING_X
import me.jordanfails.ascendduels.arena.ArenaGrid.Companion.STARTING_POINT
import me.jordanfails.ascendduels.utils.WorldEditUtils
import java.io.File
import java.util.*

/**
 * Represents an arena schematic. See [net.frozenorb.potpvp.arena]
 * for a comparison of [Arena]s and [ArenaSchematic]s.
 */
class ArenaSchematic() {

    /**
     * Name of this schematic (ex "Candyland")
     */
    var name: String? = null

    /**
     * If matches can be scheduled on an instance of this arena.
     */
    var enabled: Boolean = true

    /** Max players in this arena */
    var maxPlayerCount: Int = 256

    /** Min players in this arena */
    var minPlayerCount: Int = 2

    /** If usable for ranked matches */
    var supportsRanked: Boolean = false

    /** If usable only for archer matches */
    var archerOnly: Boolean = false

    /** If usable only for team fights */
    var teamFightsOnly: Boolean = false

    /** If usable only for Sumo matches */
    var sumoOnly: Boolean = false

    /** If usable only for Spleef matches */
    var spleefOnly: Boolean = false

    /** If usable only for BuildUHC */
    var buildUHCOnly: Boolean = false

    /** If usable only for HCF */
    var HCFOnly: Boolean = false

    /** Optional event tag */
    var eventName: String? = null

    /**
     * Index on the X axis on the grid (and in calculations regarding model arenas)
     * @see ArenaGrid
     */
    var gridIndex: Int = 0

    constructor(name: String) : this() {
        this.name = name
    }

    fun getSchematicFile(): File {
        return File(ArenaHandler.WORLD_EDIT_SCHEMATICS_FOLDER, "$name.schematic")
    }

    fun getModelArenaLocation(): Vector {
        val xModifier = GRID_SPACING_X * gridIndex
        return Vector(
            STARTING_POINT.blockX - xModifier,
            STARTING_POINT.blockY,
            STARTING_POINT.blockZ
        )
    }

    @Throws(Exception::class)
    fun pasteModelArena() {
        val start = getModelArenaLocation()
        WorldEditUtils.paste(this, start)
    }

    @Throws(Exception::class)
    fun removeModelArena() {
        val start = getModelArenaLocation()
        val size = WorldEditUtils.readSchematicSize(this)
        WorldEditUtils.clear(start, start.add(size))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArenaSchematic) return false
        return other.name == name
    }

    override fun hashCode(): Int {
        return Objects.hashCode(name)
    }

    override fun toString(): String {
        return "ArenaSchematic(name=$name, gridIndex=$gridIndex)"
    }

    val isEnabledSafe: Boolean
        get() = enabled
}