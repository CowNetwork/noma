package network.cow.minigame.noma.api.phase

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.config.StoreMiddlewareConfig
import network.cow.minigame.noma.api.state.Store
import network.cow.minigame.noma.api.state.StoreMiddleware

/**
 * @author Benedikt Wüller
 */
abstract class Phase<PlayerType : Any>(protected val game: Game<PlayerType>, val config: PhaseConfig<PlayerType>) {

    protected val storeMiddleware: StoreMiddleware = this.config.storeMiddleware.kind
        .getDeclaredConstructor(Phase::class.java, Store::class.java, StoreMiddlewareConfig::class.java)
        .newInstance(this, this.game.store, this.config.storeMiddleware)

    open fun start() {
        this.onStart()
    }

    open fun stop() {
        this.onStop()
    }

    open fun timeout() {
        this.onTimeout()
        this.stop()
    }

    open fun join(player: PlayerType) {
        this.onPlayerJoin(player)
    }

    open fun leave(player: PlayerType) {
        this.onPlayerLeave(player)
    }

    protected abstract fun onStart()

    protected abstract fun onTimeout()

    protected abstract fun onStop()

    protected abstract fun onPlayerJoin(player: PlayerType)

    protected abstract fun onPlayerLeave(player: PlayerType)

}
