package me.jordanfails.ascendduels.listener.v2

import me.jordanfails.ascendduels.AscendDuels
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.Material
import org.bukkit.event.block.Action

/**
 * Clean, simple event handler for the new match system
 */
class DuelEventListener : Listener {
    
    private val matchManager get() = AscendDuels.instance.matchManagerV2
    
    /**
     * Handle player death in duels
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val match = matchManager.getMatch(player) ?: return
        
        // Clear drops and exp - no items should drop in duels
        event.drops.clear()
        event.droppedExp = 0
        event.deathMessage = null // We'll send our own message
        
        // Handle the death
        matchManager.onPlayerDeath(player)
    }
    
    /**
     * Prevent damage when not in active state
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        val match = matchManager.getMatch(player) ?: return
        
        // Only allow damage in ACTIVE state
        if (!match.state.canTakeDamage()) {
            event.isCancelled = true
            event.damage = 0.0
        }
    }
    
    /**
     * Prevent PvP between non-opponents and track statistics
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val attacker = event.damager as? Player ?: return
        val target = event.entity as? Player ?: return
        
        val match = matchManager.getMatch(attacker) ?: return
        
        // Make sure both players are in the same match and can hurt each other
        if (!match.canHurt(attacker, target)) {
            event.isCancelled = true
            event.damage = 0.0
            return
        }
        
        // Track statistics
        val attackerStats = match.getStatistics(attacker)
        val targetStats = match.getStatistics(target)
        
        attackerStats.hitsLanded++
        attackerStats.currentCombo++
        attackerStats.damageDealt += event.finalDamage.toInt()
        
        if (attackerStats.currentCombo > attackerStats.longestCombo) {
            attackerStats.longestCombo = attackerStats.currentCombo
        }
        
        // Reset target's combo and update their health
        targetStats.currentCombo = 0
        targetStats.health = (target.health - event.finalDamage).toFloat().coerceAtLeast(0f)
    }
    
    /**
     * Handle player quit during match
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        matchManager.onPlayerDisconnect(event.player)
    }
    
    /**
     * Handle player kick during match
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerKick(event: PlayerKickEvent) {
        matchManager.onPlayerDisconnect(event.player)
    }
    
    /**
     * Track soup healing usage and count remaining soups
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        
        val player = event.player
        val match = matchManager.getMatch(player) ?: return
        val item = event.item ?: return
        
        // Track soup healing
        if (item.type == Material.MUSHROOM_SOUP) {
            val stats = match.getStatistics(player)
            stats.healsUsed++
            stats.healType = me.jordanfails.ascendduels.match.MatchStatistics.HealType.SOUP
            
            // Count remaining soups after this use
            stats.healsRemaining = player.inventory.contents.count { it?.type == Material.MUSHROOM_SOUP } - 1
        }
    }
    
    /**
     * Track potion consumption and count remaining potions
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        val player = event.player
        val match = matchManager.getMatch(player) ?: return
        val item = event.item
        
        // Track potion healing
        if (item.type.name.contains("POTION")) {
            val stats = match.getStatistics(player)
            stats.healsUsed++
            stats.healType = me.jordanfails.ascendduels.match.MatchStatistics.HealType.POT
            
            // Count remaining potions after this use
            stats.healsRemaining = player.inventory.contents.count { 
                it?.type?.name?.contains("POTION") == true 
            } - 1
        }
    }
}
