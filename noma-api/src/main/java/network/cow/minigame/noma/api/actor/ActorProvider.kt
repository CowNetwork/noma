package network.cow.minigame.noma.api.actor

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.ActorProviderConfig

/**
 * @author Benedikt Wüller
 */
abstract class ActorProvider<T>(protected val game: Game<T>, protected val config: ActorProviderConfig<T>) {

    private val playerActors = mutableMapOf<T, Actor<T>>()

    fun addPlayer(player: T) {
        if (this.playerActors.containsKey(player)) return
        val actor = this.selectActor(player)
        this.playerActors[player] = actor
        actor.addPlayer(player)

        if (!this.game.getActors().contains(actor)) {
            this.game.addActor(actor)
        }
    }

    fun removePlayer(player: T) {
        val actor = this.playerActors.remove(player) ?: return
        actor.removePlayer(player)

        if (actor.isEmpty()) {
            this.game.removeActor(actor)
        }
    }

    abstract fun selectActor(player: T) : Actor<T>

    protected fun addActor(actor: Actor<T>) = this.game.addActor(actor)

    protected fun removeActor(actor: Actor<T>) = this.game.removeActor(actor)

}
