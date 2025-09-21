package me.jordanfails.ascendduels

import co.aikar.commands.BaseCommand
import co.aikar.commands.PaperCommandManager
import me.jordanfails.ascendduels.api.service.Service
import me.jordanfails.ascendduels.arena.ArenaService
import me.jordanfails.ascendduels.command.*
import me.jordanfails.ascendduels.inventory.MatchInventoryService
import me.jordanfails.ascendduels.inventory.RiskPostMatchInventoryService
import me.jordanfails.ascendduels.utils.KillcamManager
import me.jordanfails.ascendduels.kit.KitService
import me.jordanfails.ascendduels.listener.PlayerListener
import me.jordanfails.ascendduels.listener.RiskMatchConfirmListener
import me.jordanfails.ascendduels.match.MatchService
import me.jordanfails.ascendduels.request.RequestService
import me.jordanfails.ascendduels.spy.DuelSpyService
import net.pvpwars.core.Core
import net.pvpwars.core.util.StringUtil
import net.pvpwars.duels.listeners.TeleportListener
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.function.Consumer


class AscendDuels : JavaPlugin() {

    companion object {
        lateinit var instance: AscendDuels
        const val prefix: String = "&6&lDuels&r &8➼ &f"

        fun prefix(message: String?, vararg arguments: Any?): String {
            return prefix + StringUtil.colorFormat(message, arguments)
        }
    }

    private val commandList: MutableList<BaseCommand?> = ArrayList<BaseCommand?>()
    var arenaService: ArenaService = ArenaService()
    var kitService: KitService = KitService()
    var matchService: MatchService = MatchService()
    var matchInventoryService: MatchInventoryService = MatchInventoryService()
    var riskPostMatchInventoryService: RiskPostMatchInventoryService = RiskPostMatchInventoryService()
    var requestService: RequestService = RequestService()
    var duelSpyService: DuelSpyService = DuelSpyService()
    var services: MutableList<Service> = mutableListOf()

    override fun onEnable() {
        instance = this
        services = mutableListOf(
            arenaService,
            kitService,
            matchService,
            matchInventoryService,
            riskPostMatchInventoryService,
            requestService,
            duelSpyService
        )

        services.forEach(Service::load)
        registerCommands()
        registerListeners()
    }

    override fun onDisable() {
        val playersToRestoreInv: MutableList<Player?> = ArrayList<Player?>()
        for (worldName in arenaService.arenaWorlds) {
            val world: World? = Bukkit.getWorld(worldName)
            if (world != null) {
                playersToRestoreInv.addAll(world.players)
            }
        }

        services.reverse()
        services.forEach(Service::unload)

        for (player in Bukkit.getOnlinePlayers()) {
            player.removeMetadata("riskMatchConfirming", this)
        }

        unregisterCommands()    }

    private fun registerCommands() {
        val commandManager: PaperCommandManager = Core.getInstance().commandManager.commandManager

        mutableListOf<BaseCommand?>(
            ArenaCommand(),
            DuelCommand(),
            DuelInventoryCommand(),
            DuelKitCommand(),
            DuelAdminCommand(),
            RiskMatchLootCommand(),
            DuelSpyCommand()
        ).forEach(Consumer { command: BaseCommand? ->
            commandManager.registerCommand(command, true)
            commandList.add(command)
        })
    }

    private fun unregisterCommands() {
        val commandManager: PaperCommandManager = Core.getInstance().commandManager.commandManager
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