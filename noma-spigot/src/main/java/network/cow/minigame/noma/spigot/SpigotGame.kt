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
import network.cow.minigame.noma.spigot.world.provider.CurrentWorld
import network.cow.minigame.noma.spigot.world.provider.WorldProvider
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
import org.spigotmc.event.player.PlayerSpawnLocationEvent

/**
 * @author Benedikt Wüller
 */
open class SpigotGame(config: GameConfig<Player, SpigotGame>, phaseConfigs: List<PhaseConfig<Player, SpigotGame>>, poolConfigs: List<PoolConfig<Player, SpigotGame>>)
    : Game<Player, SpigotGame>(config, phaseConfigs, poolConfigs), Listener {

    companion object {
        val FALLBACK_PREFIX = "Minigame".gradient(Gradients.MINIGAME)
    }

    var world: World = Bukkit.getWorlds().first(); private set
    var worldProvider: WorldProvider = CurrentWorld(this, WorldProviderConfig(CurrentWorld::class.java, emptyMap())); private set

    init {
        Bukkit.getPluginManager().registerEvents(this, JavaPlugin.getPlugin(NomaPlugin::class.java))
    }

    override fun onStop() {
        HandlerList.unregisterAll(this)
        Bukkit.shutdown()
    }

    override fun onSetPhase(oldPhase: Phase<Player, SpigotGame>?, newPhase: Phase<Player, SpigotGame>?) {
        if (oldPhase?.config?.requiresActors != true && newPhase?.config?.requiresActors == true) {
            for (player in Bukkit.getOnlinePlayers().filter { it.gameMode != GameMode.SPECTATOR }.take(this.config.maxPlayers)) {
                this.actorProvider.addPlayer(player)
            }
        }

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

        player.inventory.clear()
        player.gameMode = GameMode.SURVIVAL

        event.joinMessage(null)

        if (!phase.config.allowsNewPlayers || (this.config.maxPlayers >= 0 && Bukkit.getOnlinePlayers().filter { it.gameMode != GameMode.SPECTATOR }.size > this.config.maxPlayers)) {
            player.gameMode = GameMode.SPECTATOR
            return
        }

        if (phase.config.requiresActors) {
            this.actorProvider.addPlayer(player)
        }

        val prefix = MessagesPlugin.PREFIX ?: FALLBACK_PREFIX
        Bukkit.getServer().broadcastTranslatedInfo(Translations.PLAYER_JOINED, player.displayName().highlight(), prefix = prefix)

        phase.join(player)
    }

    @EventHandler
    private fun onPlayerSpawn(event: PlayerSpawnLocationEvent) {
        val phase = this.getCurrentPhase()
        val player = event.player

        val method = if (phase is SpigotPhase) phase.spigotConfig.teleportSelectionMethod else SelectionMethod.ORDERED

        // Teleport player to current world.
        if (player.gameMode == GameMode.SPECTATOR) {
            event.spawnLocation = this.worldProvider.getSpectatorSpawnLocation(method)
        } else {
            event.spawnLocation = this.worldProvider.getSpawnLocation(this.getSpigotActor(player), method)
        }
    }

    @EventHandler
    private fun onPlayerLeave(event: PlayerQuitEvent) {
        val player = event.player
        event.quitMessage(null)

        this.actorProvider.removePlayer(player)
        this.getCurrentPhase().leave(player)

        if (player.gameMode != GameMode.SPECTATOR) {
            val prefix = MessagesPlugin.PREFIX ?: FALLBACK_PREFIX
            Bukkit.getServer().broadcastTranslatedInfo(Translations.PLAYER_LEFT, player.displayName().highlight(), prefix = prefix)
        }

        if (!this.getCurrentPhase().config.allowsNewPlayers && this.getCurrentPhase().config.requiresActors && this.getActors().size < this.config.minActors) {
            val lastPhaseKey = this.phaseConfigs.last().key
            if (this.getCurrentPhase().config.key == lastPhaseKey) return
            this.setPhase(lastPhaseKey, true)
        }
    }

}
