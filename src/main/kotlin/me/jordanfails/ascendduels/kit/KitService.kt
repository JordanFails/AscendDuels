package me.jordanfails.ascendduels.kit

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.api.service.Service
import me.jordanfails.ascendduels.kit.storage.FlatfileKitStorage
import me.jordanfails.ascendduels.kit.storage.KitStorage
import net.pvpwars.core.Core
import java.util.*

class KitService : Service {

    private val kits: MutableMap<String, Kit> = HashMap()
    private val storage: KitStorage = FlatfileKitStorage(this)

    fun get(name: String): Kit? = kits[name.lowercase()]

    fun add(kit: Kit) {
        kit.name?.let {
            kits[it.lowercase()] = kit
        }
    }

    fun remove(name: String) {
        kits.remove(name.lowercase())
    }

    fun all(): Collection<Kit> = Collections.unmodifiableCollection(kits.values)

    fun getMap(): Map<String, Kit> = Collections.unmodifiableMap(kits)

    fun loadKits() = storage.loadKits()
    fun saveKits() = storage.saveKits()

    override fun load() {
        val commandManager: PaperCommandManager = Core.getInstance()
            .commandManager
            .commandManager

        // Context resolver for Kit arguments
        commandManager.commandContexts.registerContext(Kit::class.java) { context ->
            val arg = context.popFirstArg()
            val kit = get(arg)
            if (kit != null) return@registerContext kit
            throw InvalidCommandArgument(
                AscendDuels.prefix("&cThe kit '{0}' does not exist.", arg)
            )
        }

        // Tab completion
        commandManager.commandCompletions.registerAsyncCompletion("duelKits") {
            Collections.unmodifiableCollection(kits.keys)
        }

        loadKits()
    }

    override fun unload() {
        // saveKits() // Uncomment if persistence on unload is desired
    }
}