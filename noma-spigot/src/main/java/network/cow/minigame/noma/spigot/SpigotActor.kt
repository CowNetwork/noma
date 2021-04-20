package network.cow.minigame.noma.spigot

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import network.cow.minigame.noma.api.actor.Actor
import org.bukkit.Bukkit
import org.bukkit.DyeColor
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.WeatherType
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.scoreboard.Team
import java.awt.Color
import java.util.UUID

/**
 * @author Benedikt Wüller
 */
class SpigotActor(name: String? = null, color: Color = Color.WHITE, private val showPrefix: Boolean = false, prefixColor: Color = color)
    : Actor<Player>(name, color) {

    private val textColor = NamedTextColor.nearestTo(TextColor.color(this.color.red, this.color.green, this.color.blue))
    private val prefixColor = NamedTextColor.nearestTo(TextColor.color(prefixColor.red, prefixColor.green, prefixColor.blue))

    private lateinit var compassTarget: Location

    val scoreboardTeam: Team

    var weather: WeatherType = WeatherType.CLEAR
        set(value) {
            field = value
            this.apply { it.setPlayerWeather(value) }
        }

    init {
        val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        this.scoreboardTeam = scoreboard.registerNewTeam(this.initialName ?: UUID.randomUUID().toString().replace("-", "").substring(0, 16))
        this.scoreboardTeam.color(this.textColor)
    }

    override fun calculateName(): String {
        if (this.initialName != null) return this.initialName
        val players = this.getPlayers()

        var name = when {
            players.size >= 3 -> players[0].name.substring(0, 2) + players[1].name.substring(0, 2) + players[2].name.substring(0, 2)
            players.size >= 2 -> players[0].name.substring(0, 2) + players[1].name.substring(0, 2)
            players.isNotEmpty() -> players.first().name.substring(0, 4)
            else -> super.calculateName()
        }

        if (this.name != name) {
            var number = 0
            do {
                val exists = Bukkit.getScoreboardManager().mainScoreboard.getTeam(name) != null
                if (exists) number += 1
            } while (exists)
            if (number > 0) name = "$name$number"
        }

        return name
    }

    override fun updateName() {
        super.updateName()
        this.scoreboardTeam.displayName(Component.text(this.name, this.textColor))
        if (this.showPrefix) {
            this.scoreboardTeam.prefix(Component.text("[", NamedTextColor.DARK_GRAY).append(Component.text(this.name, this.prefixColor)).append(Component.text("] ", NamedTextColor.DARK_GRAY)))
        }
    }

    override fun onPlayerJoin(player: Player) {
        this.scoreboardTeam.addEntry(player.name)
    }

    override fun onPlayerLeave(player: Player) {
        this.scoreboardTeam.removeEntry(player.name)
    }

    fun <T> playEffect(location: Location, effect: Effect, data: T) = this.apply { it.playEffect(location, effect, data) }

    fun playSound(location: Location, sound: String, volume: Float, pitch: Float) = this.apply { it.playSound(location, sound, volume, pitch) }
    fun playSound(location: Location, sound: String, category: SoundCategory, volume: Float, pitch: Float) = this.apply { it.playSound(location, sound, category, volume, pitch) }
    fun playSound(location: Location, sound: Sound, volume: Float, pitch: Float) = this.apply { it.playSound(location, sound, volume, pitch) }
    fun playSound(location: Location, sound: Sound, category: SoundCategory, volume: Float, pitch: Float) = this.apply { it.playSound(location, sound, category, volume, pitch) }

    fun sendBlockChange(location: Location, block: BlockData) = this.apply { it.sendBlockChange(location, block) }
    fun sendBlockDamage(location: Location, progress: Float) = this.apply { it.sendBlockDamage(location, progress) }

    fun sendExperienceChange(progress: Float) = this.apply { it.sendExperienceChange(progress) }
    fun sendExperienceChange(progress: Float, level: Int) = this.apply { it.sendExperienceChange(progress, level) }

    fun sendRawMessage(message: String) = this.apply { it.sendRawMessage(message) }

    fun sendSignChange(location: Location, lines: List<Component>) = this.apply { it.sendSignChange(location, lines) }
    fun sendSignChange(location: Location, lines: List<Component>, color: DyeColor) = this.apply { it.sendSignChange(location, lines, color) }

    fun sendTitle(title: String?, subtitle: String?, fadeIn: Int = 10, stay: Int = 70, fadeOut: Int = 20) = this.apply { it.sendTitle(title, subtitle, fadeIn, stay, fadeOut) }

    fun getCompassTarget() = this.compassTarget.clone()
    fun setCompassTarget(location: Location) {
        this.compassTarget = location
        this.apply { it.compassTarget = location }
    }

    fun spawnParticle(particle: Particle, x: Double, y: Double, z: Double, count: Int) = this.apply { it.spawnParticle(particle, x, y, z, count) }
    fun spawnParticle(particle: Particle, x: Double, y: Double, z: Double, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double) {
        this.apply { it.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ) }
    }
    fun spawnParticle(particle: Particle, x: Double, y: Double, z: Double, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double, extra: Double) {
        this.apply { it.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra) }
    }
    fun <T> spawnParticle(particle: Particle, x: Double, y: Double, z: Double, count: Int, data: T) = this.apply { it.spawnParticle(particle, x, y, z, count, data) }
    fun <T> spawnParticle(particle: Particle, x: Double, y: Double, z: Double, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double, extra: Double, data: T) {
        this.apply { it.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, data) }
    }

    fun spawnParticle(particle: Particle, location: Location, count: Int) = this.apply { it.spawnParticle(particle, location, count) }
    fun spawnParticle(particle: Particle, location: Location, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double) {
        this.apply { it.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ) }
    }
    fun spawnParticle(particle: Particle, location: Location, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double, extra: Double) {
        this.apply { it.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra) }
    }
    fun <T> spawnParticle(particle: Particle, location: Location, count: Int, data: T) = this.apply { it.spawnParticle(particle, location, count, data) }
    fun <T> spawnParticle(particle: Particle, location: Location, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double, extra: Double, data: T) {
        this.apply { it.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra, data) }
    }

    fun stopSound(sound: String) = this.apply { it.stopSound(sound) }
    fun stopSound(sound: String, category: SoundCategory) = this.apply { it.stopSound(sound, category) }
    fun stopSound(sound: Sound) = this.apply { it.stopSound(sound) }
    fun stopSound(sound: Sound, category: SoundCategory) = this.apply { it.stopSound(sound, category) }

    fun sendMessage(message: String) = this.apply { it.sendMessage(message) }
    fun sendMessage(vararg messages: String) = this.apply { it.sendMessage(messages) }

    fun teleport(vararg locations: Location) = this.teleport(TeleportMethod.ORDERED, *locations)

    fun teleport(method: TeleportMethod = TeleportMethod.ORDERED, vararg locations: Location) {
        if (locations.isEmpty()) throw IllegalArgumentException("At least one location must be provided.")

        when (method) {
            TeleportMethod.RANDOM -> locations.shuffle()
            else -> Unit
        }

        var index = 0
        this.apply {
            it.teleport(locations[index])
            index = if (index == locations.lastIndex) 0 else (index + 1)
        }
    }

    fun addItems(vararg items: ItemStack) = this.apply { it.inventory.addItem(*items) }
    fun addItem(item: ItemStack) = this.addItems(item)
    fun setItem(slot: Int, item: ItemStack) = this.apply { it.inventory.setItem(slot, item) }
    fun setItem(slot: EquipmentSlot, item: ItemStack) = this.apply { it.inventory.setItem(slot, item) }

    fun setHeldItemSlot(slot: Int) = this.apply { it.inventory.heldItemSlot = slot }

}
