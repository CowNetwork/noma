package network.cow.minigame.noma.spigot.phase

import network.cow.messages.adventure.error
import network.cow.messages.adventure.translateToComponent
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.phase.Phase
import network.cow.minigame.noma.api.SelectionMethod
import network.cow.minigame.noma.spigot.NomaPlugin
import network.cow.minigame.noma.spigot.SpigotActor
import network.cow.minigame.noma.spigot.SpigotGame
import network.cow.minigame.noma.spigot.SpigotTranslations
import network.cow.minigame.noma.spigot.config.SpigotPhaseConfig
import network.cow.minigame.noma.spigot.config.WorldProviderConfig
import network.cow.minigame.noma.spigot.parseClass
import network.cow.minigame.noma.spigot.world.provider.CurrentWorld
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Benedikt Wüller
 */
abstract class SpigotPhase(game: SpigotGame, config: PhaseConfig<Player>) : Phase<Player, SpigotGame>(game, config), Listener {

    private lateinit var listeners: Collection<Listener>

    val spigotConfig: SpigotPhaseConfig

    init {
        val worldProviderMap = this.config.options.getOrElse("worldProvider") { emptyMap<String, Any>() } as Map<String, Any>
        val worldProviderConfig = WorldProviderConfig(
            parseClass(worldProviderMap.getOrDefault("kind", CurrentWorld::class.java.name).toString()),
            worldProviderMap
        )

        val teleportMap = this.config.options.getOrElse("teleport") { emptyMap<String, Any>() } as Map<String, Any>

        this.spigotConfig = SpigotPhaseConfig(
            this.config.options.getOrDefault("allowsSpectators", true) as Boolean,
            teleportMap.getOrDefault("onStart", false) as Boolean,
            SelectionMethod.valueOf(teleportMap.getOrDefault("selectionMethod", SelectionMethod.ORDERED.name).toString()),
            worldProviderConfig
        )
    }

    override fun start() {
        super.start()

        // Teleport players on start if requested.
        if (this.spigotConfig.teleportOnStart) {
            this.game.getSpigotActors().forEach {
                val locations = this.game.worldProvider.getSpawnLocations(it).toTypedArray()
                it.teleport(this.spigotConfig.teleportSelectionMethod, *locations)
            }

            val players = this.game.getIngamePlayers()
            Bukkit.getOnlinePlayers().filter { it !in players }.forEach {
                val location = this.game.worldProvider.getSpectatorSpawnLocation(this.spigotConfig.teleportSelectionMethod)
                it.teleport(location)
            }
        }

        this.listeners = this.getListeners()
        this.listeners.forEach {
            Bukkit.getPluginManager().registerEvents(it, JavaPlugin.getPlugin(NomaPlugin::class.java))
        }
    }

    override fun stop() {
        this.listeners.forEach { HandlerList.unregisterAll(it) }
        super.stop()
    }

    override fun join(player: Player) {
        if (!this.spigotConfig.allowSpectators && Bukkit.getOnlinePlayers().size > this.game.config.maxPlayers) {
            player.kick(SpigotTranslations.PHASE_LOBBY_FULL.translateToComponent(player).error())
            return
        }

        // Teleport the player to the current map.
        val actor = this.game.getSpigotActor(player)
        if (actor is SpigotActor) {
            val location = this.game.worldProvider.getSpawnLocation(actor, this.spigotConfig.teleportSelectionMethod)
            player.teleport(location)
        }

        super.join(player)
    }

    open fun getListeners() : Set<Listener> = setOf(this)

}
