package network.cow.minigame.noma.spigot

import net.kyori.adventure.text.Component
import network.cow.minigame.noma.api.CountdownTimer
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask

/**
 * @author Benedikt Wüller
 */
class SpigotCountdownTimer(duration: Long) : CountdownTimer(duration) {

    private lateinit var timerTask: BukkitTask

    override fun displayTime(secondsLeft: Long) {
        Bukkit.getOnlinePlayers().forEach {
            // TODO: beautify message and translate per player
            it.sendMessage(Component.text("Countdown ends in $secondsLeft seconds."))
        }
    }

    override fun onStartTimer() {
        this.timerTask = Bukkit.getScheduler().runTaskTimer(NomaPlugin.INSTANCE, this::decrement, 20L, 20L)
    }

    override fun onResetTimer() {
        if (!this::timerTask.isInitialized) return
        this.timerTask.cancel()
    }

}
