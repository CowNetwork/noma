package network.cow.minigame.noma.spigot

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.actor.ActorProvider
import network.cow.minigame.noma.api.config.ActorProviderConfig
import network.cow.minigame.noma.api.config.GameConfig
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.config.PhaseEndCountdown
import network.cow.minigame.noma.api.config.PhaseTimeout
import network.cow.minigame.noma.api.config.PoolConfig
import network.cow.minigame.noma.api.phase.Phase
import network.cow.minigame.noma.api.pool.Pool
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList

/**
 * @author Benedikt Wüller
 */
open class NomaGamePlugin : JavaPlugin() {

    lateinit var game: Game<Player>; protected set

    override fun onEnable() {
        val basePath = this.dataFolder.absolutePath
        val config = this.config

        val gamePath = Paths.get(basePath, config.getString("noma.game", "game.yml"))
        if (!Files.exists(gamePath)) error("The file ${gamePath.toAbsolutePath()} does not exist.")

        val phasesPath = Paths.get(basePath, config.getString("noma.phases", "phases.yml"))
        if (!Files.exists(phasesPath)) error("The file ${phasesPath.toAbsolutePath()} does not exist.")

        val phaseFiles = if (Files.isDirectory(phasesPath)) {
            Files.list(phasesPath).map { it.toFile() }.toList()
        } else {
            listOf(phasesPath.toFile())
        }

        val poolsPath = Paths.get(basePath, config.getString("noma.pools", "pools.yml"))
        if (!Files.exists(poolsPath)) error("The file ${poolsPath.toAbsolutePath()} does not exist.")

        val poolFiles = if (Files.isDirectory(poolsPath)) {
            Files.list(poolsPath).map { it.toFile() }.toList()
        } else {
            listOf(poolsPath.toFile())
        }

        this.game = SpigotGame(
            this.loadGameConfig(gamePath.toFile()),
            this.loadPhaseConfigs(phaseFiles),
            this.loadPoolConfigs(poolFiles)
        )

        this.game.start()
    }

    override fun onDisable() {
        try {
            this.game.stop(true)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Bukkit.shutdown()
        }
    }

    @Suppress("UncheckedCast")
    private fun loadPhaseConfigs(files: Iterable<File>) : List<PhaseConfig<Player>> {
        val configs = mutableListOf<PhaseConfig<Player>>()

        files.forEach { file ->
            val config = YamlConfiguration.loadConfiguration(file)
            config.getMapList("phases").forEach {
                val map = it as Map<String, Any>

                val countdownMap = (map["phaseEndCountdown"] ?: emptyMap<String, Any>()) as Map<*, *>
                val countdown = PhaseEndCountdown(((countdownMap["duration"] ?: 0) as Int).toLong())

                val timeout = PhaseTimeout(((map["duration"] ?: Int.MAX_VALUE) as Int).toLong(), map.getOrDefault("timeoutSilently", false) as Boolean)

                configs.add(PhaseConfig(
                    map["key"]?.toString() ?: error("Field 'key' is missing for phase in ${file.name}."),
                    Class.forName(map["kind"]?.toString() ?: error("Field 'kind' is missing for phase in ${file.name}.")) as Class<out Phase<Player, *>>,
                    (map["allowsNewPlayers"] ?: false) as Boolean,
                    (map["requiresActors"] ?: true) as Boolean,
                    countdown, timeout, map
                ))
            }
        }

        return configs
    }

    private fun loadPoolConfigs(files: Iterable<File>) : List<PoolConfig<Player>> {
        val configs = mutableListOf<PoolConfig<Player>>()
        files.forEach { file ->
            val config = YamlConfiguration.loadConfiguration(file)
            config.getMapList("pools").forEach {
                val map = it as Map<String, Any>

                configs.add(PoolConfig(
                    map["key"]?.toString() ?: error("Field 'key' is missing for phase in ${file.name}."),
                    Class.forName(map["kind"]?.toString() ?: error("Field 'kind' is missing for phase in ${file.name}.")) as Class<out Pool<Player, *>>,
                    map.getOrDefault("items", emptyList<String>()) as List<String>,
                    map
                ))
            }
        }
        return configs
    }

    private fun loadGameConfig(file: File) : GameConfig<Player> {
        val config = YamlConfiguration.loadConfiguration(file).getConfigurationSection("game") ?: error("Root element 'game' is missing in ${file.name}.")

        val actorProvider = config.getConfigurationSection("actorProvider") ?: error("Section 'game.actorProvider' is missing in ${file.name}.")
        val actorProviderConfig = ActorProviderConfig(
            Class.forName(actorProvider.getString("kind") ?: error("Field 'game.actorProvider.kind' is missing in ${file.name}.")) as Class<out ActorProvider<Player>>,
            actorProvider.getValues(false)
        )

        return GameConfig(
            config.getInt("maxPlayers", -1),
            actorProviderConfig,
            File(".").absolutePath,
            config.getValues(false)
        )
    }

}
