package network.cow.minigame.noma.spigot.world.provider

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
class StoredWorld(game: SpigotGame, config: WorldProviderConfig) : WorldProvider(game, config) {

    private val storeKey = this.config.options["storeKey"]?.toString() ?: error("No store key has been defined for 'phases.*.worldProvider.storeKey'.")

    private lateinit var worldMeta: WorldMeta
    private lateinit var world: World

    override fun selectWorld(): World {
        val value = this.game.store.get<Any>(storeKey) ?: error("No WorldMeta exists for store key $storeKey.")
        this.worldMeta = when (value) {
            is WorldMeta -> value
            is List<*> -> {
                val item = value.firstOrNull { it is WorldMeta } ?: error("The list for store key $storeKey does not contain any WorldMeta item.")
                item as WorldMeta
            }
            is VotePhase.Result<*> -> {
                val item = value.items.firstOrNull { it.value is WorldMeta } ?.value ?: error("The vote phase result for store key $storeKey does not contain any WorldMeta item.")
                item as WorldMeta
            }
            else -> error("Unsupported store value type ${value.javaClass.name}.")
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
