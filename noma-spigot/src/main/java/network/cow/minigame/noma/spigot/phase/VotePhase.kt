package network.cow.minigame.noma.spigot.phase

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.SelectionMethod
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.pool.Pool
import network.cow.minigame.noma.spigot.SpigotGame
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * @author Benedikt Wüller
 */
open class VotePhase(game: Game<Player>, config: PhaseConfig<Player>) : SpigotPhase<VotePhase.Result<Any>>(game, config), CommandExecutor {

    // TODO: rework with inventory or frame interface.
    // TODO: translation

    private val votes = mutableMapOf<Player, List<Int>>()

    private val pool: Pool<Player, *>

    val options: Int = this.config.options.getOrDefault("options", Int.MAX_VALUE) as Int
    val votesPerPlayer: Int = minOf(this.config.options.getOrDefault("votesPerPlayer", 1) as Int, this.options)
    val items: List<String>

    init {
        val key = this.config.options["pool"]?.toString() ?: error("The option 'phases.*.pool' is missing.")
        this.pool = this.game.getPool(key)

        val selectionMethod = SelectionMethod.valueOf(this.config.options.getOrDefault("optionSelectionMethod", SelectionMethod.RANDOM.name).toString())
        this.items = when (selectionMethod) {
            SelectionMethod.RANDOM -> this.pool.getKeys().shuffled()
            SelectionMethod.ORDERED -> this.pool.getKeys()
        }.take(this.options).sorted()

        Bukkit.getPluginCommand("vote")?.setExecutor(this)
    }

    override fun onStart() {
        super.onStart()
        this.displayOptions()
    }

    override fun onPlayerJoin(player: Player) {
        this.displayOptions()
    }

    override fun onPlayerLeave(player: Player) {
        this.votes.remove(player)
    }

    fun getVotes(index: Int) : Int = this.votes.count { index in it.value }

    fun getVotes(item: String) : Int = this.getVotes(this.items.indexOf(item))

    open fun displayOptions() {
        if (this.game !is SpigotGame) return
        this.game.getSpigotActors().forEach {
            it.sendMessage("You have ${this.votesPerPlayer} votes.")
            this.items.forEachIndexed { index, item -> it.sendMessage("${index + 1}. $item (votes: ${this.getVotes(index)})") }
            it.sendMessage("Use /vote <number> or /vote <number,number,...>")
        }
    }

    protected fun vote(player: Player, vararg votes: Int) {
        votes.forEach {
            if (it >= 0 && it <= this.items.lastIndex) return@forEach
            throw IndexOutOfBoundsException("The vote $it is out of bounds.")
        }
        this.votes[player] = votes.take(this.votesPerPlayer)
    }

    override fun onStop(): Result<Any> {
        // Return the items with the most votes.
        val votes = mutableMapOf<String, Int>()
        this.items.forEachIndexed { index, item -> votes[item] = this.getVotes(index) }
        return Result(votes.keys.map { ResultItem(it, this.pool.getItem(it), votes[it] ?: 0) }.sortedByDescending { it.votes }.take(this.options))
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Nope")
            return true
        }

        if (args.size != 1) {
            sender.sendMessage("Use /vote <number> or /vote <number,number,...>")
            return true
        }

        val numbers = args.first().split(",").mapNotNull { it.toIntOrNull() }.toIntArray()
        this.vote(sender, *numbers)
        this.displayOptions()
        return true
    }

    override fun onTimeout() = Unit

    data class Result<T>(val items: List<ResultItem<T>>)

    data class ResultItem<T>(val key: String, val item: T, val votes: Int)

}
