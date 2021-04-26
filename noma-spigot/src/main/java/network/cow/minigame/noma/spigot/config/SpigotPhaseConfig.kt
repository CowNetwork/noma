package network.cow.minigame.noma.spigot.config

import network.cow.minigame.noma.api.SelectionMethod

/**
 * @author Benedikt Wüller
 */
data class SpigotPhaseConfig(val teleportOnStart: Boolean, val teleportSelectionMethod: SelectionMethod, val worldProvider: WorldProviderConfig)
