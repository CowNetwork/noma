package network.cow.minigame.noma.example.spigot.phase

import network.cow.messages.adventure.formatToComponent
import network.cow.messages.adventure.highlight
import network.cow.messages.spigot.broadcastInfo
import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.phase.EmptyPhaseResult
import network.cow.minigame.noma.spigot.phase.SpigotPhase
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author Benedikt Wüller
 */
class DummyPhase(game: Game<Player>, config: PhaseConfig<Player>) : SpigotPhase<EmptyPhaseResult>(game, config) {

    override fun onStart() {
        println("phase onStart")
    }

    override fun onTimeout() {
        println("phase onTimeout")
    }

    override fun onStop(): EmptyPhaseResult {
        println("phase onStop")
        return EmptyPhaseResult()
    }

    override fun onPlayerJoin(player: Player) {
        val format = "%1\$s joined the server."
        Bukkit.getServer().broadcastInfo(format.formatToComponent(player.displayName().highlight()))
        println("phase onPlayerJoin")
    }

    override fun onPlayerLeave(player: Player) {
        println("phase onPlayerLeave")
    }

}
