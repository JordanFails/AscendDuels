package me.jordanfails.ascendduels.listener

import com.github.sirblobman.combatlogx.api.event.PlayerPreTagEvent
import com.github.sirblobman.combatlogx.api.event.PlayerTagEvent
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.arena.ArenaHandler
import me.jordanfails.ascendduels.kit.KitTag
import me.jordanfails.ascendduels.match.Match
import me.jordanfails.ascendduels.match.MatchService
import me.jordanfails.ascendduels.match.MatchState
import me.jordanfails.ascendduels.match.impl.match.player.RiskMatch
import me.jordanfails.ascendduels.utils.EventUtil
// import me.jordanfails.ascendduels.utils.EventUtil.playDeathReplay
import me.jordanfails.ascendduels.utils.KillcamManager
import me.jordanfails.ascendduels.listener.TeleportListener
import net.pvpwars.core.Core
import net.pvpwars.core.game.features.armorsets.events.ArmorSetWearEvent
import net.pvpwars.core.util.material.MaterialUtil
import net.pvpwars.core.util.runnable.RunnableBuilder
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack

class PlayerListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val world = player.world
        if (AscendDuels.instance.arenaHandler.getArenaWorld() == world) {
            val spawn = Core.getInstance().locationFile.spawn
            RunnableBuilder.forPlugin(AscendDuels.instance)
                .with { player.teleport(spawn) }
                .runSync()
            RunnableBuilder.forPlugin(AscendDuels.instance)
                .with { player.teleport(spawn) }
                .runSyncLater(2L)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onSplash(event: PotionSplashEvent) {
        val potion = event.potion
        val shooter = potion.shooter
        if (shooter !is Player) return
        if (potion.item.durability.toInt() != 16421) return

        val match = AscendDuels.instance.matchService.getByPlayer(shooter)
        if (match != null) {
            val stats = match.getStatistics(shooter)
            stats.healsUsed++
            if (event.getIntensity(shooter) < 0.6) stats.healsMissed++
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return

        val player = event.player
        if (player.health >= player.maxHealth) return
        val item = player.itemInHand ?: return
        if (item.type != Material.MUSHROOM_SOUP) return

        val match = AscendDuels.instance.matchService.getByPlayer(player)
        if (match != null) {
            event.isCancelled = true
            match.getStatistics(player).healsUsed++
            player.itemInHand = ItemStack(Material.BOWL)
            player.updateInventory()
            player.health = (player.health + 7.0).coerceAtMost(player.maxHealth)
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onInventoryOpen(event: InventoryOpenEvent) {
        val player = event.player as? Player ?: return
        val match = AscendDuels.instance.matchService.getByPlayer(player)
        if (match != null) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onDropItem(event: PlayerDropItemEvent) {
        val player = event.player
        val match = AscendDuels.instance.matchService.getByPlayer(player) ?: return

        if (match is RiskMatch || match.state == MatchState.ENDED) {
            event.isCancelled = true
            return
        }

        val itemEntity = event.itemDrop
        val itemStack = itemEntity.itemStack
        when {
            MaterialUtil.isEquipment(itemStack.type) && !match.kit.hasTag(KitTag.DROPPABLE) -> {
                event.isCancelled = true
            }
            itemStack.type == Material.BOWL || itemStack.type == Material.GLASS_BOTTLE -> {
                itemEntity.pickupDelay = 20
                RunnableBuilder.forPlugin(AscendDuels.instance).with {
                    if (itemEntity.isValid) itemEntity.remove()
                    match.entitiesToClear?.remove(itemEntity)
                }.runSyncLater(4L)
            }
            else -> {
                RunnableBuilder.forPlugin(AscendDuels.instance).with {
                    if (itemEntity.isValid) itemEntity.remove()
                    match.entitiesToClear?.remove(itemEntity)
                }.runSyncLater(20L * 30L)
            }
        }

        if (!event.isCancelled) {
            match.entitiesToClear.add(itemEntity)
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPickupItem(event: PlayerPickupItemEvent) {
        val player = event.player
        val match = AscendDuels.instance.matchService.getByPlayer(player)
        if (match != null && (match is RiskMatch || match.state == MatchState.ENDED)) {
            event.isCancelled = true
        }
    }

    // Damage handling moved to DuelEventListener for new match system

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onArmorSetEquip(event: ArmorSetWearEvent) {
        if (!event.armorSet.internalName.equals("Builder", true)) return
        val player = event.player
        val match = AscendDuels.instance.matchService.getByPlayer(player)
        if (match != null && match.kit.hasTag(KitTag.SOUP)) {
            event.isCancelled = true
        }
    }

    // Entity damage by entity handling moved to DuelEventListener for new match system

    // Death handling moved to DuelEventListener for new match system

    // Quit/kick handling moved to DuelEventListener for new match system

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onTeleport(event: PlayerTeleportEvent) {
        val player = event.player
        val arenaService: ArenaHandler = AscendDuels.instance.arenaHandler
        val matchService: MatchService = AscendDuels.instance.matchService

        val fromWorld: World = event.from.world
        val toWorld: World = event.to.world

        if (toWorld != fromWorld) {
            // Handle pre-duel setup
            if (arenaService.isWorld(toWorld)) {
                // Entering an arena world
                TeleportListener.pinInventory(player)
                player.removeMetadata("riskMatchConfirming", AscendDuels.instance)
            }
            // Note: Inventory saving/restoration is now handled in match lifecycle

            // Now handle cancellation logic
            if (arenaService.isWorld(toWorld)
                && !arenaService.isWorld(fromWorld)
                && matchService.getByPlayer(player) == null
            ) {
                event.isCancelled = true
            }

            if (arenaService.isWorld(fromWorld)
                && !arenaService.isWorld(toWorld)
            ) {
                val match = matchService.getByPlayer(player)
                if (match != null && match.state != MatchState.ENDED) {
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        if (AscendDuels.instance.matchService.getByPlayer(player) == null) return
        if (player.isOp) return

        val msg = event.message.lowercase()
        val allowed = listOf(
            "/hub", "/lobby", "/server", "/list", "/duelinventory",
            "/ft", "/itemfilter", "/sc", "/staffchat", "/msg", "/m",
            "/pm", "/tell", "/t", "/helpop", "/request", "/report",
            "/f c", "/f chat"
        )

        val allowedPrefix = allowed.any { msg.startsWith(it) || msg == it }
        if (!allowedPrefix) {
            player.sendMessage(AscendDuels.prefix("&fYou can't do commands while in a duel!"))
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onProjectileHit(event: ProjectileHitEvent) {
        val source = event.entity.shooter
        if (source is Player) {
            val match = AscendDuels.instance.matchService.getByPlayer(source)
            if (match != null) {
                match.entitiesToClear?.add(event.entity)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        if (KillcamManager.isInKillcam(player.uniqueId)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPreTag(event: PlayerPreTagEvent) {
        val match = AscendDuels.instance.matchService.getByPlayer(event.player)
        if (match != null) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onReplay(event: PlayerDeathEvent) {
        val victim = event.entity
        // Note: playDeathReplay function not available, skipping
        val match = AscendDuels.instance.matchService.getByPlayer(victim) ?: return

        val killer = (victim.lastDamageCause as? EntityDamageByEntityEvent)?.let {
            EventUtil.linkProjectile(it.damager)
        }

        // Cancel auto-respawn/processing for a moment
        event.deathMessage = null // Hide vanilla death msg

        if (killer != null) {
            match.sendMessage(AscendDuels.prefix("&c${victim.displayName} &fwas slain by &c${killer.displayName}&f!"))
            // playDeathReplay(victim, killer, match) // Function not available
        } else {
            match.sendMessage(AscendDuels.prefix("&c${victim.displayName} &fdied!"))
            match.onDeath(victim, Match.DeathReason.OTHER)
        }
    }
}