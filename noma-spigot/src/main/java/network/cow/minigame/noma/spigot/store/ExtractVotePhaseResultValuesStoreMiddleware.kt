package network.cow.minigame.noma.spigot.store

import network.cow.minigame.noma.api.config.StoreMiddlewareConfig
import network.cow.minigame.noma.api.phase.Phase
import network.cow.minigame.noma.api.store.Store
import network.cow.minigame.noma.api.store.StoreMiddleware
import network.cow.minigame.noma.spigot.phase.VotePhase

/**
 * @author Benedikt Wüller
 */
class ExtractVotePhaseResultValuesStoreMiddleware(phase: Phase<*, *>, store: Store, config: StoreMiddlewareConfig) : StoreMiddleware(phase, store, config) {

    override fun transformValue(phase: Phase<*, *>, value: Any?): Any? {
        return when (value) {
            is VotePhase.Result<*> -> value.items.mapNotNull { it.value }
            else -> value
        }
    }

    override fun transformKey(phase: Phase<*, *>, key: String): String = key

}
