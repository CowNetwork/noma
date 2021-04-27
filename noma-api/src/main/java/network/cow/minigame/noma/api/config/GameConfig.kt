package network.cow.minigame.noma.api.config

/**
 * @author Benedikt Wüller
 */
data class GameConfig<PlayerType : Any>(
    val maxPlayers: Int,
    val actorProvider: ActorProviderConfig<PlayerType>,
    val workingDirectory: String,
    val options: Map<String, Any>
)
