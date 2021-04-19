package network.cow.minigame.noma.api

import network.cow.minigame.noma.api.actor.Actor
import network.cow.minigame.noma.api.actor.ActorProvider
import network.cow.minigame.noma.api.config.GameConfig

/**
 * @author Benedikt Wüller
 */
abstract class Game<T>(val config: GameConfig<T>) {

    private val actors = mutableListOf<Actor<T>>()

    val actorProvider: ActorProvider<T> = this.config.actorProvider.kind.getDeclaredConstructor(Game::class.java).newInstance(this)

    internal fun addActor(actor: Actor<T>) = this.actors.add(actor)

    internal fun removeActor(actor: Actor<T>) = this.actors.remove(actor)

    fun getActors() = this.actors.toTypedArray()

}
