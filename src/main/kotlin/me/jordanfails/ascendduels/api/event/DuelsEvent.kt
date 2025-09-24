package me.jordanfails.ascendduels.api.event

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class DuelsEvent : Event() {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    fun call(): DuelsEvent {
        Bukkit.getServer().pluginManager.callEvent(this)
        return this
    }

    companion object {
        // ✅ keep as private or internal so it does not auto-generate a getter with same JVM signature
        @JvmStatic
        private val handlerList: HandlerList = HandlerList()

        // ✅ provide the required static getter explicitly
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }
}