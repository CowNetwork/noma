package network.cow.minigame.noma.api

/**
 * @author Benedikt Wüller
 */
open class GameState(var currentPhase: Phase? = null)

data class Phase(val key: String, var secondsLeft: Long)
