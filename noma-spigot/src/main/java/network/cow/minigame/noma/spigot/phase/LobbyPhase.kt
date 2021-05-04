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
open class LobbyPhase(game: Game<Player>, config: PhaseConfig<Player>) : SpigotPhase(game, PhaseConfig(
    config.key, config.kind, allowsNewPlayers = true, requiresActors = false,
    config.phaseEndCountdown, PhaseTimeoutConfig(Long.MAX_VALUE, true),
    config.storeMiddleware, config.options.toMutableMap().apply { this["allowsSpectators"] = false }
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
        this.timeoutCountdown.start()
    }

    override fun onStop() {
        this.timeoutCountdown.reset()
        this.startCountdown.reset()
    }

    override fun onPlayerJoin(player: Player) = Unit
    override fun onPlayerLeave(player: Player) = Unit

    override fun onTimeout() = Bukkit.shutdown()

}

fun SpigotPhase.createLobbyTimeoutCountdown(game: Game<*>, tick: () -> Unit) = SpigotCountdownTimer(this.config.timeout.duration).silent()
        .onDone(this::timeout)
        .onTick {
            tick()

            if (it % 30 != 0L) return@onTick

            val missingPlayers = game.config.minPlayers - Bukkit.getOnlinePlayers().size
            if (missingPlayers <= 0) return@onTick

            val key = when (missingPlayers) {
                1 -> SpigotTranslations.PHASE_LOBBY_WAITING_FOR_PLAYER
                else -> SpigotTranslations.PHASE_LOBBY_WAITING_FOR_PLAYERS
            }

            Bukkit.getServer().broadcastTranslatedInfo(key, missingPlayers.toString().highlight())
        }
