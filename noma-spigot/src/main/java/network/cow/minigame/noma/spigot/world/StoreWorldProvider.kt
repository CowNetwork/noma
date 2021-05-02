package network.cow.minigame.noma.spigot.world

import network.cow.minigame.noma.spigot.SpigotActor
import network.cow.minigame.noma.spigot.SpigotGame
import network.cow.minigame.noma.spigot.config.WorldProviderConfig
import network.cow.minigame.noma.spigot.phase.VotePhase
import network.cow.minigame.noma.spigot.pool.WorldMeta
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import java.nio.file.Paths
import java.util.UUID

/**
 * @author Benedikt Wüller
 */
class StoreWorldProvider(game: SpigotGame, config: WorldProviderConfig) : WorldProvider(game, config) {

    private val storeKey = this.config.options["storeKey"]?.toString() ?: error("No store key has been defined for 'phases.*.worldProvider.storeKey'.")

    private lateinit var worldMeta: WorldMeta
    private lateinit var world: World

    override fun selectWorld(): World {
        val value = this.game.store.get<Any>(storeKey) ?: TODO("error")
        this.worldMeta = when (value) {
            is WorldMeta -> value
            is VotePhase.Result<*> -> {
                val item = value.items.firstOrNull()?.value ?: TODO("error")
                if (item !is WorldMeta) TODO("error")
                item
            }
            else -> TODO("error")
        }

        val targetName = UUID.randomUUID().toString()
        worldMeta.path.toFile().copyRecursively(Paths.get(this.game.config.workingDirectory, targetName).toFile(), overwrite = true)

        this.world = WorldCreator(targetName)
            .generateStructures(false)
            .environment(World.Environment.NORMAL)
            .createWorld()!!

        this.worldMeta.gameRules.forEach {
            this.world.setGameRule(it.key, it.value)
        }

        return this.world
    }

    override fun getSpawnLocations(actor: SpigotActor?): List<Location> {
        val locations = actor?.let { this.worldMeta.actorSpawnLocations[actor.key] } ?: this.worldMeta.globalSpawnLocations
        return locations.map { it.toLocation(this.world) }
    }

}
