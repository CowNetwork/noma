package network.cow.minigame.noma.api.actor

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.ActorProviderConfig

/**
 * @author Benedikt Wüller
 */
abstract class ActorProvider<PlayerType : Any, GameType : Game<PlayerType, GameType>>(protected val game: GameType, protected val config: ActorProviderConfig<PlayerType, GameType>) {

    private val playerActors = mutableMapOf<PlayerType, Actor<PlayerType>>()

    fun addPlayer(player: PlayerType) {
        if (this.playerActors.containsKey(player)) return
        val actor = this.selectActor(player)
        this.playerActors[player] = actor
        actor.addPlayer(player)

        if (!this.game.getActors().contains(actor)) {
            this.addActor(actor)
        }
    }

    fun removePlayer(player: PlayerType) {
        val actor = this.playerActors.remove(player) ?: return
        actor.removePlayer(player)

        if (actor.isEmpty()) {
            this.removeActor(actor)
        }
    }

    abstract fun selectActor(player: PlayerType) : Actor<PlayerType>

    protected fun addActor(actor: Actor<PlayerType>) = this.game.addActor(actor)

    protected fun removeActor(actor: Actor<PlayerType>) = this.game.removeActor(actor)

}
