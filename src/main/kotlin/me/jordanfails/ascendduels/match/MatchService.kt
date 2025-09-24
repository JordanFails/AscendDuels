package me.jordanfails.ascendduels.match

import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.api.service.Service
import net.pvpwars.core.util.runnable.RunnableBuilder
import me.jordanfails.ascendduels.arena.Arena
import me.jordanfails.ascendduels.kit.Kit
import me.jordanfails.ascendduels.match.impl.match.player.PlayerMatch
import me.jordanfails.ascendduels.match.impl.match.player.RiskMatch
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MatchService : Service {

    private val matches: MutableSet<Match<*, *>> = ConcurrentHashMap.newKeySet()
    private val byUuid: MutableMap<UUID, Match<*, *>> = ConcurrentHashMap()

    fun <T : Match<*, *>> registerMatch(match: T): T {
        matches.add(match)
        match.getParticipants().forEach { participant ->
            byUuid[participant.uuid] = match
        }
        return match
    }

    fun <T : Match<*, *>> unregisterMatch(match: T) {
        matches.remove(match)
        match.getParticipants().forEach { participant ->
            byUuid.remove(participant.uuid)
        }
    }

    fun getMatches(): Set<Match<*, *>> = matches

    fun getByPlayer(player: Player): Match<*, *>? = byUuid[player.uniqueId]

    fun getByUuid(uuid: UUID): Match<*, *>? =
        byUuid[uuid]

    fun newPlayerMatch(kit: Kit?, arena: Arena?, playerOne: Player, playerTwo: Player): PlayerMatch? {
        val requirements = findRequirements(kit, arena) ?: return null
        return registerMatch(
            PlayerMatch(
                requirements.component1(),
                requirements.component2(),
                playerOne,
                playerTwo
            )
        )
    }

    fun newRiskMatch(kit: Kit?, arena: Arena?, playerOne: Player, playerTwo: Player): RiskMatch? {
        val requirements = findRequirements(kit, arena) ?: return null
        return registerMatch(
            RiskMatch(
                requirements.component1(),
                requirements.component2(),
                playerOne,
                playerTwo
            )
        )
    }

    fun findRequirements(kit: Kit?): Pair<Kit, Arena>? =
        findRequirements(kit, null)

    fun findRequirements(kitParam: Kit?, arenaParam: Arena?): Pair<Kit, Arena>? {
        var kit = kitParam
        var arena = arenaParam

        if (kit == null) {
            kit = AscendDuels.instance.kitService.all()
                .firstOrNull { it.isEnabled } ?: Kit.DEFAULT
        }

        if (arena == null) {
            arena = AscendDuels.instance.arenaHandler.allocateUnusedArena { true }.orElse(null) ?: return null
        }

        return Pair(kit, arena)
    }

    override fun load() {
        RunnableBuilder.forPlugin(AscendDuels.instance)
            .with {
                for (match in matches) {
                    match.tick()
                }
            }
            .runSyncTimer(100L, 20L)
    }

    override fun unload() {
        matches.forEach { match -> match.end(true) }
    }
}