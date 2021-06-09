package network.cow.minigame.noma.api.config

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.actor.provider.ActorProvider

/**
 * @author Benedikt Wüller
 */
data class ActorProviderConfig<PlayerType : Any, GameType : Game<PlayerType, GameType>>(val kind: Class<out ActorProvider<PlayerType, GameType>>, val options: Map<String, Any>)
