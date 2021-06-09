package network.cow.minigame.noma.spigot

/**
 * @author Benedikt Wüller
 */

private val PACKAGE_REPLACEMENTS = mapOf(
    "noma/actorProviders/" to "network.cow.minigame.noma.api.actor.provider.",
    "noma/phases/" to "network.cow.minigame.noma.api.phase.",
    "noma/pools/" to "network.cow.minigame.noma.api.pool.",
    "noma/storeMiddlewares/" to "network.cow.minigame.noma.api.store.middleware.",
    "noma/spigot/actorProviders/" to "network.cow.minigame.noma.spigot.actor.provider.",
    "noma/spigot/phases/" to "network.cow.minigame.noma.spigot.phase.",
    "noma/spigot/pools/" to "network.cow.minigame.noma.spigot.pool.",
    "noma/spigot/storeMiddlewares/" to "network.cow.minigame.noma.spigot.store.middleware.",
    "noma/spigot/worldProviders/" to "network.cow.minigame.noma.spigot.world.provider.",
)

fun <T : Any> parseClass(value: String) : Class<out T> {
    val className = when (val replacement = PACKAGE_REPLACEMENTS.entries.firstOrNull { value.startsWith(it.key) }) {
        null -> value
        else -> value.replaceFirst(replacement.key, replacement.value)
    }
    return Class.forName(className) as Class<out T>
}
