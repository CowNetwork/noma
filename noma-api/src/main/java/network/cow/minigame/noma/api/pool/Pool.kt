package network.cow.minigame.noma.api.pool

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.PoolConfig

/**
 * @author Benedikt Wüller
 */
abstract class Pool<PlayerType : Any, ItemType : Any>(protected val game: Game<PlayerType>, val config: PoolConfig<PlayerType>) {

    open fun getKeys() : List<String> = this.config.items

    abstract fun getItem(key: String) : ItemType

}
