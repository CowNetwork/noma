package network.cow.minigame.noma.spigot

import network.cow.minigame.noma.api.CountdownTimer
import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.GameConfig
import network.cow.minigame.noma.api.config.PhaseConfig
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Team

/**
 * @author Benedikt Wüller
 */
open class SpigotGame(config: GameConfig<Player>, phaseConfigs: List<PhaseConfig<Player>>) : Game<Player>(config, phaseConfigs), Listener {

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

    fun getScoreboardTeam(player: Player) : Team? {
        val actor = this.getActor(player) ?: return null
        if (actor !is SpigotActor) return null
        return actor.scoreboardTeam
    }

    fun getScoreboardTeams() = this.getActors().filterIsInstance<SpigotActor>().map { it.scoreboardTeam }

    override fun createCountdownTimer(duration: Long): CountdownTimer = SpigotCountdownTimer(duration)

}
