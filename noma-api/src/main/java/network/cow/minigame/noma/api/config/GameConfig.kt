package network.cow.minigame.noma.api.config

import network.cow.minigame.noma.api.Game

/**
 * @author Benedikt Wüller
 */
data class GameConfig<PlayerType : Any, GameType : Game<PlayerType, GameType>>(
    var minPlayers: Int,
    var maxPlayers: Int,
    var minActors: Int,
    val actorProvider: ActorProviderConfig<PlayerType, GameType>,
    val workingDirectory: String,
    val options: Map<String, Any>
)
