package network.cow.minigame.noma.api.store.middleware

import network.cow.minigame.noma.api.config.StoreMiddlewareConfig
import network.cow.minigame.noma.api.phase.Phase
import network.cow.minigame.noma.api.store.Store

/**
 * @author Benedikt Wüller
 */
abstract class StoreMiddleware(val phase: Phase<*, *>, val store: Store, val config: StoreMiddlewareConfig) {

    fun store(key: String, value: Any?) = this.store.set(
        this.transformKey(phase, key),
        this.transformValue(phase, value)
    )

    abstract fun transformValue(phase: Phase<*, *>, value: Any?) : Any?

    abstract fun transformKey(phase: Phase<*, *>, key: String) : String

}
