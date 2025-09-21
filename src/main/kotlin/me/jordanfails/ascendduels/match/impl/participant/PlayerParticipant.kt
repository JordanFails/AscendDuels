package me.jordanfails.ascendduels.match.impl.participant

import me.jordanfails.ascendduels.match.MatchParticipant
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

class PlayerParticipant(player: Player) : MatchParticipant<Player>(player) {

    override val uuid: UUID
        get() = get().uniqueId

    override val name: String
        get() = get().name

    override val displayName: String
        get() = get().displayName

    override fun sendMessage(message: String) {
        get().sendMessage(message)
    }

    override fun teleport(location: Location) {
        get().teleport(location)
    }
}