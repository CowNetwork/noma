package network.cow.minigame.noma.api.store.middleware

import network.cow.minigame.noma.api.config.StoreMiddlewareConfig
import network.cow.minigame.noma.api.phase.Phase
import network.cow.minigame.noma.api.store.Store

/**
 * @author Benedikt Wüller
 */
class Default(phase: Phase<*, *>, store: Store, config: StoreMiddlewareConfig) : StoreMiddleware(phase, store, config) {

    override fun transformValue(phase: Phase<*, *>, value: Any?) = value

    override fun transformKey(phase: Phase<*, *>, key: String) = config.options.getOrDefault("storeKey", key).toString()

}
