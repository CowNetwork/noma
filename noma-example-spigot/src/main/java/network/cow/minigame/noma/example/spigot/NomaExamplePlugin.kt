package network.cow.minigame.noma.example.spigot

import network.cow.messages.adventure.gradient
import network.cow.messages.core.Gradients
import network.cow.messages.spigot.MessagesPlugin
import network.cow.minigame.noma.spigot.NomaGamePlugin

/**
 * @author Benedikt Wüller
 */
class NomaExamplePlugin : NomaGamePlugin() {

    override fun onEnable() {
        super.onEnable()
        MessagesPlugin.PREFIX = "ExampleGame".gradient(Gradients.MINIGAME)
    }

}
