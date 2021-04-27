package network.cow.minigame.noma.api.pool

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.PoolConfig

/**
 * @author Benedikt Wüller
 */
class StringPool<PlayerType : Any>(game: Game<PlayerType>, config: PoolConfig<PlayerType>) : Pool<PlayerType, String>(game, config) {

    override fun getItem(key: String): String = key

}
