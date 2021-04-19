package network.cow.minigame.noma.spigot.actor

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.actor.ActorProvider
import network.cow.minigame.noma.api.config.ActorProviderConfig
import org.bukkit.entity.Player

/**
 * @author Benedikt Wüller
 */
abstract class SpigotActorProvider(game: Game<Player>, config: ActorProviderConfig<Player>) : ActorProvider<Player>(game, config)
