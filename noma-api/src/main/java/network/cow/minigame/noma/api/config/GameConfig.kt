package network.cow.minigame.noma.api.config

/**
 * @author Benedikt Wüller
 */
data class GameConfig<PlayerType : Any>(
    val minPlayers: Int,
    val maxPlayers: Int,
    val minActors: Int,
    val actorProvider: ActorProviderConfig<PlayerType>,
    val workingDirectory: String,
    val options: Map<String, Any>
)
