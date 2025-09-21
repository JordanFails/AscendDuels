package me.jordanfails.ascendduels.api.event.match.impl

import me.jordanfails.ascendduels.api.event.match.MatchEvent
import me.jordanfails.ascendduels.match.Match

class MatchEndEvent(
    match: Match<*, *>
) : MatchEvent(match)