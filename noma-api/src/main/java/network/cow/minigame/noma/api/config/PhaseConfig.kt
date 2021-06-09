package network.cow.minigame.noma.api.config

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.phase.Phase
import network.cow.minigame.noma.api.store.middleware.StoreMiddleware

/**
 * @author Benedikt Wüller
 */
data class PhaseConfig<PlayerType : Any, GameType : Game<PlayerType, GameType>>(
    val key: String,
    val kind: Class<out Phase<PlayerType, GameType>>,
    var allowsNewPlayers: Boolean,
    var requiresActors: Boolean,
    val phaseEndCountdown: PhaseEndCountdownConfig,
    val timeout: PhaseTimeoutConfig,
    val storeMiddleware: StoreMiddlewareConfig,
    val options: Map<String, Any>
)

data class PhaseTimeoutConfig(var duration: Long, var silent: Boolean)

data class PhaseEndCountdownConfig(var duration: Long)

data class StoreMiddlewareConfig(val kind: Class<out StoreMiddleware>, val options: Map<String, Any>)
