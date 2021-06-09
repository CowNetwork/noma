package network.cow.minigame.noma.spigot.phase

import net.kyori.adventure.text.Component
import network.cow.messages.adventure.comp
import network.cow.messages.adventure.corporate
import network.cow.messages.adventure.highlight
import network.cow.messages.adventure.translate
import network.cow.messages.spigot.broadcastTranslatedInfo
import network.cow.messages.spigot.sendTranslatedInfo
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.config.PhaseTimeoutConfig
import network.cow.minigame.noma.spigot.SpigotActor
import network.cow.minigame.noma.spigot.SpigotGame
import network.cow.minigame.noma.spigot.SpigotTranslations
import network.cow.minigame.noma.spigot.world.provider.InitialWorld
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author Benedikt Wüller
 */
open class EndPhase(game: SpigotGame, config: PhaseConfig<Player, SpigotGame>) : SpigotPhase(game, PhaseConfig(
        config.key, config.kind, false, config.requiresActors, config.phaseEndCountdown,
        PhaseTimeoutConfig(0, true), config.storeMiddleware, populateSpigotOptions(config.options.toMutableMap())
)) {

    companion object {
        const val STORE_KEY = "end_phase.result"
    }

    private fun displayRanking(rankings: List<Set<SpigotActor>>, index: Int) {
        if (rankings.size <= index) return

        val ranking = when (index) {
            0 -> SpigotTranslations.COMMON_FIRST
            1 -> SpigotTranslations.COMMON_SECOND
            2 -> SpigotTranslations.COMMON_THIRD
            else -> error("Ranking is only implemented for the first 3 indices. Index given: $index")
        }

        val winners = rankings[index].map { it.getPlayers().map { player -> player.displayName() }.join(", ") }.join(", ")
        Bukkit.getOnlinePlayers().forEach { player ->
            player.sendTranslatedInfo(
                SpigotTranslations.PHASE_END_RANKING,
                ranking.translate(player).corporate(),
                winners
            )
        }
    }

    override fun onStart() {
        val result = this.game.store.get<Result>(STORE_KEY)

        if (result == null) {
            Bukkit.getServer().broadcastTranslatedInfo(SpigotTranslations.PHASE_END_NO_WINNERS)
        } else {
            this.displayRanking(result.rankings, 0) // 1st place
            this.displayRanking(result.rankings, 1) // 2nd place
            this.displayRanking(result.rankings, 2) // 3rd place

            // Display "your rank: xyz" for all other places.
            for (i in 3 until result.rankings.size) {
                val ranking = result.rankings[i]
                ranking.forEach { actor ->
                    actor.apply { it.sendTranslatedInfo(SpigotTranslations.PHASE_END_YOUR_RANK, (i + 1).toString().highlight()) }
                }
            }
        }

        // TODO: display statistics slightly delayed
    }

    override fun onTimeout() = Unit
    override fun onStop() = Unit
    override fun onPlayerJoin(player: Player) = Unit
    override fun onPlayerLeave(player: Player) = Unit

    data class Result(val rankings: List<Set<SpigotActor>>)

}

fun populateSpigotOptions(options: MutableMap<String, Any>) : Map<String, Any> {
    val teleport = ((options["teleport"] as Map<String, Any>?)?.toMutableMap() ?: mutableMapOf())
    teleport["onStart"] = true
    options["teleport"] = teleport

    val worldProvider = ((options["worldProvider"] as Map<String, Any>?)?.toMutableMap() ?: mutableMapOf())
    worldProvider["kind"] = worldProvider.getOrDefault("kind", InitialWorld::class.java.name)
    options["worldProvider"] = worldProvider

    return options
}

fun List<Component>.join(separator: String = "") : Component {
    return this.reduce { first, second -> first.append(separator.comp()).append(second) }
}
