package me.jordanfails.ascendduels.match.impl.match.player

import io.papermc.lib.PaperLib
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.match.Match
import me.jordanfails.ascendduels.match.MatchStatistics
import me.jordanfails.ascendduels.match.impl.participant.PlayerParticipant
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.pvpwars.core.Core
import net.pvpwars.core.util.StringUtil
import net.pvpwars.core.util.runnable.RunnableBuilder
import me.jordanfails.ascendduels.arena.Arena
import me.jordanfails.ascendduels.kit.KitTag
import me.jordanfails.ascendduels.match.MatchState
import me.jordanfails.ascendduels.kit.Kit
import org.apache.commons.lang.StringUtils
import org.bukkit.*
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import java.util.*

open class PlayerMatch(
    kit: Kit,
    arena: Arena,
    playerOne: Player,
    playerTwo: Player
) : Match<Player, PlayerParticipant>(kit, arena) {

    private val players: List<Player> = listOf(playerOne, playerTwo)
    private val participants: List<PlayerParticipant> =
        listOf(PlayerParticipant(playerOne), PlayerParticipant(playerTwo))

    private val statistics: MutableMap<UUID, MatchStatistics> = HashMap()

    var winner: PlayerParticipant? = null
    var loser: PlayerParticipant? = null

    override fun onStart() {
        val p1 = getParticipant(0)
        val p2 = getParticipant(1)

        prepare(p1.get(), 0)
        prepare(p2.get(), 1)
    }

    protected open fun prepare(player: Player, index: Int) {
        player.closeInventory()

        val spawnPoint = arena.getSpawnLocation(index)
        PaperLib.getChunkAtAsync(spawnPoint).thenAccept { _ ->
            player.foodLevel = 20
            player.health = player.maxHealth
            player.fireTicks = 0
            player.gameMode = GameMode.SURVIVAL
            player.fallDistance = 0f
            player.allowFlight = false
            player.isFlying = false
            player.setMetadata("ignoreSector", FixedMetadataValue(AscendDuels.instance, true))

            player.activePotionEffects.forEach { effect: PotionEffect ->
                player.removePotionEffect(effect.type)
            }

            player.closeInventory()
            player.teleport(spawnPoint)

            kit.inventory!!.load(player)
            player.saturation = 10f
        }
    }

    override fun onEnd(instant: Boolean) {
        if (winner == null) {
            yeetAndClean()
            return
        }

        AscendDuels.instance.matchInventoryService
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
        val resultMessage = getResultMessage(winner!!.get(), loser!!.get())

        consumePlayers { player ->
            player.sendMessage(" ")
            player.sendMessage(header)
            player.sendMessage(" ")
            resultMessage.forEach { message ->
                player.spigot().sendMessage(message)
            }
            player.sendMessage(" ")
        }
    }

    protected fun yeetAndClean() {
        consumePlayers { player ->
            if (player.isOnline && player.world == arena.bounds!!.getWorld()) {
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

        entitiesToClear.filter(Entity::isValid).forEach(Entity::remove)
        entitiesToClear.clear()

        arena.inUse = false
        AscendDuels.instance.matchService.unregisterMatch(this)
    }

    protected fun getResultMessage(winner: Player, loser: Player): Array<BaseComponent> {
        val centered = StringUtils.repeat(
            " ",
            StringUtil.getCenterSpaceCount(listOf(winner.name, loser.name).joinToString(", "))
        )

        val builder = ComponentBuilder(centered)
        appendPlayerStats(winner, builder, ChatColor.GREEN)
        builder.append(", ").retain(ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
        appendPlayerStats(loser, builder, ChatColor.RED)

        return builder.create()
    }

    private fun appendPlayerStats(player: Player, builder: ComponentBuilder, color: ChatColor) {
        builder.append(player.name).retain(ComponentBuilder.FormatRetention.NONE).color(color)
        val statistics = getStatistics(player)

        val healLine = when (statistics.healType) {
            MatchStatistics.HealType.POT -> "&r &8• &bPotions (remaining | missed): &f${statistics.healsRemaining} | ${statistics.healsMissed}\n"
            MatchStatistics.HealType.SOUP -> "&r &8• &bSoups (remaining | used): &f${statistics.healsRemaining} | ${statistics.healsUsed}\n"
            else -> ""
        }

        val hoverComponent = TextComponent.fromLegacyText(
            StringUtil.color(
                "${color}Player Rundown:&r\n\n" +
                        "&r &8• &bHealth: &f${(Math.round(statistics.health * 2f) / 2f)}&c♥\n" +
                        "&r &8• &bHits (all | longest combo): &f${statistics.hitsLanded} | ${statistics.longestCombo}\n" +
                        healLine +
                        "\n&r&7&oClick to expand match statistics."
            )
        )

        builder.event(HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent))
        val stringUUID = player.uniqueId.toString()
        builder.event(
            ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/duelinventory $stringUUID"
            )
        )
    }

    override fun onDeath(player: Player, reason: DeathReason) {
        handlePlayerDeath(player)

        if (state != MatchState.ENDED) {
            loser = findParticipant(player)
            winner = getOpponent(player)
            end()
        }
    }

    private fun handlePlayerDeath(player: Player) {
        AscendDuels.instance.matchInventoryService.cacheInventory(player, getStatistics(player))

        val world = player.world

        fun dropInvItems(items: Array<ItemStack?>) {
            for (itemStack in items) {
                if (itemStack != null && itemStack.type != Material.AIR) {
                    val itemEntity: Item = world.dropItemNaturally(player.location, itemStack)
                    itemEntity.pickupDelay = 120
                    itemEntity.setMetadata(
                        "SKIP_CLEANUP",
                        FixedMetadataValue(AscendDuels.instance, true)
                    )
                    entitiesToClear.add(itemEntity)
                }
            }
        }

        dropInvItems(player.inventory.contents)
        dropInvItems(player.inventory.armorContents)

        player.foodLevel = 20
        player.saturation = 5f
        player.health = player.maxHealth
        player.fireTicks = 0
        player.noDamageTicks = 60

        player.inventory.clear()
        player.inventory.armorContents = null
        player.updateInventory()

        player.velocity = Vector(player.velocity.x, 3.0, player.velocity.z)

        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 19, 0))
        world.playSound(player.location, Sound.IRONGOLEM_DEATH, 1.0f, 1.0f)

        player.allowFlight = true
        player.isFlying = true
        player.flySpeed = player.flySpeed
    }

    override fun canHurt(damager: Player, entity: Player): Boolean = true

    override fun getStatistics(player: Player): MatchStatistics =
        statistics.computeIfAbsent(player.uniqueId) {
            MatchStatistics().apply {
                healType = when {
                    kit.hasTag(KitTag.SOUP) -> MatchStatistics.HealType.SOUP
                    kit.hasTag(KitTag.NORMAL) || !kit.hasTag(KitTag.HARDCORE) -> MatchStatistics.HealType.POT
                    else -> healType
                }
            }
        }

    override fun findParticipant(player: Player): PlayerParticipant? =
        participants.firstOrNull { it.get().uniqueId == player.uniqueId }

    override fun getParticipants(): Collection<PlayerParticipant> = participants
    override fun getPlayers(): Collection<Player> = players

    protected fun getParticipant(index: Int): PlayerParticipant = participants[index]

    protected fun getOpponent(player: Player): PlayerParticipant? =
        participants.firstOrNull { it.get().uniqueId != player.uniqueId }
}