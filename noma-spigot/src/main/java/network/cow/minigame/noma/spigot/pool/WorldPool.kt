package network.cow.minigame.noma.spigot.pool

import net.kyori.adventure.text.Component
import network.cow.messages.adventure.comp
import network.cow.messages.adventure.corporate
import network.cow.messages.adventure.highlight
import network.cow.messages.adventure.info
import network.cow.messages.adventure.translate
import network.cow.minigame.noma.api.Translations
import network.cow.minigame.noma.api.config.PoolConfig
import network.cow.minigame.noma.api.get
import network.cow.minigame.noma.spigot.SpigotGame
import network.cow.spigot.extensions.ItemBuilder
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author Benedikt Wüller
 */
class WorldPool(game: SpigotGame, config: PoolConfig<Player>) : SpigotPool<WorldMeta>(game, config) {

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
            val key = it["actorKey"]?.toString() ?: error("A value has to be provided for 'actorSpawns.*.actorKey'.")
            actorSpawnLocations[key] = it.get<List<Any>>("spawns", emptyList()).map(this::readLocation)
        }

        val gameRuleSection = config.getConfigurationSection("gameRules")
        val gameRules = mutableMapOf<GameRule<Any>, Any>()

        gameRuleSection?.getKeys(false)?.forEach {
            val gameRule = GameRule.getByName(it) as GameRule<Any>? ?: error("No game rule exists for key '$it'.")
            val value = gameRuleSection.get(it)!!
            gameRules[gameRule] = value
        }

        val name = config.getString("name", path.fileName.toString())!!
        val authors = config.getString("authors", "-")?.split(",") ?: emptyList()
        val material = config.getString("material")?.let { Material.valueOf(it) } ?: Material.STONE

        return WorldMeta(
            path,
            name,
            authors,
            material,
            globalSpawnLocations,
            actorSpawnLocations,
            gameRules,
            config.getValues(false)
        )
    }

    private fun readLocation(obj: Any) : SpawnLocation {
        return when (obj) {
            is Map<*, *> -> {
                val map = obj as Map<String, Any>
                SpawnLocation(
                    Vector(
                        map.get("x", 0.0),
                        map.get("y", 0.0),
                        map.get("z", 0.0)
                    ),
                    map.get("yaw", 0.0F),
                    map.get("pitch", 0.0F)
                )
            }
            is String -> TODO("read from string")
            else -> error("Unsupported location type ${obj.javaClass.name}.")
        }
    }

    override fun getDisplayItem(player: Player, key: String): ItemStack {
        val item = this.getItem(key)

        var names = Component.empty()
        (item.authors).forEachIndexed { index, author ->
            names = names.append(author.highlight())
            if (index != item.authors.lastIndex) {
                names = names.append(", ".comp())
            }
        }

        return ItemBuilder(item.material)
            .name(item.name.corporate())
            .lore(Component.empty(), Translations.POOLS_WORLD_BUILDERS.translate(player).info(), names.info())
            .build()
    }

}

data class WorldMeta(
    val path: Path,
    val name: String,
    val authors: List<String>,
    val material: Material,
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
