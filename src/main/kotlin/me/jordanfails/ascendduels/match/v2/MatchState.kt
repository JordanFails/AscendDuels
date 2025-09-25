package me.jordanfails.ascendduels.match.v2

/**
 * Clean state machine for matches with clear transitions
 */
enum class MatchState {
    /**
     * Match has been created but not started yet
     */
    CREATED,
    
    /**
     * Players are being prepared (teleported, inventory setup, etc.)
     */
    PREPARING,
    
    /**
     * Countdown is active, players can see each other but can't fight yet
     */
    STARTING,
    
    /**
     * Match is active, players can fight
     */
    ACTIVE,
    
    /**
     * Someone died, match is ending (spectator period)
     */
    ENDING,
    
    /**
     * Match is completely finished, cleanup done
     */
    FINISHED;
    
    /**
     * Valid state transitions
     */
    fun canTransitionTo(newState: MatchState): Boolean {
        return when (this) {
            CREATED -> newState == PREPARING
            PREPARING -> newState == STARTING || newState == FINISHED // Can abort during prep
            STARTING -> newState == ACTIVE || newState == FINISHED // Can abort during countdown
            ACTIVE -> newState == ENDING || newState == FINISHED // Normal end or force end
            ENDING -> newState == FINISHED // Only way out is finish
            FINISHED -> false // Terminal state
        }
    }
    
    /**
     * States where players can take damage
     */
    fun canTakeDamage(): Boolean = this == ACTIVE
    
    /**
     * States where the match is considered "in progress"
     */
    fun isInProgress(): Boolean = this in setOf(PREPARING, STARTING, ACTIVE, ENDING)
    
    /**
     * States where players should be in the arena
     */
    fun shouldBeInArena(): Boolean = this in setOf(PREPARING, STARTING, ACTIVE, ENDING)
}
