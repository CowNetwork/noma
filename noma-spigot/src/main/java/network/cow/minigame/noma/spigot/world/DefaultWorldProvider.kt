package network.cow.minigame.noma.spigot.world

import network.cow.minigame.noma.spigot.SpigotActor
import network.cow.minigame.noma.spigot.SpigotGame
import network.cow.minigame.noma.spigot.config.WorldProviderConfig
import org.bukkit.Location
import org.bukkit.World

/**
 * @author Benedikt Wüller
 */
class DefaultWorldProvider(game: SpigotGame) : WorldProvider(game, WorldProviderConfig(DefaultWorldProvider::class.java, emptyMap())) {

    override fun selectWorld(): World = this.game.world

    override fun getSpawnLocations(actor: SpigotActor?): List<Location> = listOf(this.game.world.spawnLocation)

}
