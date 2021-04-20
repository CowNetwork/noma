package network.cow.minigame.noma.spigot.config

import network.cow.minigame.noma.spigot.world.WorldProvider

/**
 * @author Benedikt Wüller
 */
data class WorldProviderConfig(val kind: Class<out WorldProvider>, val options: Map<String, Any>)
