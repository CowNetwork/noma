package network.cow.minigame.noma.spigot

import network.cow.minigame.noma.api.CountdownTimer
import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.GameConfig
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.spigot.phase.SpigotPhase
import network.cow.minigame.noma.spigot.world.DefaultWorldProvider
import network.cow.minigame.noma.spigot.world.WorldProvider
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scoreboard.Team

/**
 * @author Benedikt Wüller
 */
open class SpigotGame(config: GameConfig<Player>, phaseConfigs: List<PhaseConfig<Player>>) : Game<Player>(config, phaseConfigs), Listener {

    var world: World = Bukkit.getWorlds().first(); private set
    var worldProvider: WorldProvider = DefaultWorldProvider(this); private set

    init {
        Bukkit.getPluginManager().registerEvents(this, NomaPlugin.INSTANCE)
    }

    override fun getNextPhaseKey(): String? {
        // TODO: use events to allow key override
        return super.getNextPhaseKey()
    }

    override fun onStop() {
        HandlerList.unregisterAll(this)
        Bukkit.shutdown()
    }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val phase = this.getCurrentPhase()
        val player = event.player

        if (!phase.config.allowsNewPlayers || (this.config.maxPlayers >= 0 && this.getPlayers().size >= this.config.maxPlayers)) {
            player.gameMode = GameMode.SPECTATOR

            // Teleport spectator to current world.
            val method = if (phase is SpigotPhase) phase.spigotConfig.teleportSelectionMethod else SelectionMethod.ORDERED
            player.teleport(this.worldProvider.getSpectatorSpawnLocation(method))
            return
        }

        if (phase.config.requiresActors) {
            this.actorProvider.addPlayer(player)
            phase.join(player)
        }
    }

    @EventHandler
    private fun onPlayerLeave(event: PlayerQuitEvent) {
        val player = event.player
        this.actorProvider.removePlayer(player)
        this.getCurrentPhase().leave(player)
    }

    fun getSpigotActor(player: Player) : SpigotActor? {
        val actor = this.getActor(player) ?: return null
        if (actor !is SpigotActor) return null
        return actor
    }

    fun getSpigotActors() = this.getActors().filterIsInstance<SpigotActor>()

    fun getScoreboardTeam(player: Player) : Team? = this.getSpigotActor(player)?.scoreboardTeam

    fun getScoreboardTeams() = this.getActors().filterIsInstance<SpigotActor>().map { it.scoreboardTeam }

    override fun createCountdownTimer(duration: Long): CountdownTimer = SpigotCountdownTimer(duration)

}
