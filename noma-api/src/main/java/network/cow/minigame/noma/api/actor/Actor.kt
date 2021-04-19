package network.cow.minigame.noma.api.actor

import java.awt.Color

/**
 * @author Benedikt Wüller
 */
abstract class Actor<T>(protected val initialName: String? = null, var color: Color = Color.WHITE) {

    private val players = mutableListOf<T>()

    val size: Int; get() = this.players.size

    fun isEmpty() = this.players.isEmpty()

    fun isNotEmpty() = this.players.isNotEmpty()

    open fun getName(): String = this.initialName ?: "¯\\_(ツ)_/¯"

    fun addPlayer(player: T) = this.players.add(player)

    fun removePlayer(player: T) = this.players.remove(player)

    fun clearPlayers() = this.players.clear()

    fun getPlayers(): List<T> = this.players

    fun apply(executor: (T) -> Unit) = this.players.forEach { executor(it) }

}
