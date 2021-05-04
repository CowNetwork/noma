package network.cow.minigame.noma.spigot

import network.cow.messages.adventure.gradient
import network.cow.messages.adventure.highlight
import network.cow.messages.core.Gradients
import network.cow.messages.spigot.MessagesPlugin
import network.cow.messages.spigot.broadcastTranslatedInfo
import network.cow.minigame.noma.api.CountdownTimer
import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.SelectionMethod
import network.cow.minigame.noma.api.Translations
import network.cow.minigame.noma.api.config.GameConfig
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.config.PoolConfig
import network.cow.minigame.noma.api.phase.Phase
import network.cow.minigame.noma.spigot.config.WorldProviderConfig
import network.cow.minigame.noma.spigot.phase.SpigotPhase
import network.cow.minigame.noma.spigot.world.DefaultWorldProvider
import network.cow.minigame.noma.spigot.world.WorldProvider
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Team

/**
 * @author Benedikt Wüller
 */
open class SpigotGame(config: GameConfig<Player>, phaseConfigs: List<PhaseConfig<Player>>, poolConfigs: List<PoolConfig<Player>>)
    : Game<Player>(config, phaseConfigs, poolConfigs), Listener {

    var world: World = Bukkit.getWorlds().first(); private set
    var worldProvider: WorldProvider = DefaultWorldProvider(this, WorldProviderConfig(DefaultWorldProvider::class.java, emptyMap())); private set

    init {
        Bukkit.getPluginManager().registerEvents(this, JavaPlugin.getPlugin(NomaPlugin::class.java))
    }

    override fun onStop() {
        HandlerList.unregisterAll(this)
        Bukkit.shutdown()
    }

    override fun onSetPhase(oldPhase: Phase<Player>?, newPhase: Phase<Player>?) {
        if (newPhase !is SpigotPhase) return
        val config = newPhase.spigotConfig.worldProvider
        this.worldProvider = config.kind.getDeclaredConstructor(SpigotGame::class.java, WorldProviderConfig::class.java).newInstance(this, config)
        this.world = this.worldProvider.selectWorld()
    }

    fun getSpigotActor(player: Player) : SpigotActor? {
        val actor = this.getActor(player) ?: return null
        if (actor !is SpigotActor) return null
        return actor
    }

    fun getSpigotActors() = this.getActors().filterIsInstance<SpigotActor>()

    fun getScoreboardTeam(player: Player) : Team? = this.getSpigotActor(player)?.scoreboardTeam

    fun getScoreboardTeams() = this.getActors().filterIsInstance<SpigotActor>().map { it.scoreboardTeam }

    override fun createCountdownTimer(duration: Long, baseTranslationKey: String): CountdownTimer = SpigotCountdownTimer(duration, baseTranslationKey)

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val phase = this.getCurrentPhase()
        val player = event.player

        if (!phase.config.allowsNewPlayers || (this.config.maxPlayers >= 0 && this.getIngamePlayers().size >= this.config.maxPlayers)) {
            player.gameMode = GameMode.SPECTATOR
            event.joinMessage(null)

            // Teleport spectator to current world.
            val method = if (phase is SpigotPhase) phase.spigotConfig.teleportSelectionMethod else SelectionMethod.ORDERED
            player.teleport(this.worldProvider.getSpectatorSpawnLocation(method))
            return
        }

        if (phase.config.requiresActors) {
            this.actorProvider.addPlayer(player)
        }

        // Teleport player to current world.
        val method = if (phase is SpigotPhase) phase.spigotConfig.teleportSelectionMethod else SelectionMethod.ORDERED
        player.teleport(this.worldProvider.getSpawnLocation(this.getSpigotActor(player), method))

        event.joinMessage(null)

        val prefix = MessagesPlugin.PREFIX ?: "Minigame".gradient(Gradients.MINIGAME)
        Bukkit.getServer().broadcastTranslatedInfo(Translations.PLAYER_JOINED, player.displayName().highlight(), prefix = prefix)

        phase.join(player)
    }

    @EventHandler
    private fun onPlayerLeave(event: PlayerQuitEvent) {
        val player = event.player
        event.quitMessage(null)

        this.actorProvider.removePlayer(player)
        this.getCurrentPhase().leave(player)

        if (player.gameMode == GameMode.SPECTATOR) return
        val prefix = MessagesPlugin.PREFIX ?: "Minigame".gradient(Gradients.MINIGAME)
        Bukkit.getServer().broadcastTranslatedInfo(Translations.PLAYER_LEFT, player.displayName().highlight(), prefix = prefix)
    }

}
