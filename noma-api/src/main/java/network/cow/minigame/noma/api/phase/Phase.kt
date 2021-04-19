package network.cow.minigame.noma.api.phase

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.PhaseConfig

/**
 * @author Benedikt Wüller
 */
abstract class Phase<PlayerType : Any, ResultType : Any>(protected val game: Game<PlayerType>, val config: PhaseConfig<PlayerType, ResultType>) {

    private lateinit var result: ResultType

    internal open fun start() {
        this.onStart()
    }

    internal open fun stop() : ResultType {
        this.result = this.onStop()
        return this.getResult()
    }

    open fun timeout() : ResultType {
        this.onTimeout()
        return this.stop()
    }

    open fun join(player: PlayerType) {
        this.onPlayerJoin(player)
    }

    open fun leave(player: PlayerType) {
        this.onPlayerLeave(player)
    }

    protected abstract fun onStart()

    protected abstract fun onTimeout()

    protected abstract fun onStop() : ResultType

    protected abstract fun onPlayerJoin(player: PlayerType)

    protected abstract fun onPlayerLeave(player: PlayerType)

    open fun getResult() : ResultType {
        if (!this::result.isInitialized) error("This phase (${this.config.key}) has not been completed yet.")
        return this.result
    }

}
