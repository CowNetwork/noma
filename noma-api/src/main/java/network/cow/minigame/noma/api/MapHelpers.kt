package network.cow.minigame.noma.api

/**
 * @author Benedikt Wüller
 */

fun <T : Any> Map<String, Any>.get(key: String, default: T) : T {
    val parts = key.split(".")
    var map = this
    parts.forEachIndexed { index, key ->
        if (index == parts.lastIndex) return map[key] as T? ?: default
        map = map[key] as Map<String, Any>? ?: emptyMap()
    }
    return default
}
