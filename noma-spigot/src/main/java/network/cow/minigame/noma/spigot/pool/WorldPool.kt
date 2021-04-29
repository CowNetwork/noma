package network.cow.minigame.noma.spigot.pool

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.PoolConfig
import network.cow.minigame.noma.api.get
import network.cow.minigame.noma.api.pool.Pool
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author Benedikt Wüller
 */
class WorldPool(game: Game<Player>, config: PoolConfig<Player>) : Pool<Player, WorldMeta>(game, config) {

    private val maps = mutableMapOf<String, WorldMeta>()
    private val relativeConfigPath = this.config.options.get("relativeConfigLocation", "mapconfig.yml")

    init {
        val path = this.config.options["location"].toString()
        val worlds = Files.list(Paths.get(path))
        worlds.forEach {
            this.maps[it.fileName.toString()] = this.readWorldMeta(it.toAbsolutePath())
        }
    }

    override fun getKeys(): List<String> = this.maps.keys.toList()

    override fun getItem(key: String): WorldMeta = this.maps[key]!!

    private fun readWorldMeta(path: Path) : WorldMeta {
        val config = YamlConfiguration.loadConfiguration(Paths.get(path.toString(), this.relativeConfigPath).toFile())

        val globalSpawnLocations = (config.getList("globalSpawns", emptyList<Map<String, Any>>()) as List<Map<String, Any>>).map(this::readLocation)

        val actorSpawnLocations = mutableMapOf<String, List<SpawnLocation>>()
        (config.getList("actorSpawns", emptyList<Map<String, Any>>()) as List<Map<String, Any>>).forEach {
            val key = it["actorKey"]?.toString() ?: error("") // TODO
            actorSpawnLocations[key] = it.get<List<Map<String, Any>>>("spawns", emptyList()).map(this::readLocation)
        }

        val gameRuleSection = config.getConfigurationSection("gameRules")
        val gameRules = mutableMapOf<GameRule<Any>, Any>()

        gameRuleSection?.getKeys(false)?.forEach {
            val gameRule = GameRule.getByName(it) as GameRule<Any>? ?: error("") // TODO
            val value = gameRuleSection.get(it) ?: error("") // TODO
            gameRules[gameRule] = value
        }

        return WorldMeta(
            path,
            globalSpawnLocations,
            actorSpawnLocations,
            gameRules,
            config.getValues(false)
        )
    }

    private fun readLocation(map: Map<String, Any>) = SpawnLocation(
        Vector(
            map.get("x", 0.0),
            map.get("y", 0.0),
            map.get("z", 0.0)
        ),
        map.get("yaw", 0.0F),
        map.get("pitch", 0.0F)
    )

}

data class WorldMeta(
    val path: Path,
    val globalSpawnLocations: List<SpawnLocation>,
    val actorSpawnLocations: Map<String, List<SpawnLocation>>,
    val gameRules: Map<GameRule<Any>, Any>,
    val options: Map<String, Any>
)

data class SpawnLocation(val coordinates: Vector, val yaw: Float, val pitch: Float) {
    fun toLocation(world: World) : Location {
        val location = this.coordinates.toLocation(world)
        location.yaw = this.yaw
        location.pitch = this.pitch
        return location
    }
}
