package network.cow.minigame.noma.spigot.phase

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.Translations
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.config.PhaseTimeoutConfig
import network.cow.minigame.noma.spigot.SpigotCountdownTimer
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author Benedikt Wüller
 */
class LobbyVotePhase(game: Game<Player>, config: PhaseConfig<Player>) : VotePhase(game, PhaseConfig(
    config.key, config.kind, allowsNewPlayers = true, requiresActors = false,
    config.phaseEndCountdown, PhaseTimeoutConfig(Long.MAX_VALUE, true),
    config.storeMiddleware, config.options
)) {

    private val timeoutCountdown = this.createLobbyTimeoutCountdown(this.game, this::tick)

    private val startCountdown = SpigotCountdownTimer(this.config.phaseEndCountdown.duration, Translations.COUNTDOWN_MESSAGE_GAME_START)
            .onDone { this.game.nextPhase(true) }
            .onTick { this.tick() }

    private fun tick() {
        val playerCount = Bukkit.getOnlinePlayers().size
        if (playerCount < this.game.config.minPlayers) {
            this.timeoutCountdown.start()
            this.startCountdown.reset()
        } else {
            this.timeoutCountdown.reset()
            this.startCountdown.start()
        }
    }

    override fun onStart() {
        super.onStart()
        this.timeoutCountdown.start()
    }

    override fun onStop() {
        super.onStop()
        this.timeoutCountdown.reset()
        this.startCountdown.reset()
    }

    override fun onTimeout() = Bukkit.shutdown()

}
