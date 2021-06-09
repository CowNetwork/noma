package network.cow.minigame.noma.api.store

/**
 * @author Benedikt Wüller
 */
class Store {

    private val state = mutableMapOf<String, Any>()

    fun set(key: String, value: Any?) = when (value) {
        null -> this.state.remove(key)
        else -> this.state[key] = value
    }

    fun <T : Any> get(key: String, default: T? = null) : T? = this.state[key] as T? ?: default

    fun toMap() = this.state.toMap()

}
