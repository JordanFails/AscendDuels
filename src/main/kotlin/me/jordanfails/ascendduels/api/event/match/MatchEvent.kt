package me.jordanfails.ascendduels.api.event.match

import me.jordanfails.ascendduels.api.event.DuelsEvent
import me.jordanfails.ascendduels.match.Match

open class MatchEvent(
    val match: Match<*, *>
) : DuelsEvent()