package network.cow.minigame.noma.api.config

/**
 * @author Benedikt Wüller
 */
data class GameConfig<PlayerType : Any>(
    var minPlayers: Int,
    var maxPlayers: Int,
    var minActors: Int,
    val actorProvider: ActorProviderConfig<PlayerType>,
    val workingDirectory: String,
    val options: Map<String, Any>
)
