package network.cow.minigame.noma.spigot.actor

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.actor.Actor
import network.cow.minigame.noma.api.config.ActorProviderConfig
import network.cow.minigame.noma.spigot.SpigotActor
import org.bukkit.entity.Player
import java.awt.Color
import java.util.UUID

/**
 * @author Benedikt Wüller
 */
class TeamActorProvider(game: Game<Player>, config: ActorProviderConfig<Player>) : SpigotActorProvider(game, config) {

    private val maxPlayersPerTeam: Int

    private val actors = mutableListOf<Actor<Player>>()

    init {
        val teams = this.config.options["teams"]
        if (teams == null || teams !is List<*>) error("The option 'game.actorProvider.teams' must be provided.")
        if (teams.isEmpty()) error("The option 'game.actorProvider.teams' must contain at least one team.")

        teams.forEach {
            val team = it as Map<*, *>

            val key = team["key"]?.toString() ?: UUID.randomUUID().toString()
            val name = team["name"]?.toString()
            if (name == null || name.isEmpty()) error("The option 'game.actorProviders.teams.*.name' must be provided.")

            val color = team.getOrDefault("color", "#FFFFFF").toString().toColor()
            this.actors.add(SpigotActor(key, name, color, this.config.options.getOrDefault("showPrefix", false) as Boolean))
        }

        this.maxPlayersPerTeam = this.config.options.getOrDefault(
            "maxPlayersPerTeam",
            this.game.config.maxPlayers / this.actors.size
        ).toString().toInt()

        if (this.maxPlayersPerTeam * this.actors.size < this.game.config.maxPlayers) {
            error("There are not enough 'game.actorProviders.teams' items and/or 'game.actorProviders.maxPlayersPerTeam' for 'game.maxPlayers'.")
        }
    }

    override fun selectActor(player: Player): Actor<Player> {
        val actors = this.actors.filter { it.size < this.maxPlayersPerTeam }.shuffled()
        return actors.minByOrNull { it.size } ?: error("No actor with less than 'maxPlayersPerTeam' ($maxPlayersPerTeam) could be found.")
    }

    private fun String.toColor() = Color(
        this.substring(1, 3).toInt(16),
        this.substring(3, 5).toInt(16),
        this.substring(5, 7).toInt(16)
    )

}
