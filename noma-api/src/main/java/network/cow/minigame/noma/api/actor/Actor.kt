package network.cow.minigame.noma.api.actor

import java.awt.Color

/**
 * @author Benedikt Wüller
 */
abstract class Actor<PlayerType : Any>(protected val initialName: String? = null, var color: Color = Color.WHITE) {

    private val players = mutableListOf<PlayerType>()

    val size: Int; get() = this.players.size

    fun isEmpty() = this.players.isEmpty()

    fun isNotEmpty() = this.players.isNotEmpty()

    open fun getName(): String = this.initialName ?: "¯\\_(ツ)_/¯"

    fun addPlayer(player: PlayerType) = this.players.add(player)

    fun removePlayer(player: PlayerType) = this.players.remove(player)

    fun clearPlayers() = this.players.clear()

    fun getPlayers(): List<PlayerType> = this.players

    fun apply(executor: (PlayerType) -> Unit) = this.players.forEach { executor(it) }

}
