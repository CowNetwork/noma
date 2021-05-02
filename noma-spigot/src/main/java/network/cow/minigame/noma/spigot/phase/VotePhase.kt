package network.cow.minigame.noma.spigot.phase

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import network.cow.messages.adventure.CascadeType
import network.cow.messages.adventure.cascadeColor
import network.cow.messages.adventure.comp
import network.cow.messages.adventure.corporate
import network.cow.messages.adventure.translateToComponent
import network.cow.messages.adventure.translate
import network.cow.messages.adventure.highlight
import network.cow.messages.adventure.gradient
import network.cow.messages.adventure.info
import network.cow.messages.adventure.prefix
import network.cow.messages.core.Gradients
import network.cow.messages.spigot.sendTranslatedError
import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.SelectionMethod
import network.cow.minigame.noma.api.Translations
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.pool.Pool
import network.cow.minigame.noma.spigot.NomaPlugin
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
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.awt.Color

/**
 * @author Benedikt Wüller
 */
open class VotePhase<T : Any>(game: Game<Player>, config: PhaseConfig<Player>) : SpigotPhase(game, config) {

    companion object {
        private const val STATE_KEY_VOTE_ITEM = "vote_phase_item"
    }

    private val votes = mutableMapOf<Player, MutableList<Int>>()
    private val inventories = mutableMapOf<Player, InventoryMenu>()

    private val pool: Pool<Player, T>

    val options: Int = this.config.options.getOrDefault("options", Int.MAX_VALUE) as Int
    val votesPerPlayer: Int = minOf(this.config.options.getOrDefault("votesPerPlayer", 1) as Int, this.options)
    val items: List<String>

    init {
        val key = this.config.options["pool"]?.toString() ?: error("The option 'phases.*.pool' is missing.")
        this.pool = this.game.getPool(key) as Pool<Player, T>

        val selectionMethod = SelectionMethod.valueOf(this.config.options.getOrDefault("optionSelectionMethod", SelectionMethod.RANDOM.name).toString())
        this.items = when (selectionMethod) {
            SelectionMethod.RANDOM -> this.pool.getKeys().shuffled()
            SelectionMethod.ORDERED -> this.pool.getKeys()
        }.take(this.options).sorted()
    }

    override fun onStart() {
        this.game.getPlayers().forEach(this::updateVoteItem)
    }

    override fun onPlayerJoin(player: Player) {
        this.updateVoteItem(player)
    }

    override fun onPlayerLeave(player: Player) {
        val currentItem = player.getState<ItemStack>(NomaPlugin::class.java, STATE_KEY_VOTE_ITEM)
        currentItem?.let { player.inventory.removeItem(currentItem) }
        player.clearState(NomaPlugin::class.java, STATE_KEY_VOTE_ITEM)
        this.votes.remove(player)
        this.inventories.remove(player)
    }

    fun getVotes(index: Int) : Int = this.votes.count { index in it.value }

    fun getVotes(item: String) : Int = this.getVotes(this.items.indexOf(item))

    fun updateVoteItem(player: Player) {
        val name = this.pool.config.options.getOrDefault("name", this.pool.config.key).toString()
        val votesLeft = this.votesPerPlayer - votes.getOrPut(player) { mutableListOf() }.size

        val currentItem = player.getState<ItemStack>(NomaPlugin::class.java, STATE_KEY_VOTE_ITEM)
        currentItem?.let { player.inventory.removeItem(it) }

        val prefix = Translations.PHASE_VOTE_TITLE.translate(player).gradient(Gradients.MINIGAME) as TextComponent
        val nameComponent = name.translate(player).highlight().prefix(prefix).decoration(TextDecoration.ITALIC, false)
        val item = ItemBuilder(Material.NAME_TAG)
            .name(nameComponent)
            .lore(Translations.PHASE_VOTE_VOTES_LEFT.translateToComponent(
                player,
                votesLeft.toString().highlight(),
                this.votesPerPlayer.toString().comp()).info().decoration(TextDecoration.ITALIC, false)
            )
            .build()

        player.setState(NomaPlugin::class.java, STATE_KEY_VOTE_ITEM, item)
        player.inventory.addItem(item)

        this.inventories.getOrPut(player) {
            VoteInventoryMenu(player, nameComponent.cascadeColor(NamedTextColor.DARK_GRAY, CascadeType.DEEP_ALL))
        }.update {
            item(4, item)
        }
    }

    override fun onStop() {
        // Return the items with the most votes.
        val votes = mutableMapOf<String, Int>()
        this.items.forEachIndexed { index, item -> votes[item] = this.getVotes(index) }

        this.game.getPlayers().forEach { player ->
            val currentItem = player.getState<ItemStack>(NomaPlugin::class.java, STATE_KEY_VOTE_ITEM)
            currentItem?.let { player.inventory.removeItem(it) }
            player.clearState(NomaPlugin::class.java, STATE_KEY_VOTE_ITEM)
            player.closeInventory()
        }

        this.storeMiddleware.store(this.config.key, Result(votes.keys
            .map { ResultItem(it, this.pool.getItem(it), votes[it] ?: 0) }
            .shuffled()
            .sortedByDescending { it.votes }
            .take(this.options)))
    }

    override fun onTimeout() = Unit

    @EventHandler
    private fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player

        val item = player.getState<ItemStack>(NomaPlugin::class.java, STATE_KEY_VOTE_ITEM) ?: return
        println(player.inventory.itemInMainHand.isSimilar(item))
        if (!player.inventory.itemInMainHand.isSimilar(item)) return

        this.inventories[player]?.open()
        event.setUseItemInHand(Event.Result.DENY)
    }

    data class Result<T>(val items: List<ResultItem<T>>)

    data class ResultItem<T>(val key: String, val value: T, val votes: Int)

    inner class VoteInventoryMenu(owner: Player, title: Component) : PagedInventoryMenu(owner, 5 * 9, title) {

        override fun getItemCount(): Int = minOf(this@VotePhase.options, this@VotePhase.items.size)

        override fun getItem(player: Player, index: Int): InventoryItem {
            val item = this@VotePhase.items[index]
            val builder = when (val pool = this@VotePhase.pool) {
                is SpigotPool<*> -> ItemBuilder(pool.getDisplayItem(player, item))
                else -> ItemBuilder(Material.PAPER).name(item.corporate())
            }

            val itemIndex = this@VotePhase.items.indexOf(item)
            val votes = this@VotePhase.getVotes(itemIndex)
            builder.amount(maxOf(votes, 1))

            val isVoted = this@VotePhase.votes[player]?.contains(itemIndex) == true

            val votesComponent = Translations.PHASE_VOTE_VOTES.translateToComponent(player, votes.toString().highlight()).info()

            if (isVoted) {
                builder.glow()
                builder.lore(
                    votesComponent,
                    Component.empty(),
                    Translations.PHASE_VOTE_CLICK_TO_UNVOTE.translateToComponent(player).info()
                )
            } else {
                builder.lore(
                    votesComponent,
                    Component.empty(),
                    Translations.PHASE_VOTE_CLICK_TO_VOTE.translateToComponent(player).info()
                )
            }

            return builder.build().withAction { _, _ ->
                val playerVotes = this@VotePhase.votes.getOrPut(player) { mutableListOf() }
                if (isVoted) {
                    playerVotes.remove(itemIndex)
                    updateItem(index)
                    updateVoteItem(player)
                    return@withAction true
                }

                if (playerVotes.size >= this@VotePhase.votesPerPlayer) {
                    player.sendTranslatedError(Translations.PHASE_VOTE_NO_VOTES_LEFT)
                    return@withAction false
                }

                playerVotes.add(itemIndex)
                updateItem(index)
                updateVoteItem(player)
                return@withAction true
            }
        }

    }

}
