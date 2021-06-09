package network.cow.minigame.noma.api.pool

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.PoolConfig

/**
 * @author Benedikt Wüller
 */
class StringPool<PlayerType : Any, GameType : Game<PlayerType, GameType>>(game: GameType, config: PoolConfig<PlayerType>) : Pool<PlayerType, GameType, String>(game, config) {

    override fun getItem(key: String): String = key

}
