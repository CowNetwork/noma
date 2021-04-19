package network.cow.minigame.noma.spigot.actor

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.ActorProviderConfig
import network.cow.minigame.noma.spigot.SpigotActor
import org.bukkit.entity.Player

/**
 * @author Benedikt Wüller
 */
class OneToOneActorProvider(game: Game<Player>, config: ActorProviderConfig<Player>) : SpigotActorProvider(game, config) {

    override fun selectActor(player: Player) = SpigotActor(player.name)

}
