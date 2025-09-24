package me.jordanfails.ascendduels

import co.aikar.commands.BaseCommand
import co.aikar.commands.PaperCommandManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sk89q.worldedit.BlockVector
import me.jordanfails.ascendduels.api.service.Service
import me.jordanfails.ascendduels.arena.ArenaHandler
import me.jordanfails.ascendduels.command.*
import me.jordanfails.ascendduels.inventory.MatchInventoryService
import me.jordanfails.ascendduels.inventory.PlayerInventoryService
import me.jordanfails.ascendduels.inventory.RiskPostMatchInventoryService
import me.jordanfails.ascendduels.kit.KitService
import me.jordanfails.ascendduels.listener.PlayerListener
import me.jordanfails.ascendduels.listener.RiskMatchConfirmListener
import me.jordanfails.ascendduels.listener.TeleportListener
import me.jordanfails.ascendduels.match.MatchService
import me.jordanfails.ascendduels.request.RequestService
import me.jordanfails.ascendduels.spy.DuelSpyService
import me.jordanfails.ascendduels.utils.KillcamManager
import me.jordanfails.ascendduels.utils.WorldUtil
import me.jordanfails.ascendduels.utils.serialization.VectorAdapter
import net.pvpwars.core.util.CC
import net.pvpwars.core.util.StringUtil
import net.pvpwars.core.util.serialization.BlockVectorAdapter
import net.pvpwars.core.util.serialization.ItemStackAdapter
import net.pvpwars.core.util.serialization.LocationAdapter
import net.pvpwars.core.util.serialization.PotionEffectAdapter
import org.bukkit.Bukkit
import org.bukkit.ChunkSnapshot
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import java.util.*
import java.util.function.Consumer


class AscendDuels : JavaPlugin() {

    companion object {
        lateinit var instance: AscendDuels
        const val prefix: String = "&6&lDuels&r &8➼ &f"

        fun prefix(message: String?, vararg arguments: Any?): String {
            return CC.translate(prefix + StringUtil.colorFormat(message, arguments))
        }
    }

    private val commandList: MutableList<BaseCommand?> = ArrayList<BaseCommand?>()
    lateinit var kitService: KitService
    lateinit var arenaHandler: ArenaHandler
    lateinit var matchService: MatchService
    lateinit var matchInventoryService: MatchInventoryService
    lateinit var playerInventoryService: PlayerInventoryService
    lateinit var riskPostMatchInventoryService: RiskPostMatchInventoryService
    lateinit var requestService: RequestService
    lateinit var duelSpyService: DuelSpyService
    lateinit var commandHandler: PaperCommandManager
    val gson: Gson = GsonBuilder()
        .registerTypeHierarchyAdapter(PotionEffect::class.java, PotionEffectAdapter())
        .registerTypeHierarchyAdapter(ItemStack::class.java, ItemStackAdapter())
        .registerTypeHierarchyAdapter(Location::class.java, LocationAdapter())
        .registerTypeHierarchyAdapter(Vector::class.java, VectorAdapter())
        .registerTypeAdapter(BlockVector::class.java, BlockVectorAdapter())
        .serializeNulls()
        .create()
    var services: MutableList<Service> = mutableListOf()


    override fun onEnable() {
        instance = this
        
        // Ensure duels world exists before loading services
        try {
            WorldUtil.ensureDuelsWorld()
        } catch (e: Exception) {
            logger.severe("Failed to create duels world: ${e.message}")
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
            return
        }
        
        kitService = KitService()
        arenaHandler = ArenaHandler()
        matchService = MatchService()
        matchInventoryService = MatchInventoryService()
        playerInventoryService = PlayerInventoryService()
        riskPostMatchInventoryService = RiskPostMatchInventoryService()
        requestService = RequestService()
        duelSpyService = DuelSpyService()
        services = mutableListOf(
            kitService,
            matchService,
            matchInventoryService,
            playerInventoryService,
            riskPostMatchInventoryService,
            requestService,
            duelSpyService
        )
        commandHandler = PaperCommandManager(this)

        services.forEach(Service::load)
        registerCommands()
        registerListeners()
    }

    override fun onDisable() {
        services.reverse()
        services.forEach(Service::unload)

        for (player in Bukkit.getOnlinePlayers()) {
            player.removeMetadata("riskMatchConfirming", this)
        }

        unregisterCommands()    }

    private fun registerCommands() {
        val commandManager: PaperCommandManager = instance.commandHandler

        mutableListOf<BaseCommand?>(
            ArenaCommand(),
            DuelCommand(),
            DuelInventoryCommand(),
            DuelKitCommand(),
            DuelAdminCommand(),
            RiskMatchLootCommand(),
            DuelSpyCommand(),
            TPPosCommand()
        ).forEach(Consumer { command: BaseCommand? ->
            commandManager.registerCommand(command, true)
            commandList.add(command)
        })
    }

    private fun unregisterCommands() {
        val commandManager: PaperCommandManager = instance.commandHandler
        commandList.forEach(commandManager::unregisterCommand)
    }

    private fun registerListeners() {
        val pluginManager: PluginManager = server.pluginManager
        pluginManager.registerEvents(PlayerListener(), this)
        pluginManager.registerEvents(TeleportListener(), this)
        pluginManager.registerEvents(RiskMatchConfirmListener(), this)
        pluginManager.registerEvents(KillcamManager, this)
    }
}