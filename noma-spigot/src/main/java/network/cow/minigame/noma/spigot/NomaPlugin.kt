package network.cow.minigame.noma.spigot

import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Benedikt Wüller
 */
class NomaPlugin : JavaPlugin() {

    companion object {
        @JvmStatic
        lateinit var INSTANCE: NomaPlugin
    }

    override fun onEnable() {
        INSTANCE = this
    }

}
