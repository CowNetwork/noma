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
class VotedWorldProvider(game: SpigotGame, config: WorldProviderConfig) : WorldProvider(game, config) {

    private val votePhase: VotePhase<WorldMeta>
    private lateinit var worldMeta: WorldMeta
    private lateinit var world: World

    init {
        val votePhaseKey = this.config.options["fromPhase"]?.toString() ?: error("No phase key has been defined in 'phases.*.worldProvider.fromPhase'.")
        val phase = this.game.getPhase(votePhaseKey)

        if (phase !is VotePhase<*>) error("The phase '$votePhaseKey' (defined in 'phases.*.worldProvider.fromPhase') is not of type ${VotePhase::class.java.name}.")
        this.votePhase = this.game.getPhase(votePhaseKey) as VotePhase<WorldMeta>
    }

    override fun selectWorld(): World {
        this.worldMeta = this.votePhase.getResult().items.first().item

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
