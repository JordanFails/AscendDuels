package me.jordanfails.ascendduels.spy

import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.api.event.match.impl.MatchStartEvent
import me.jordanfails.ascendduels.api.service.Service
import me.jordanfails.ascendduels.match.Match
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

class DuelSpyService : Service, Listener {
    private val spyingSet: MutableSet<UUID?> = Collections.newSetFromMap(ConcurrentHashMap<UUID, Boolean>())

    init {
        val plugin: JavaPlugin = AscendDuels.instance
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    fun toggleSpying(player: Player): Boolean {
        if (spyingSet.add(player.uniqueId)) {
            return true
        } else if (spyingSet.remove(player.uniqueId)) {
            return false
        }
        return false
    }

    fun isSpying(player: Player): Boolean {
        return spyingSet.contains(player.uniqueId)
    }

    @EventHandler
    fun onMatchStart(event: MatchStartEvent) {
        val match: Match<*, *> = event.match

        val message: String = AscendDuels.prefix(
            "A duel has started between " + match.getPlayers().stream()
                .map(HumanEntity::getName)
                .collect(Collectors.joining(", "))
        )

        for (player in Bukkit.getOnlinePlayers()) {
            if (this.isSpying(player)) {
                player.sendMessage("")
                player.sendMessage(message)
                player.sendMessage("")
            }
        }
    }

    override fun load() {
    }

    override fun unload() {
    }
}