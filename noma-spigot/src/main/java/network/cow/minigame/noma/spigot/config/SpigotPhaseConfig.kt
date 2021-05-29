package network.cow.minigame.noma.spigot.config

import network.cow.minigame.noma.api.SelectionMethod

/**
 * @author Benedikt Wüller
 */
data class SpigotPhaseConfig(
        var allowSpectators: Boolean,
        var teleportOnStart: Boolean,
        var teleportSelectionMethod: SelectionMethod,
        val worldProvider: WorldProviderConfig
)
