package me.jordanfails.ascendduels.match.v2

import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.api.service.Service
import me.jordanfails.ascendduels.arena.Arena
import me.jordanfails.ascendduels.kit.Kit
import net.pvpwars.core.util.runnable.RunnableBuilder
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Centralized match manager that handles all match lifecycle operations
 */
class MatchManagerV2 : Service {
    
    private val matches = ConcurrentHashMap<UUID, DuelMatch>()
    private val playerToMatch = ConcurrentHashMap<UUID, UUID>()
    private val countdownMatches = mutableSetOf<UUID>()
    
    /**
     * Create a new regular duel match
     */
    fun createMatch(kit: Kit, arena: Arena, player1: Player, player2: Player): DuelMatch? {
        if (isInMatch(player1) || isInMatch(player2)) {
            return null // One of the players is already in a match
        }
        
        val match = DuelMatch(
            kit = kit,
            arena = arena,
            player1 = player1,
            player2 = player2,
            isRiskMatch = false
        )
        
        return registerMatch(match)
    }
    
    /**
     * Create a new risk duel match
     */
    fun createRiskMatch(kit: Kit, arena: Arena, player1: Player, player2: Player): DuelMatch? {
        if (isInMatch(player1) || isInMatch(player2)) {
            return null // One of the players is already in a match
        }
        
        val match = DuelMatch(
            kit = kit,
            arena = arena,
            player1 = player1,
            player2 = player2,
            isRiskMatch = true
        )
        
        return registerMatch(match)
    }
    
    /**
     * Get the match a player is in
     */
    fun getMatch(player: Player): DuelMatch? {
        val matchId = playerToMatch[player.uniqueId] ?: return null
        return matches[matchId]
    }
    
    /**
     * Check if a player is in a match
     */
    fun isInMatch(player: Player): Boolean = playerToMatch.containsKey(player.uniqueId)
    
    /**
     * Handle player death event
     */
    fun onPlayerDeath(player: Player) {
        val match = getMatch(player) ?: return
        match.onPlayerDeath(player)
    }
    
    /**
     * Handle player disconnect
     */
    fun onPlayerDisconnect(player: Player) {
        val match = getMatch(player) ?: return
        
        // In an active match, disconnecting = losing
        if (match.state.isInProgress()) {
            match.onPlayerDeath(player) // Treat disconnect as death
        }
    }
    
    /**
     * Force end a match (admin command, etc.)
     */
    fun forceEndMatch(matchId: UUID) {
        val match = matches[matchId] ?: return
        match.forceEnd()
    }
    
    /**
     * Get all active matches
     */
    fun getActiveMatches(): Collection<DuelMatch> = matches.values
    
    /**
     * Get match statistics
     */
    fun getStats(): String {
        val total = matches.size
        val byState = matches.values.groupingBy { it.state }.eachCount()
        return "Matches: $total total, $byState"
    }
    
    // Internal methods
    private fun registerMatch(match: DuelMatch): DuelMatch {
        matches[match.id] = match
        match.players.forEach { player ->
            playerToMatch[player.uniqueId] = match.id
        }
        
        AscendDuels.instance.logger.info("Registered match: $match")
        return match
    }
    
    private fun unregisterMatch(match: DuelMatch) {
        matches.remove(match.id)
        match.players.forEach { player ->
            playerToMatch.remove(player.uniqueId)
        }
        countdownMatches.remove(match.id)
        
        // Release arena
        match.arena.inUse = false
        
        AscendDuels.instance.logger.info("Unregistered match: $match")
    }
    
    // Called by matches
    internal fun onMatchStateChanged(match: DuelMatch, oldState: MatchState, newState: MatchState) {
        AscendDuels.instance.logger.info("Match ${match.id} state: $oldState -> $newState")
        
        // Handle special state transitions
        when (newState) {
            MatchState.PREPARING -> {
                match.arena.inUse = true
            }
            MatchState.FINISHED -> {
                // Will be unregistered in onMatchFinished
            }
            else -> { /* No special handling */ }
        }
    }
    
    internal fun startCountdown(match: DuelMatch) {
        countdownMatches.add(match.id)
    }
    
    internal fun scheduleMatchEnd(match: DuelMatch, delaySeconds: Int) {
        RunnableBuilder.forPlugin(AscendDuels.instance)
            .with { 
                match.transitionTo(MatchState.FINISHED)
            }
            .runSyncLater(delaySeconds * 20L)
    }
    
    internal fun onMatchFinished(match: DuelMatch) {
        unregisterMatch(match)
    }
    
    // Service implementation
    override fun load() {
        // Start the match ticker
        RunnableBuilder.forPlugin(AscendDuels.instance)
            .with { tick() }
            .runSyncTimer(0L, 20L) // Every second
            
        AscendDuels.instance.logger.info("MatchManagerV2 loaded")
    }
    
    override fun unload() {
        // Force end all matches
        matches.values.forEach { match ->
            match.forceEnd()
        }
        
        // Clear all data
        matches.clear()
        playerToMatch.clear()
        countdownMatches.clear()
        
        AscendDuels.instance.logger.info("MatchManagerV2 unloaded")
    }
    
    private fun tick() {
        // Tick countdown for all matches in STARTING state
        countdownMatches.toList().forEach { matchId ->
            val match = matches[matchId]
            if (match?.state == MatchState.STARTING) {
                match.tickCountdown()
            } else {
                countdownMatches.remove(matchId)
            }
        }
    }
}
