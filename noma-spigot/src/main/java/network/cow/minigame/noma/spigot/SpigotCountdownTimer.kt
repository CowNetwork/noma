package network.cow.minigame.noma.spigot

import network.cow.messages.adventure.gradient
import network.cow.messages.adventure.highlight
import network.cow.messages.adventure.translate
import network.cow.messages.core.Gradients
import network.cow.messages.spigot.sendTranslatedInfo
import network.cow.minigame.noma.api.CountdownTimer
import network.cow.minigame.noma.api.Translations
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

/**
 * @author Benedikt Wüller
 */
class SpigotCountdownTimer(duration: Long, baseTranslationKey: String = Translations.COUNTDOWN_MESSAGE_GENERIC_BASE)
    : CountdownTimer(duration, baseTranslationKey) {

    private lateinit var timerTask: BukkitTask

    var sound: Sound? = Sound.BLOCK_NOTE_BLOCK_BIT

    fun sound(sound: Sound?) : SpigotCountdownTimer {
        this.sound = sound
        return this
    }

    override fun displayTime(secondsLeft: Long) {
        val minutes = (secondsLeft / 60).toInt()
        val seconds = (secondsLeft % 60).toInt()

        Bukkit.getOnlinePlayers().forEach {
            val time = when {
                minutes == 1 -> Translations.MINUTE_SINGULAR.translate(it, minutes.toString())
                minutes > 0 -> Translations.MINUTE_PLURAL.translate(it, minutes.toString())
                seconds == 1 -> Translations.SECOND_SINGULAR.translate(it, seconds.toString())
                else -> Translations.SECOND_PLURAL.translate(it, seconds.toString())
            }.highlight()

            val format = when {
                minutes == 1 || (minutes == 0 && seconds == 1) -> this.singularTranslationKey
                else -> this.pluralTranslationKey
            }

            it.sendTranslatedInfo(format, time, prefix = "Countdown".gradient(Gradients.MINIGAME))

            val sound = this.sound ?: return@forEach
            it.playSound(it.location, sound, 1.0F, 1.0F)
        }
    }

    override fun onStartTimer() {
        this.timerTask = Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(NomaPlugin::class.java), this::decrement, 20L, 20L)
    }

    override fun onResetTimer() {
        if (!this::timerTask.isInitialized) return
        this.timerTask.cancel()
    }

}
