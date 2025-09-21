package me.jordanfails.ascendduels.api.event.match.impl

import me.jordanfails.ascendduels.api.event.match.MatchEvent
import me.jordanfails.ascendduels.match.Match
import me.jordanfails.ascendduels.match.MatchState

class MatchChangeStateEvent(
    match: Match<*, *>,
    val oldState: MatchState?,
    val newState: MatchState = match.state ?: MatchState.STARTING
) : MatchEvent(match)