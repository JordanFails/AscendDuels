package me.jordanfails.ascendduels.match

import net.pvpwars.core.util.StringUtil
import org.bukkit.Location
import java.util.UUID

abstract class MatchParticipant<T>(
    private val participant: T
) {

    abstract val uuid: UUID
    abstract val name: String
    abstract val displayName: String

    abstract fun sendMessage(message: String)
    abstract fun teleport(location: Location)

    fun get(): T = participant

    fun sendMessage(message: String, vararg arguments: Any) {
        sendMessage(StringUtil.colorFormat(message, *arguments))
    }
}