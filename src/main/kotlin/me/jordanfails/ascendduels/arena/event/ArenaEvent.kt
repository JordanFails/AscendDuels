package me.jordanfails.ascendduels.arena.event

import com.google.common.base.Preconditions
import me.jordanfails.ascendduels.arena.Arena
import org.bukkit.event.Event

/**
 * Base type for all Arena-related events.
 */
abstract class ArenaEvent(arena: Arena) : Event() {
    val arena: Arena = Preconditions.checkNotNull(arena, "arena")
}