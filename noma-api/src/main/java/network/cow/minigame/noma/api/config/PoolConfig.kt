package network.cow.minigame.noma.api.config

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.pool.Pool

/**
 * @author Benedikt Wüller
 */
data class PoolConfig<PlayerType : Any, GameType : Game<PlayerType, GameType>>(
    val key: String,
    val kind: Class<out Pool<PlayerType, GameType, *>>,
    val items: List<String>,
    val options: Map<String, Any>
)
