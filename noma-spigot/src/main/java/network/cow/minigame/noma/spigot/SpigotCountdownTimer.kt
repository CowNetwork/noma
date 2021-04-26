package network.cow.minigame.noma.spigot

import net.kyori.adventure.text.Component
import network.cow.messages.adventure.comp
import network.cow.messages.adventure.formatToComponent
import network.cow.messages.adventure.gradient
import network.cow.messages.adventure.highlight
import network.cow.messages.core.Gradients
import network.cow.messages.spigot.sendInfo
import network.cow.minigame.noma.api.CountdownTimer
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask

/**
 * @author Benedikt Wüller
 */
class SpigotCountdownTimer(duration: Long, val name: String? = null) : CountdownTimer(duration) {

    private lateinit var timerTask: BukkitTask

    override fun displayTime(secondsLeft: Long) {
        Bukkit.getOnlinePlayers().forEach {
            val secondsFormat = if (secondsLeft == 1L) "%1\$s second" else "%1\$s seconds"
            val baseFormat = if (secondsLeft == 1L) "There is %1\$s left" else "There are %1\$s left"
            val format = baseFormat + (if (this.name != null) " (%2\$s)" else "") + "."

            it.sendInfo(format.formatToComponent(
                secondsFormat.formatToComponent(secondsLeft.toString().comp()).highlight(),
                this.name?.comp() ?: Component.empty()
            ), "Countdown".comp().gradient(Gradients.MINIGAME))
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
