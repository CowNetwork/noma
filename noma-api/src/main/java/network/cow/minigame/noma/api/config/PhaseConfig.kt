package network.cow.minigame.noma.api.config

import network.cow.minigame.noma.api.phase.Phase

/**
 * @author Benedikt Wüller
 */
data class PhaseConfig<PlayerType : Any>(
    val key: String,
    val kind: Class<out Phase<PlayerType, *>>,
    val allowsNewPlayers: Boolean,
    val requiresActors: Boolean,
    val phaseEndCountdown: PhaseEndCountdown,
    val timeout: PhaseTimeout,
    val options: Map<String, Any>
)

data class PhaseTimeout(val duration: Long)

data class PhaseEndCountdown(val duration: Long)
