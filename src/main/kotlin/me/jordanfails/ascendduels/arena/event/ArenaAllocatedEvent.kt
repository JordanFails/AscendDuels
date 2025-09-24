package me.jordanfails.ascendduels.arena.event

import me.jordanfails.ascendduels.arena.Arena
import org.bukkit.event.HandlerList

/**
 * Called when an [Arena] is allocated for use by a
 * [net.frozenorb.potpvp.match.Match]
 */
class ArenaAllocatedEvent(arena: Arena) : ArenaEvent(arena) {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }
}