package network.cow.minigame.noma.spigot.pool

import network.cow.messages.adventure.corporate
import network.cow.minigame.noma.api.config.PoolConfig
import network.cow.minigame.noma.api.pool.Pool
import network.cow.minigame.noma.spigot.SpigotGame
import network.cow.spigot.extensions.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author Benedikt Wüller
 */
abstract class SpigotPool<ItemType : Any>(game: SpigotGame, config: PoolConfig<Player>) : Pool<Player, SpigotGame, ItemType>(game, config) {

    open fun getDisplayItem(player: Player, key: String) = ItemBuilder(Material.PAPER).name(key.corporate()).build()

}
