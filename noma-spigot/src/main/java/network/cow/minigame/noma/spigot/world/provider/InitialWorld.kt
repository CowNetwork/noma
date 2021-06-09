package network.cow.minigame.noma.spigot.world.provider

import network.cow.minigame.noma.spigot.SpigotActor
import network.cow.minigame.noma.spigot.SpigotGame
import network.cow.minigame.noma.spigot.config.WorldProviderConfig
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

/**
 * @author Benedikt Wüller
 */
class InitialWorld(game: SpigotGame, config: WorldProviderConfig) : WorldProvider(game, config) {

    override fun selectWorld(): World = Bukkit.getWorlds().first()

    override fun getSpawnLocations(actor: SpigotActor?): List<Location> = listOf(Bukkit.getWorlds().first().spawnLocation)

}
