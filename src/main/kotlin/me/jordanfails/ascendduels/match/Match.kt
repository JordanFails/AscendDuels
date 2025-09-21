package me.jordanfails.ascendduels.match

import com.rit.sucy.CustomEnchantment
import com.rit.sucy.EnchantmentAPI
import com.rit.sucy.service.ENameParser
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.api.event.match.impl.MatchChangeStateEvent
import me.jordanfails.ascendduels.api.event.match.impl.MatchEndEvent
import me.jordanfails.ascendduels.api.event.match.impl.MatchStartEvent
import net.pvpwars.core.util.StringUtil
import net.pvpwars.core.util.runnable.RunnableBuilder
import me.jordanfails.ascendduels.arena.Arena
import me.jordanfails.ascendduels.arena.GenArena
import me.jordanfails.ascendduels.kit.Kit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta
import kotlin.collections.HashSet

abstract class Match<T, P : MatchParticipant<T>>(
    val kit: Kit,
    val arena: Arena,
    val genArena: GenArena
) {

    var state: MatchState? = null
        private set

    var secondsTilStart: Int = 6
    var secondsTilTimeout: Int = 3600

    val entitiesToClear: MutableSet<Entity> = HashSet()

    protected abstract fun onStart()
    protected abstract fun onEnd(instant: Boolean)

    abstract fun onDeath(player: Player, reason: DeathReason)

    abstract fun canHurt(damager: Player, entity: Player): Boolean

    abstract fun getStatistics(player: Player): MatchStatistics

    abstract fun findParticipant(player: Player): P?
    abstract fun getParticipants(): Collection<P>

    abstract fun getPlayers(): Collection<Player>

    fun tick() {
        if (state == MatchState.STARTING) {
            if (--secondsTilStart > 0) {
                sendMessage(
                    AscendDuels.prefix(
                        "The match will begin in {0} second{1}...",
                        secondsTilStart, if (secondsTilStart == 1) "" else "s"
                    )
                )
                playSound(Sound.NOTE_PLING, 1.0f, 1.0f)
            } else {
                setState(MatchState.ONGOING)

                sendMessage(AscendDuels.prefix("The match has now started!"))
                playSound(Sound.NOTE_PLING, 3.0f, 2.0f)

                consumePlayers { player ->
                    for (armorContent in player.inventory.armorContents) {
                        if (armorContent == null || armorContent.type == Material.AIR) continue

                        val meta: ItemMeta = armorContent.itemMeta ?: continue
                        if (!meta.hasLore()) continue

                        for (lore in meta.lore ?: emptyList()) {
                            val name = ENameParser.parseName(lore)
                            val level = ENameParser.parseLevel(lore)
                            if (name == null || level == 0) continue

                            if (EnchantmentAPI.isRegistered(name)) {
                                val enchantment: CustomEnchantment =
                                    EnchantmentAPI.getEnchantment(name)
                                enchantment.applyEquipEffect(player, level)
                            }
                        }
                    }
                }
            }
        }

        if (--secondsTilTimeout == 0) {
            end()
        }
    }

    fun start() {
        genArena.occupied = true
        setState(MatchState.STARTING)
        onStart()
        MatchStartEvent(this).call()
    }

    fun end() = end(false)

    fun end(instant: Boolean) {
        if (!instant) {
            RunnableBuilder.forPlugin(AscendDuels.instance)
                .with { AscendDuels.instance.matchService.unregisterMatch(this) }
                .runSyncLater(20L * 10)
        } else {
            AscendDuels.instance.matchService.unregisterMatch(this)
        }

        if (state != MatchState.ENDED) {
            setState(MatchState.ENDED)
            onEnd(instant)
            MatchEndEvent(this).call()
        }
    }

    fun setState(state: MatchState) {
        MatchChangeStateEvent(this, this.state!!, state).call()
        this.state = state
    }

    fun sendMessage(message: String) {
        val finalMessage = StringUtil.color(message)
        consumeParticipants { participant -> participant.sendMessage(finalMessage) }
    }

    fun sendMessage(message: String, vararg arguments: Any) {
        sendMessage(StringUtil.format(message, *arguments))
    }

    fun playSound(sound: Sound, volume: Float, pitch: Float) {
        consumePlayers { player ->
            player.playSound(player.location, sound, volume, pitch)
        }
    }

    fun consumePlayers(consumer: (Player) -> Unit) {
        getPlayers().forEach(consumer)
    }

    fun consumeParticipants(consumer: (P) -> Unit) {
        getParticipants().forEach(consumer)
    }

    enum class DeathReason {
        QUIT,
        KICKED,
        KILLED,
        OTHER
    }
}