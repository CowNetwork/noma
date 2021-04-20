package network.cow.minigame.noma.spigot.phase

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.phase.Phase
import network.cow.minigame.noma.spigot.SelectionMethod
import network.cow.minigame.noma.spigot.SpigotActor
import network.cow.minigame.noma.spigot.SpigotGame
import network.cow.minigame.noma.spigot.config.SpigotPhaseConfig
import network.cow.minigame.noma.spigot.config.WorldProviderConfig
import network.cow.minigame.noma.spigot.world.DefaultWorldProvider
import network.cow.minigame.noma.spigot.world.WorldProvider
import org.bukkit.entity.Player

/**
 * @author Benedikt Wüller
 */
abstract class SpigotPhase<ResultType : Any>(game: Game<Player>, config: PhaseConfig<Player>) : Phase<Player, ResultType>(game, config) {

    val spigotConfig: SpigotPhaseConfig

    init {
        val worldProviderMap = this.config.options.getOrElse("worldProvider") { emptyMap<String, Any>() } as Map<String, Any>
        val worldProviderConfig = WorldProviderConfig(
            Class.forName(worldProviderMap.getOrDefault("kind", DefaultWorldProvider::class.java.name).toString()) as Class<out WorldProvider>,
            worldProviderMap
        )

        this.spigotConfig = SpigotPhaseConfig(
            this.config.options.getOrDefault("teleportOnStart", false) as Boolean,
            SelectionMethod.valueOf(this.config.options.getOrDefault("teleportSelectionMethod", SelectionMethod.ORDERED.name).toString()),
            worldProviderConfig
        )
    }

    override fun onStart() {
        // Teleport players on start if requested.
        if (this.spigotConfig.teleportOnStart && this.game is SpigotGame) {
            this.game.getSpigotActors().forEach {
                val locations = this.game.worldProvider.getSpawnLocations(it).toTypedArray()
                it.teleport(this.spigotConfig.teleportSelectionMethod, *locations)
            }
        }
    }

    override fun join(player: Player) {
        // Teleport the player to the current map.
        if (this.game is SpigotGame) {
            val actor = this.game.getSpigotActor(player)
            if (actor is SpigotActor) {
                val location = this.game.worldProvider.getSpawnLocation(actor, this.spigotConfig.teleportSelectionMethod)
                player.teleport(location)
            }
        }

        super.join(player)
    }

}
