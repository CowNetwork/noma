package network.cow.minigame.noma.api.actor

import java.awt.Color

/**
 * @author Benedikt Wüller
 */
abstract class Actor<PlayerType : Any>(protected val initialName: String? = null, var color: Color = Color.WHITE) {

    private val players = mutableListOf<PlayerType>()

    var name = this.initialName ?: "¯\\_(ツ)_/¯"; private set

    val size: Int; get() = this.players.size

    fun isEmpty() = this.players.isEmpty()

    fun isNotEmpty() = this.players.isNotEmpty()

    protected open fun calculateName() : String {
        return this.initialName ?: "¯\\_(ツ)_/¯"
    }

    protected open fun updateName() {
        this.name = this.calculateName()
    }

    fun addPlayer(player: PlayerType) {
        this.onPlayerJoin(player)
        this.players.add(player)
        this.updateName()
    }

    fun removePlayer(player: PlayerType) {
        this.onPlayerLeave(player)
        this.players.remove(player)
        this.updateName()
    }

    fun clearPlayers() {
        this.players.forEach(this::onPlayerLeave)
        this.players.clear()
        this.updateName()
    }

    fun getPlayers(): List<PlayerType> = this.players

    fun apply(executor: (PlayerType) -> Unit) = this.players.forEach { executor(it) }

    protected open fun onPlayerJoin(player: PlayerType) = Unit

    protected open fun onPlayerLeave(player: PlayerType) = Unit

}
