package me.jordanfails.ascendduels.request

import me.jordanfails.ascendduels.arena.ArenaSchematic
import me.jordanfails.ascendduels.kit.Kit
import java.util.UUID

data class Request(
    val id: UUID = UUID.randomUUID(),
    var sender: UUID? = null,
    var receiver: UUID? = null,
    var kit: Kit? = null,
    var arena: ArenaSchematic? = null,
    var timestamp: Long = System.currentTimeMillis()
)
