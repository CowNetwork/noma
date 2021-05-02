package network.cow.minigame.noma.api.config

import network.cow.minigame.noma.api.phase.Phase
import network.cow.minigame.noma.api.state.StoreMiddleware

/**
 * @author Benedikt Wüller
 */
data class PhaseConfig<PlayerType : Any>(
    val key: String,
    val kind: Class<out Phase<PlayerType>>,
    val allowsNewPlayers: Boolean,
    val requiresActors: Boolean,
    val phaseEndCountdown: PhaseEndCountdownConfig,
    val timeout: PhaseTimeoutConfig,
    val storeMiddleware: StoreMiddlewareConfig,
    val options: Map<String, Any>
)

data class PhaseTimeoutConfig(val duration: Long, val silent: Boolean)

data class PhaseEndCountdownConfig(val duration: Long)

data class StoreMiddlewareConfig(val kind: Class<out StoreMiddleware>, val options: Map<String, Any>)
