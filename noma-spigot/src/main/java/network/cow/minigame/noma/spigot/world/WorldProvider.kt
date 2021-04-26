package network.cow.minigame.noma.spigot.world

import network.cow.minigame.noma.api.SelectionMethod
import network.cow.minigame.noma.spigot.SpigotActor
import network.cow.minigame.noma.spigot.SpigotGame
import network.cow.minigame.noma.spigot.config.WorldProviderConfig
import org.bukkit.Location
import org.bukkit.World

/**
 * @author Benedikt Wüller
 */
abstract class WorldProvider(protected val game: SpigotGame, val config: WorldProviderConfig) {

    abstract fun selectWorld() : World

    abstract fun getSpawnLocations(actor: SpigotActor? = null) : List<Location>

    open fun getSpawnLocation(actor: SpigotActor? = null, selectionMethod: SelectionMethod = SelectionMethod.ORDERED) : Location = when (selectionMethod) {
        SelectionMethod.RANDOM -> this.getSpawnLocations(actor).random()
        SelectionMethod.ORDERED -> this.getSpawnLocations(actor).first()
    }

    open fun getSpectatorSpawnLocation(selectionMethod: SelectionMethod = SelectionMethod.ORDERED) : Location = this.getSpawnLocation(null, selectionMethod)

}
