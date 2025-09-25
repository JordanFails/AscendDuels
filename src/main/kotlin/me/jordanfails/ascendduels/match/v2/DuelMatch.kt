package me.jordanfails.ascendduels.match.v2

import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.arena.Arena
import me.jordanfails.ascendduels.kit.Kit
import me.jordanfails.ascendduels.match.MatchStatistics
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.pvpwars.core.util.StringUtil
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import java.util.*
import kotlin.math.round

/**
 * Modern, clean match implementation focusing on simplicity and reliability
 */
class DuelMatch(
    val id: UUID = UUID.randomUUID(),
    val kit: Kit,
    val arena: Arena,
    val player1: Player,
    val player2: Player,
    val isRiskMatch: Boolean = false
) {
    
    private var _state: MatchState = MatchState.CREATED
    val state: MatchState get() = _state
    
    private val statistics = mutableMapOf<UUID, MatchStatistics>()
    private var winner: Player? = null
    private var loser: Player? = null
    
    private var startCountdown = 5
    private val manager get() = AscendDuels.instance.matchManagerV2
    
    /**
     * All players in this match
     */
    val players: List<Player> = listOf(player1, player2)
    
    /**
     * Get the opponent of a given player
     */
    fun getOpponent(player: Player): Player? {
        return when (player) {
            player1 -> player2
            player2 -> player1
            else -> null
        }
    }
    
    /**
     * Check if a player is in this match
     */
    fun hasPlayer(player: Player): Boolean = player in players
    
    /**
     * Get or create statistics for a player
     */
    fun getStatistics(player: Player): MatchStatistics {
        return statistics.computeIfAbsent(player.uniqueId) { MatchStatistics() }
    }
    
    /**
     * Transition to a new state (with validation)
     */
    fun transitionTo(newState: MatchState): Boolean {
        if (!_state.canTransitionTo(newState)) {
            AscendDuels.instance.logger.warning("Invalid state transition: ${_state} -> $newState for match $id")
            return false
        }
        
        val oldState = _state
        _state = newState
        
        // Handle state entry logic
        when (newState) {
            MatchState.PREPARING -> onEnterPreparing()
            MatchState.STARTING -> onEnterStarting()
            MatchState.ACTIVE -> onEnterActive()
            MatchState.ENDING -> onEnterEnding()
            MatchState.FINISHED -> onEnterFinished()
            MatchState.CREATED -> { /* Initial state */ }
        }
        
        manager.onMatchStateChanged(this, oldState, newState)
        return true
    }
    
    /**
     * Start the match preparation
     */
    fun start() {
        if (!transitionTo(MatchState.PREPARING)) return
        
        // Validate kit has items
        if (!validateKit()) {
            players.forEach { player ->
                player.sendMessage("§cMatch cancelled: Kit '${kit.displayName}' has no items!")
            }
            transitionTo(MatchState.FINISHED)
            return
        }
        
        // Save player states
        players.forEach { player ->
            AscendDuels.instance.playerInventoryManager.savePlayerState(player)
        }
        
        // Prepare players (teleport, clear inventory, etc.)
        preparePlayer(player1, 0)
        preparePlayer(player2, 1)
        
        // Move to starting phase
        transitionTo(MatchState.STARTING)
    }
    
    /**
     * Handle player death
     */
    fun onPlayerDeath(deadPlayer: Player) {
        if (!state.canTakeDamage()) return
        
        winner = getOpponent(deadPlayer)
        loser = deadPlayer
        
        // Handle dead player's inventory immediately
        AscendDuels.instance.playerInventoryManager.handleMatchEnd(deadPlayer, isRiskMatch, false)
        
        // Put dead player in spectator mode
        deadPlayer.gameMode = GameMode.SPECTATOR
        deadPlayer.sendMessage("§7You are now spectating for 5 seconds...")
        
        // Transition to ending state
        transitionTo(MatchState.ENDING)
    }
    
    /**
     * Force end the match (for disconnects, commands, etc.)
     */
    fun forceEnd() {
        transitionTo(MatchState.FINISHED)
    }
    
    /**
     * Check if a player can hurt another player
     */
    fun canHurt(attacker: Player, target: Player): Boolean {
        return state.canTakeDamage() && 
               hasPlayer(attacker) && 
               hasPlayer(target) && 
               attacker != target
    }
    
    // State entry handlers
    private fun onEnterPreparing() {
        sendMessage("§ePreparing duel arena...")
    }
    
    private fun onEnterStarting() {
        startCountdown = 5
        manager.startCountdown(this)
    }
    
    private fun onEnterActive() {
        sendMessage("§a§lFIGHT!")
        sendTitle("§a§lFIGHT!", "")
        playSound(org.bukkit.Sound.NOTE_PLING, 2.0f, 2.0f)
    }
    
    private fun onEnterEnding() {
        val winnerName = winner?.displayName ?: "Unknown"
        val loserName = loser?.displayName ?: "Unknown"
        
        sendMessage("§c$loserName §fhas been defeated by §a$winnerName§f!")
        
        // Schedule final cleanup after spectator period
        manager.scheduleMatchEnd(this, 5) // 5 seconds
    }
    
    private fun onEnterFinished() {
        // Cache inventories for /duelinventory command
        players.forEach { player ->
            if (player.isOnline) {
                val stats = getStatistics(player)
                // Update final stats before caching
                stats.health = player.health.toFloat()
                stats.hunger = player.foodLevel.toFloat()
                stats.effects = player.activePotionEffects.toMutableList()
                AscendDuels.instance.matchInventoryService.cacheInventory(player, stats)
            }
        }
        
        // Handle winner's inventory
        winner?.let { winnerPlayer ->
            AscendDuels.instance.playerInventoryManager.handleMatchEnd(winnerPlayer, isRiskMatch, true)
        }
        
        // Display post-match statistics
        displayPostMatchStats()
        
        // Clean up and teleport players back
        players.forEach { player ->
            if (player.isOnline) {
                cleanupPlayer(player)
            }
        }
        
        // Notify manager
        manager.onMatchFinished(this)
    }
    
    // Countdown tick (called by manager)
    fun tickCountdown() {
        if (state != MatchState.STARTING) return
        
        if (startCountdown > 0) {
            sendMessage("§eMatch starts in §a$startCountdown §esecond${if (startCountdown == 1) "" else "s"}...")
            sendTitle("§e§lStarting", "§a$startCountdown")
            playSound(org.bukkit.Sound.NOTE_PLING, 1.0f, 1.0f)
            startCountdown--
        } else {
            transitionTo(MatchState.ACTIVE)
        }
    }
    
    private fun preparePlayer(player: Player, spawnIndex: Int) {
        val spawnPoint = arena.getSpawnLocation(spawnIndex)
        
        // Clear effects
        player.activePotionEffects.forEach { effect: PotionEffect ->
            player.removePotionEffect(effect.type)
        }
        
        // Reset player state
        player.health = player.maxHealth
        player.foodLevel = 20
        player.saturation = 10f
        player.fireTicks = 0
        player.fallDistance = 0f
        player.gameMode = GameMode.SURVIVAL
        player.allowFlight = false
        player.isFlying = false
        
        // Teleport
        player.teleport(spawnPoint)
        
        // Handle inventory
        AscendDuels.instance.playerInventoryManager.prepareDuelInventory(player, isRiskMatch)
        kit.inventory?.load(player)
        
        // Initialize statistics
        val stats = getStatistics(player)
        stats.health = player.maxHealth.toFloat()
        stats.hunger = player.foodLevel.toFloat()
        stats.effects = player.activePotionEffects.toMutableList()
    }
    
    private fun cleanupPlayer(player: Player) {
        // Clear effects
        player.activePotionEffects.forEach { effect ->
            player.removePotionEffect(effect.type)
        }
        
        // Reset state
        player.health = player.maxHealth
        player.foodLevel = 20
        player.saturation = 5f
        player.gameMode = GameMode.SURVIVAL
        player.allowFlight = false
        player.isFlying = false
        player.fireTicks = 0
        player.fallDistance = 0f
        
        // Teleport back to spawn
        player.teleport(net.pvpwars.core.Core.getInstance().locationFile.spawn)
    }
    
    /**
     * Validates that the kit has at least one item (either in contents or armor)
     */
    private fun validateKit(): Boolean {
        val kitInventory = kit.inventory ?: return false
        
        // Check if kit has any items in contents
        val hasContentItems = kitInventory.contents.any { it != null }
        
        // Check if kit has any armor items
        val hasArmorItems = kitInventory.armorContents.any { it != null }
        
        return hasContentItems || hasArmorItems
    }
    
    /**
     * Displays post-match statistics to both players with enhanced hover/click functionality
     */
    private fun displayPostMatchStats() {
        val winnerName = winner?.displayName ?: "Unknown"
        val loserName = loser?.displayName ?: "Unknown"
        val winnerColor = if (winner == player1) ChatColor.GREEN else ChatColor.RED
        val loserColor = if (loser == player1) ChatColor.RED else ChatColor.GREEN
        
        // Create header
        val header = "§8§m----------------------------------------"
        
        // Send to both players
        players.forEach { player ->
            if (player.isOnline) {
                player.sendMessage("")
                player.sendMessage("§c§lDUEL STATISTICS")
                player.sendMessage("§fKit: §e${kit.displayName} §7| §fArena: §e${arena.schematic ?: "Unknown"}")
                player.sendMessage("")
                
                // Winner result
                val winnerComponents = getResultMessage(winner ?: player1, winnerColor)
                player.spigot().sendMessage(*winnerComponents)
                
                // Loser result  
                val loserComponents = getResultMessage(loser ?: player2, loserColor)
                player.spigot().sendMessage(*loserComponents)
                
                player.sendMessage("")
                player.sendMessage("§7§oHover over names for detailed stats or click to view inventories.")
                player.sendMessage("§8§m----------------------------------------")
            }
        }
    }
    
    /**
     * Creates enhanced result message with hover and click events
     */
    private fun getResultMessage(player: Player, color: ChatColor): Array<BaseComponent> {
        val componentBuilder = ComponentBuilder(StringUtil.repeat(" ", StringUtil.getCenterSpaceCount(player.name)))
        componentBuilder.append(player.name).retain(ComponentBuilder.FormatRetention.NONE).color(color)
        
        val statistics = getStatistics(player)
        var healLine = ""
        
        when (statistics.healType) {
            MatchStatistics.HealType.POT -> {
                healLine = "&r &8• &bPotions (remaining | missed): &f${statistics.healsRemaining} | ${statistics.healsMissed}\n"
            }
            MatchStatistics.HealType.SOUP -> {
                healLine = "&r &8• &bSoups (remaining | used): &f${statistics.healsRemaining} | ${statistics.healsUsed}\n"
            }
            MatchStatistics.HealType.NONE -> {
                // No heal line
            }
        }
        
        val hoverComponent = TextComponent.fromLegacyText(
            StringUtil.color(
                "${color}Player Rundown:&r\n" +
                "\n" +
                "&r &8• &bHealth: &f${round(statistics.health * 2.0f) / 2.0f}&c♥\n" +
                "&r &8• &bHits (all | longest combo): &f${statistics.hitsLanded} | ${statistics.longestCombo}\n" +
                healLine +
                "\n" +
                "&r&7&oClick to expand match statistics."
            )
        )
        
        componentBuilder.event(HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent))
        componentBuilder.event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duelinventory ${player.uniqueId}"))
        
        return componentBuilder.create()
    }
    
    // Utility methods
    private fun sendMessage(message: String) {
        players.forEach { it.sendMessage(message) }
    }
    
    private fun sendTitle(title: String, subtitle: String) {
        players.forEach { player ->
            player.sendTitle(title, subtitle)
        }
    }
    
    private fun playSound(sound: org.bukkit.Sound, volume: Float, pitch: Float) {
        players.forEach { player ->
            player.playSound(player.location, sound, volume, pitch)
        }
    }
    
    override fun toString(): String {
        return "DuelMatch(id=$id, state=$state, players=[${player1.name}, ${player2.name}], risk=$isRiskMatch)"
    }
}
