package network.cow.minigame.noma.spigot

import network.cow.messages.adventure.comp
import network.cow.messages.adventure.formatToComponent
import network.cow.messages.adventure.gradient
import network.cow.messages.adventure.highlight
import network.cow.messages.core.Gradients
import network.cow.messages.spigot.sendInfo
import network.cow.minigame.noma.api.CountdownTimer
import network.cow.minigame.noma.api.Translations
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

/**
 * @author Benedikt Wüller
 */
class SpigotCountdownTimer(duration: Long, baseTranslationKey: String = Translations.COUNTDOWN_MESSAGE_GENERIC_BASE)
    : CountdownTimer(duration, baseTranslationKey) {

    private lateinit var timerTask: BukkitTask

    override fun displayTime(secondsLeft: Long) {
        val minutes = (secondsLeft / 60).toInt()
        val seconds = (secondsLeft % 60).toInt()

        Bukkit.getOnlinePlayers().forEach {
            // TODO: use translations

            val time = when {
                minutes == 1 -> "$minutes minute"
                minutes > 0 -> "$minutes minutes"
                seconds == 1 -> "$seconds second"
                else -> "$seconds seconds"
            }.highlight()

            val format = when {
                minutes == 1 || (minutes == 0 && seconds == 1) -> "There is %1\$s left"
                else -> "There are %1\$s left"
            }

            it.sendInfo(format.formatToComponent(time), "Countdown".comp().gradient(Gradients.MINIGAME))
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
