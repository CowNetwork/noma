package network.cow.minigame.noma.api.config

import network.cow.minigame.noma.api.actor.ActorProvider

/**
 * @author Benedikt Wüller
 */
data class ActorProviderConfig<PlayerType : Any>(val kind: Class<out ActorProvider<PlayerType>>, val options: Map<String, Any>)
