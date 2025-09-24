package me.jordanfails.ascendduels.arena.event

import me.jordanfails.ascendduels.arena.Arena
import org.bukkit.event.HandlerList

class ArenaReleasedEvent(arena: Arena) : ArenaEvent(arena) {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }
}