package me.jordanfails.ascendduels.arena

import co.aikar.commands.BukkitCommandCompletionContext
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.api.service.Service
import net.pvpwars.core.Core
import org.apache.commons.io.FileUtils
import org.bukkit.World
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors


class ArenaService : Service {

    private val arenaList: MutableList<Arena> = mutableListOf()
    private val byName: MutableMap<String, Arena> = ConcurrentHashMap()
    val arenaWorlds: MutableSet<String> = HashSet()

    fun all(): List<Arena> = Collections.unmodifiableList(arenaList)

    fun get(name: String): Arena? = byName[name]

    fun add(arena: Arena) {
        arenaList.add(arena)
        byName[arena.name as String] = arena
    }

    fun remove(name: String) {
        val arena = byName.remove(name)
        if (arena != null) {
            arenaList.remove(arena)
        }
    }

    fun getRandom(): Arena? = getRandom(ArenaTag.NORMAL)

    fun getRandom(arenaTag: ArenaTag?): Arena? =
        all().firstOrNull { arena ->
            arena.isComplete && (arenaTag == null || arena.hasTag(arenaTag))
        }

    fun registerWorld(world: World) {
        arenaWorlds.add(world.name)
    }

    fun isWorld(world: World): Boolean = arenaWorlds.contains(world.name)

    fun saveAll() {
        val dataFolder = AscendDuels.instance.dataFolder
        if (!dataFolder.exists()) dataFolder.mkdirs()

        val file = File(dataFolder, "arenas.json")
        try {
            OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8).use { writer ->
                for (arena in all()) {
                    if (arena.isComplete) {
                        writer.write(arena.serializeToString() + "\n")
                    }
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun loadAll() {
        val file = File(AscendDuels.instance.dataFolder, "arenas.json")
        if (file.exists()) {
            try {
                val lines = FileUtils.readLines(file, StandardCharsets.UTF_8)
                for (line in lines) {
                    val arena = Arena()
                    arena.deserialize(line)
                    add(arena)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    override fun load() {
        val commandManager: PaperCommandManager = Core.getInstance().commandManager.commandManager

        // Argument context resolver
        commandManager.commandContexts.registerContext(Arena::class.java) { context ->
            val arg = context.popFirstArg()
            val arena = get(arg)
            if (arena != null) return@registerContext arena
            throw InvalidCommandArgument(AscendDuels.prefix("&cThe arena '{0}' does not exist.", arg))
        }

        commandManager.commandCompletions.registerAsyncCompletion(
            "arenaTags"
        ) { _: BukkitCommandCompletionContext? ->
            Arrays.stream<ArenaTag>(
                ArenaTag.entries.toTypedArray()
            ).map<String?> { obj: ArenaTag? -> obj!!.name }.collect(Collectors.toList())
        }

        loadAll()
    }

    override fun unload() {
        TODO("Not yet implemented")
    }
}

// Tab completions