package network.cow.minigame.noma.spigot.phase

import network.cow.messages.adventure.highlight
import network.cow.messages.spigot.broadcastTranslatedInfo
import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.Translations
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.config.PhaseTimeoutConfig
import network.cow.minigame.noma.spigot.SpigotCountdownTimer
import network.cow.minigame.noma.spigot.SpigotTranslations
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

    private val maxWaitDuration = (this.config.options.getOrDefault("maxWaitDuration", 5 * 60) as Int).toLong()

    private val timeoutCountdown = SpigotCountdownTimer(this.maxWaitDuration).silent()
        .onDone(this::timeout)
        .onTick {
            if (it % 30 != 0L) return@onTick

            val missingPlayers = this.game.config.minPlayers - this.game.getPlayers().size
            if (missingPlayers <= 0) return@onTick

            val key = when (missingPlayers) {
                1 -> SpigotTranslations.PHASE_LOBBY_WAITING_FOR_PLAYER
                else -> SpigotTranslations.PHASE_LOBBY_WAITING_FOR_PLAYERS
            }

            Bukkit.getServer().broadcastTranslatedInfo(key, missingPlayers.toString().highlight())
        }

    private val startCountdown = SpigotCountdownTimer(this.config.phaseEndCountdown.duration, Translations.COUNTDOWN_MESSAGE_GAME_START).onDone(this::stop)

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
