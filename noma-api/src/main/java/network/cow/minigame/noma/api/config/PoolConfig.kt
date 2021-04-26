package network.cow.minigame.noma.api.config

import network.cow.minigame.noma.api.pool.Pool

/**
 * @author Benedikt Wüller
 */
data class PoolConfig<PlayerType : Any>(
    val key: String,
    val kind: Class<out Pool<PlayerType, *>>,
    val items: List<String>,
    val options: Map<String, Any>
)
