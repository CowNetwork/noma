package network.cow.minigame.noma.spigot.phase

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.config.PhaseTimeoutConfig
import network.cow.minigame.noma.spigot.world.InitialWorldProvider
import org.bukkit.entity.Player

/**
 * @author Benedikt Wüller
 */
open class EndPhase(game: Game<Player>, config: PhaseConfig<Player>) : SpigotPhase(game, PhaseConfig(
        config.key, config.kind, false, config.requiresActors, config.phaseEndCountdown,
        PhaseTimeoutConfig(0, true), config.storeMiddleware, populateSpigotOptions(config.options.toMutableMap())
)) {

    companion object {
        const val STORE_KEY_WINNERS = "winners"
    }

    override fun onStart() = Unit
    override fun onTimeout() = Unit
    override fun onStop() = Unit
    override fun onPlayerJoin(player: Player) = Unit
    override fun onPlayerLeave(player: Player) = Unit
}

fun populateSpigotOptions(options: MutableMap<String, Any>) : Map<String, Any> {
    val teleport = ((options["teleport"] as Map<String, Any>?)?.toMutableMap() ?: mutableMapOf())
    teleport["onStart"] = true
    options["teleport"] = teleport

    val worldProvider = ((options["worldProvider"] as Map<String, Any>?)?.toMutableMap() ?: mutableMapOf())
    worldProvider["kind"] = worldProvider.getOrDefault("kind", InitialWorldProvider::class.java.name)
    options["worldProvider"] = worldProvider

    return options
}
