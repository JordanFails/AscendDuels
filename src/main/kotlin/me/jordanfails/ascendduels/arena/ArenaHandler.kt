package me.jordanfails.ascendduels.arena

import com.google.common.base.Charsets
import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableSet
import com.google.common.io.Files
import com.google.gson.reflect.TypeToken
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.arena.event.ArenaAllocatedEvent
import me.jordanfails.ascendduels.arena.event.ArenaReleasedEvent
import me.jordanfails.ascendduels.arena.listener.ArenaItemResetListener
import org.apache.logging.log4j.LogManager
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.asKotlinRandom


/**
 * Manages arena schematics & arena instances.
 *
 * - Maintains schematics
 * - Allocates/Deallocates arenas
 * - Saves and loads arenas/schematics
 */
class ArenaHandler {

    companion object {
        val WORLD_EDIT_SCHEMATICS_FOLDER: File =
            File(JavaPlugin.getPlugin(WorldEditPlugin::class.java).dataFolder, "schematics")

        private const val ARENA_INSTANCES_FILE_NAME = "arenaInstances.json"
        private const val SCHEMATICS_FILE_NAME = "schematics.json"
    }

    // schematic -> (copy id -> Arena instance)
    private val arenaInstances: MutableMap<String, MutableMap<Int, Arena>> = HashMap()

    // schematic name -> schematic
    val schematics: MutableMap<String, ArenaSchematic> = TreeMap()

    val grid = ArenaGrid()

    init {
        // register listener
        Bukkit.getPluginManager()
            .registerEvents(ArenaItemResetListener(), AscendDuels.instance)

        val folder = AscendDuels.instance.dataFolder
        val arenaInstancesFile = File(folder, ARENA_INSTANCES_FILE_NAME)
        val schematicsFile = File(folder, SCHEMATICS_FILE_NAME)

        try {
            // Load Arena instances
            if (arenaInstancesFile.exists()) {
                arenaInstancesFile.bufferedReader(Charsets.UTF_8).use { reader ->
                    val arenaListType: Type = object : TypeToken<List<Arena>>() {}.type
                    val arenaList: List<Arena> =
                        AscendDuels.instance.gson.fromJson(reader, arenaListType) ?: emptyList()

                    for (arena in arenaList) {
                        val copies =
                            arenaInstances.computeIfAbsent(arena.schematic!!) { HashMap() }
                        copies[arena.copy] = arena
                    }
                }
            }

            // Load Schematics
            if (schematicsFile.exists()) {
                schematicsFile.bufferedReader(Charsets.UTF_8).use { reader ->
                    val schematicListType: Type = object : TypeToken<List<ArenaSchematic>>() {}.type
                    val schematicList: List<ArenaSchematic> =
                        AscendDuels.instance.gson.fromJson(reader, schematicListType) ?: emptyList()

                    for (schematic in schematicList) {
                        schematics[schematic.name ?: continue] = schematic
                    }
                }
            }
        } catch (ex: IOException) {
            throw RuntimeException(ex) // fail fast
        }

        cacheChunks()
    }

    fun isWorld(world: World): Boolean {
        return world == getArenaWorld()
    }

    /** Pre-load chunks synchronously */
    fun cacheChunks() {
        val start = System.currentTimeMillis()
        LogManager.getLogger().info("[Practice] Loading chunks...")

        for (schematic in schematics.values) {
            if (!schematic.isEnabledSafe) continue

            for (arena in getArenas(schematic)) {
                arena.bounds!!.getChunks().forEach { chunk ->
                    if (!chunk.isLoaded) chunk.load()
                }
            }
        }

        val seconds = this.millisToSeconds(System.currentTimeMillis() - start)
        LogManager.getLogger().info("[Practice] Chunks loaded successfully in ${seconds}s!")
    }

    fun saveSchematics() {
        Files.write(
            AscendDuels.instance.gson.toJson(schematics.values),
            File(AscendDuels.instance.dataFolder, SCHEMATICS_FILE_NAME),
            Charsets.UTF_8
        )
    }

    @Throws(IOException::class)
    fun saveArenas() {
        val allArenas = mutableListOf<Arena>()

        arenaInstances.forEach { (_, copies) ->
            allArenas.addAll(copies.values)
        }

        Files.write(
            AscendDuels.instance.gson.toJson(allArenas),
            File(AscendDuels.instance.dataFolder, ARENA_INSTANCES_FILE_NAME),
            Charsets.UTF_8
        )
    }

    fun getArenaWorld(): World = Bukkit.getWorld("arenas")

    fun registerSchematic(schematic: ArenaSchematic) {
        var lastGridIndex = 0
        for (other in schematics.values) {
            lastGridIndex = maxOf(lastGridIndex, other.gridIndex)
        }
        schematic.gridIndex = lastGridIndex + 1
        schematics[schematic.name!!] = schematic
    }

    fun unregisterSchematic(schematic: ArenaSchematic) {
        schematics.remove(schematic.name)
    }

    internal fun registerArena(arena: Arena) {
        val copies = arenaInstances.computeIfAbsent(arena.schematic!!) { HashMap() }
        copies[arena.copy] = arena
    }

    internal fun unregisterArena(arena: Arena) {
        arenaInstances[arena.schematic]?.remove(arena.copy)
    }

    fun getArena(schematic: ArenaSchematic, copy: Int): Arena? =
        arenaInstances[schematic.name]?.get(copy)

    fun getArenas(schematic: ArenaSchematic): Set<Arena> =
        arenaInstances[schematic.name]?.values?.let { ImmutableSet.copyOf(it) } ?: ImmutableSet.of()

    fun countArenas(schematic: ArenaSchematic): Int =
        arenaInstances[schematic.name]?.size ?: 0

    fun getSchematics(): Set<ArenaSchematic> = ImmutableSet.copyOf(schematics.values)

    fun getSchematic(schematicName: String): ArenaSchematic? = schematics[schematicName]

    fun allocateUnusedArena(acceptable: (ArenaSchematic) -> Boolean): Optional<Arena> {
        val acceptableArenas = mutableListOf<Arena>()

        for (schematic in schematics.values) {
            if (!acceptable(schematic)) continue
            val instances = arenaInstances[schematic.name] ?: continue

            for (arena in instances.values) {
                if (!arena.inUse) acceptableArenas.add(arena)
            }
        }

        if (acceptableArenas.isEmpty()) return Optional.empty()

        val selected = acceptableArenas.random(ThreadLocalRandom.current().asKotlinRandom())
        selected.inUse = true
        Bukkit.getPluginManager().callEvent(ArenaAllocatedEvent(selected))

        return Optional.of(selected)
    }

    fun releaseArena(arena: Arena) {
        Preconditions.checkArgument(arena.inUse, "Cannot release arena not in use.")
        arena.inUse = false
        Bukkit.getPluginManager().callEvent(ArenaReleasedEvent(arena))
    }

    fun millisToSeconds(millis: Long): String? {
        return DecimalFormat("#0").format((millis / 1000.0f).toDouble())
    }
}