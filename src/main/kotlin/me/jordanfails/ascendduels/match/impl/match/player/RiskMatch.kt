package me.jordanfails.ascendduels.match.impl.match.player

import io.papermc.lib.PaperLib
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.match.MatchState
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.pvpwars.core.Core
import net.pvpwars.core.util.StringUtil
import net.pvpwars.core.util.runnable.RunnableBuilder
import me.jordanfails.ascendduels.arena.Arena
import me.jordanfails.ascendduels.inventory.RiskPostMatchInventory
import me.jordanfails.ascendduels.kit.Kit
import mkremins.fanciful.FancyMessage
import net.pvpwars.core.util.CC
import org.apache.commons.lang3.StringUtils
import org.bukkit.*
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

class RiskMatch(
    kit: Kit,
    arena: Arena,
    playerOne: Player,
    playerTwo: Player
) : PlayerMatch(kit, arena, playerOne, playerTwo) {

    private var riskPostMatchInventory: RiskPostMatchInventory? = null

    override fun prepare(player: Player, index: Int) {
        val spawnPoint = arena.getSpawnLocation(index)

        // Runs async chunk load, then applies setup
        PaperLib.getChunkAtAsync(spawnPoint).thenAccept {
            player.foodLevel = 20
            player.saturation = 5f
            player.health = player.maxHealth
            player.fireTicks = 0
            player.gameMode = GameMode.SURVIVAL
            player.fallDistance = 0f
            player.allowFlight = false
            player.isFlying = false

            player.setMetadata(
                "ignoreSector",
                FixedMetadataValue(AscendDuels.instance, true)
            )

            // Remove all potion effects
            player.activePotionEffects.forEach { effect ->
                player.removePotionEffect(effect.type)
            }

            player.closeInventory()
            player.teleport(spawnPoint)

            kit.inventory!!.load(player, true)
            player.saturation = 10f
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onEnd(instant: Boolean) {
        if (winner == null) {
            yeetAndClean()
            return
        }

        AscendDuels.instance
            .matchInventoryService
            .cacheInventory(winner!!.get(), getStatistics(winner!!.get()))

        if (!instant) {
            RunnableBuilder.forPlugin(AscendDuels.instance)
                .with { yeetAndClean() }
                .runSyncLater(100L)
        } else {
            yeetAndClean()
        }

        Bukkit.broadcastMessage(
            AscendDuels.prefix(
                "{0} &fhas defeated {1} &fin a duel!",
                winner!!.displayName,
                loser!!.displayName
            )
        )

        val header = StringUtil.center("&c&lMatch Results &7&o(Click a name to expand their stats)")
        val resultMessage: Array<BaseComponent> = getResultMessage(winner!!.get(), loser!!.get())

        consumePlayers { player ->
            player.sendMessage(" ")
            player.sendMessage(header)
            player.sendMessage(" ")
            resultMessage.forEach { player.spigot().sendMessage(it) }
            player.sendMessage(" ")
        }

        riskPostMatchInventory?.let { lootInv ->
            val lootMessageFancy = FancyMessage(
                StringUtil.repeat(" ", StringUtil.getCenterSpaceCount("Risk Duel Loot") + 1)
            ).then("Risk Duel Loot")
                .color(org.bukkit.ChatColor.RED)
                .style(org.bukkit.ChatColor.BOLD)
                .tooltip("Click to take opponent's loot")
                .command("/riskmatchloot ${lootInv.uuid}")

            lootMessageFancy.send(winner!!.get())
            winner!!.get().sendMessage(
                StringUtil.center("&7(You have 3 minutes to take the loot)")
            )
            winner!!.get().sendMessage(" ")

            winner!!.get().sendTitle(
                CC.translate("&a&lWinner"),
                CC.translate(
                    "&fYou have defeated &6${loser!!.get().name}&f!"
                )
            )
            loser!!.get().sendTitle(CC.translate("&c&lLoser"),
                CC.translate(
                    "&fYou lost to &6${winner!!.get().name}&f!"
                )
            )
        }
    }

    override fun onDeath(player: Player, reason: DeathReason) {
        handlePlayerDeath(player)
        // Match ending is now handled in handlePlayerDeath after spectator period
    }

    private fun handlePlayerDeath(player: Player) {
        AscendDuels.instance
            .matchInventoryService.cacheInventory(player, getStatistics(player))

        riskPostMatchInventory =
            AscendDuels.instance.riskPostMatchInventoryService.create(player)

        val world = player.world

        // Items are no longer dropped on death - they are handled in match ending process

        player.foodLevel = 20
        player.saturation = 5f
        player.health = player.maxHealth
        player.fireTicks = 0

        player.noDamageTicks = 60
        
        // Don't clear inventory here - it will be handled in match ending process

        player.velocity = Vector(player.velocity.x, 3.0, player.velocity.z)

        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 19, 0))
        world.playSound(player.location, Sound.IRONGOLEM_DEATH, 1.0f, 1.0f)

        // Put player in spectator mode for 5 seconds
        player.gameMode = GameMode.SPECTATOR
        player.sendMessage("§7You are now spectating...")
        
        // Handle inventory for the dead player immediately
        val isWinner = false // The dead player is never the winner
        AscendDuels.instance.playerInventoryManager.handleMatchEnd(player, true, isWinner)
        
        // Schedule match end after 5 seconds of spectating
        RunnableBuilder.forPlugin(AscendDuels.instance)
            .with { 
                if (state != MatchState.ENDED) {
                    loser = findParticipant(player)
                    winner = getOpponent(player)
                    
                    loser?.let { l ->
                        val craftPlayer = l.get() as CraftPlayer
                        craftPlayer.saveData() // snapshot data persistence
                    }
                    
                    // Handle inventory for the winner when match ends
                    winner?.let { winnerParticipant ->
                        val winnerPlayer = winnerParticipant.get()
                        AscendDuels.instance.playerInventoryManager.handleMatchEnd(winnerPlayer, true, true)
                    }
                    
                    end()
                }
            }
            .runSyncLater(100L) // 5 seconds (20 ticks per second)

        RunnableBuilder.forPlugin(AscendDuels.instance)
            .with {
                if (player.isOnline && player.world == world) {
                    for (effect in player.activePotionEffects) {
                        player.removePotionEffect(effect.type)
                    }
                    player.foodLevel = 20
                    player.saturation = 5f
                    player.health = player.maxHealth
                    player.fireTicks = 0
                    player.allowFlight = false
                    player.isFlying = false
                    player.flySpeed = player.flySpeed
                    player.removeMetadata("ignoreSector", AscendDuels.instance)
                    player.fallDistance = 0f
                    player.gameMode = GameMode.SURVIVAL
                    player.velocity = Vector()
                    player.teleport(Core.getInstance().locationFile.spawn)
                }
            }
            .runSyncLater(100L)
    }
}