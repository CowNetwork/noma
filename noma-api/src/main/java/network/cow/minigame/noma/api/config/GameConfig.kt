package network.cow.minigame.noma.api.config

/**
 * @author Benedikt Wüller
 */
data class GameConfig<T>(val maxPlayers: Int, val actorProvider: ActorProviderConfig<T>)
