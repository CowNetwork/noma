package network.cow.minigame.noma.api.state

import network.cow.minigame.noma.api.config.StoreMiddlewareConfig
import network.cow.minigame.noma.api.phase.Phase

/**
 * @author Benedikt Wüller
 */
class DefaultStoreMiddleware(phase: Phase<*>, store: Store, config: StoreMiddlewareConfig) : StoreMiddleware(phase, store, config) {

    override fun transformValue(phase: Phase<*>, value: Any?) = value

    override fun transformKey(phase: Phase<*>, key: String) = config.options.getOrDefault("storeKey", key).toString()

}
