package network.cow.minigame.noma.spigot

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.GameConfig
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

/**
 * @author Benedikt Wüller
 */
open class SpigotGame(config: GameConfig<Player>) : Game<Player>(config), Listener {

    @EventHandler
    private fun onPlayerLeave(event: PlayerQuitEvent) {
        this.actorProvider.removePlayer(event.player)
    }

}
