package network.cow.minigame.noma.spigot

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Benedikt Wüller
 */
class NomaPlugin : JavaPlugin() {

    override fun onEnable() {
        Bukkit.getScoreboardManager().mainScoreboard.teams
            .filter { it.name.startsWith(SpigotActor.SCOREBOARD_PREFIX) }
            .forEach { it.unregister() }
    }

}
