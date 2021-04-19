package network.cow.minigame.noma.spigot

import network.cow.minigame.noma.api.CountdownTimer
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask

/**
 * @author Benedikt Wüller
 */
class SpigotCountdownTimer(duration: Long) : CountdownTimer(duration) {

    private lateinit var timerTask: BukkitTask

    override fun displayTime(secondsLeft: Long) {
        TODO("Not yet implemented")
    }

    override fun onStartTimer() {
        this.timerTask = Bukkit.getScheduler().runTaskTimer(NomaPlugin.INSTANCE, this::decrement, 20L, 20L)
    }

    override fun onResetTimer() {
        this.timerTask.cancel()
    }

}
