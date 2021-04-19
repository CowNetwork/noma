package network.cow.minigame.noma.api.config

import network.cow.minigame.noma.api.actor.ActorProvider

/**
 * @author Benedikt Wüller
 */
data class ActorProviderConfig<T>(val kind: Class<ActorProvider<T>>, val options: Map<String, Any>)
