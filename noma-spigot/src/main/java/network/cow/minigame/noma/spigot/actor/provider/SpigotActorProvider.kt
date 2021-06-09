package network.cow.minigame.noma.spigot.actor.provider

import network.cow.minigame.noma.api.actor.provider.ActorProvider
import network.cow.minigame.noma.api.config.ActorProviderConfig
import network.cow.minigame.noma.spigot.SpigotGame
import org.bukkit.entity.Player

/**
 * @author Benedikt Wüller
 */
abstract class SpigotActorProvider(game: SpigotGame, config: ActorProviderConfig<Player, SpigotGame>) : ActorProvider<Player, SpigotGame>(game, config)
