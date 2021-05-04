package network.cow.minigame.noma.spigot.phase

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import network.cow.messages.adventure.CascadeType
import network.cow.messages.adventure.cascadeColor
import network.cow.messages.adventure.comp
import network.cow.messages.adventure.corporate
import network.cow.messages.adventure.gradient
import network.cow.messages.adventure.highlight
import network.cow.messages.adventure.info
import network.cow.messages.adventure.prefix
import network.cow.messages.adventure.translate
import network.cow.messages.adventure.translateToComponent
import network.cow.messages.core.Gradients
import network.cow.messages.spigot.sendTranslatedError
import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.SelectionMethod
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.pool.Pool
import network.cow.minigame.noma.spigot.NomaPlugin
import network.cow.minigame.noma.spigot.SpigotTranslations
import network.cow.minigame.noma.spigot.pool.SpigotPool
import network.cow.spigot.extensions.ItemBuilder
import network.cow.spigot.extensions.state.clearState
import network.cow.spigot.extensions.state.getState
import network.cow.spigot.extensions.state.setState
import network.cow.spigot.inventory.InventoryItem
import network.cow.spigot.inventory.InventoryMenu
import network.cow.spigot.inventory.PagedInventoryMenu
import network.cow.spigot.inventory.withAction
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * @author Benedikt Wüller
 */

private const val STATE_KEY_VOTE_ITEM_PREFIX = "vote_phase_item"

open class VotePhase(game: Game<Player>, config: PhaseConfig<Player>) : SpigotPhase(game, config) {

    private val interactListener = InteractListener()

    private val voteables: List<Voteable<*>>

    init {
        val votableMaps = this.config.options["voteables"] as List<Map<String, Any>>? ?: emptyList()
        this.voteables = votableMaps.map { map ->
            val poolKey = map["pool"]?.toString() ?: error("The option 'phases.*.voteables.*.pool' is missing.")
            val pool = this.game.getPool(poolKey)

            val options = map.getOrDefault("options", Int.MAX_VALUE) as Int
            val votesPerPlayer = minOf(map.getOrDefault("votesPerPlayer", 1) as Int, options)
            val selectionMethod = SelectionMethod.valueOf(map.getOrDefault("optionSelectionMethod", SelectionMethod.RANDOM.name).toString())

            val storeKey = map.getOrDefault("storeKey", "${this.config.key}.$poolKey").toString()
            val slot = map.getOrDefault("slot", -1) as Int

            Voteable(pool, votesPerPlayer, options, selectionMethod, storeKey, slot)
        }
    }

    private fun updateVoteItems(player: Player) = this.voteables.forEach { it.updateVoteItem(player) }

    override fun onStart() = this.game.getPlayers().forEach(this::updateVoteItems)

    override fun onStop() {
        this.voteables.forEach { votable ->
            val votes = mutableMapOf<String, Int>()
            votable.items.forEachIndexed { index, item -> votes[item] = votable.getVotes(index) }

            this.game.getPlayers().forEach { player -> this.clear(player, votable) }

            this.storeMiddleware.store(votable.storeKey, Result(votes.keys
                .map { ResultItem(it, votable.pool.getItem(it), votes[it] ?: 0) }
                .shuffled()
                .sortedByDescending { it.votes }
                .take(votable.options)))
        }
    }

    override fun onPlayerJoin(player: Player) = this.updateVoteItems(player)

    override fun onPlayerLeave(player: Player) = this.voteables.forEach { this.clear(player, it) }

    private fun clear(player: Player, voteable: Voteable<*>) {
        val itemKey = "$STATE_KEY_VOTE_ITEM_PREFIX.${voteable.id}"
        val currentItem = player.getState<ItemStack>(NomaPlugin::class.java, itemKey)
        currentItem?.let { player.inventory.removeItem(currentItem) }
        player.clearState(NomaPlugin::class.java, itemKey)
        voteable.votes.remove(player)
        voteable.inventories.remove(player)
    }

    override fun onTimeout() = Unit

    override fun getListeners(): Set<Listener> = setOf(this.interactListener)

    inner class InteractListener : Listener {
        @EventHandler
        private fun onPlayerInteract(event: PlayerInteractEvent) {
            val player = event.player
            this@VotePhase.voteables.forEach {
                val itemKey = "$STATE_KEY_VOTE_ITEM_PREFIX.${it.id}"
                val item = player.getState<ItemStack>(NomaPlugin::class.java, itemKey) ?: return@forEach
                if (!player.inventory.itemInMainHand.isSimilar(item)) return@forEach
                it.inventories[player]?.open()
                event.setUseItemInHand(Event.Result.DENY)
            }
        }
    }

    data class Result<T>(val items: List<ResultItem<T>>)

    data class ResultItem<T>(val key: String, val value: T, val votes: Int)

}

class Voteable<T : Any>(val pool: Pool<Player, T>, val votesPerPlayer: Int, val options: Int, selectionMethod: SelectionMethod, val storeKey: String, val slot: Int = -1) {

    val id: UUID = UUID.randomUUID()

    val votes = mutableMapOf<Player, MutableList<Int>>()

    val inventories = mutableMapOf<Player, InventoryMenu>()

    val items = when (selectionMethod) {
        SelectionMethod.RANDOM -> this.pool.getKeys().shuffled()
        SelectionMethod.ORDERED -> this.pool.getKeys()
    }.take(this.options).sorted()

    fun getVotes(index: Int) : Int = this.votes.count { index in it.value }

    fun getVotes(item: String) : Int = this.getVotes(this.items.indexOf(item))

    fun updateVoteItem(player: Player) {
        val itemKey = "$STATE_KEY_VOTE_ITEM_PREFIX.$id"

        val name = this.pool.config.options.getOrDefault("name", this.pool.config.key).toString()
        val votesLeft = this.votesPerPlayer - this.votes.getOrPut(player) { mutableListOf() }.size

        val currentItem = player.getState<ItemStack>(NomaPlugin::class.java, itemKey)
        currentItem?.let { player.inventory.removeItem(it) }

        val prefix = SpigotTranslations.PHASE_VOTE_TITLE.translate(player).gradient(Gradients.MINIGAME) as TextComponent
        val nameComponent = name.translate(player).highlight().prefix(prefix).decoration(TextDecoration.ITALIC, false)
        val item = ItemBuilder(Material.NAME_TAG)
            .name(nameComponent)
            .lore(SpigotTranslations.PHASE_VOTE_VOTES_LEFT.translateToComponent(
                player,
                votesLeft.toString().highlight(),
                this.votesPerPlayer.toString().comp()).info().decoration(TextDecoration.ITALIC, false)
            )
            .build()

        player.setState(NomaPlugin::class.java, itemKey, item)

        when {
            this.slot < 0 -> player.inventory.addItem(item)
            else -> player.inventory.setItem(this.slot, item)
        }

        this.inventories.getOrPut(player) {
            VoteInventoryMenu(player, nameComponent.cascadeColor(NamedTextColor.DARK_GRAY, CascadeType.DEEP_ALL))
        }.update {
            item(4, item)
        }
    }

    inner class VoteInventoryMenu(owner: Player, title: Component) : PagedInventoryMenu(owner, 5 * 9, title) {

        override fun getItemCount(): Int = minOf(this@Voteable.options, this@Voteable.items.size)

        override fun getItem(player: Player, index: Int): InventoryItem {
            val item = this@Voteable.items[index]
            val builder = when (val pool = this@Voteable.pool) {
                is SpigotPool<*> -> ItemBuilder(pool.getDisplayItem(player, item))
                else -> ItemBuilder(Material.PAPER).name(item.corporate())
            }

            val itemIndex = this@Voteable.items.indexOf(item)
            val votes = this@Voteable.getVotes(itemIndex)
            builder.amount(maxOf(votes, 1))

            val isVoted = this@Voteable.votes[player]?.contains(itemIndex) == true

            val votesComponent = SpigotTranslations.PHASE_VOTE_VOTES.translateToComponent(player, votes.toString().highlight()).info()

            if (isVoted) {
                builder.glow()
                builder.lore(
                        votesComponent,
                        Component.empty(),
                        SpigotTranslations.PHASE_VOTE_CLICK_TO_UNVOTE.translateToComponent(player).info()
                )
            } else {
                builder.lore(
                        votesComponent,
                        Component.empty(),
                        SpigotTranslations.PHASE_VOTE_CLICK_TO_VOTE.translateToComponent(player).info()
                )
            }

            return builder.build().withAction { _, _ ->
                val playerVotes = this@Voteable.votes.getOrPut(player) { mutableListOf() }
                if (isVoted) {
                    playerVotes.remove(itemIndex)
                    updateItem(index)
                    this@Voteable.updateVoteItem(player)
                    return@withAction true
                }

                if (playerVotes.size >= this@Voteable.votesPerPlayer) {
                    player.sendTranslatedError(SpigotTranslations.PHASE_VOTE_NO_VOTES_LEFT)
                    return@withAction false
                }

                playerVotes.add(itemIndex)
                updateItem(index)
                this@Voteable.updateVoteItem(player)
                return@withAction true
            }
        }
    }

}
