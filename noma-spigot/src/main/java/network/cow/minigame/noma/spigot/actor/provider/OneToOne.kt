package network.cow.minigame.noma.spigot.actor.provider

import network.cow.minigame.noma.api.config.ActorProviderConfig
import network.cow.minigame.noma.spigot.SpigotActor
import network.cow.minigame.noma.spigot.SpigotGame
import org.bukkit.entity.Player

/**
 * @author Benedikt Wüller
 */
class OneToOne(game: SpigotGame, config: ActorProviderConfig<Player, SpigotGame>) : SpigotActorProvider(game, config) {

    override fun selectActor(player: Player) = SpigotActor(player.name)

}
