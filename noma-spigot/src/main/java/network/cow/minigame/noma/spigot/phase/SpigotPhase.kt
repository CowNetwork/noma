package network.cow.minigame.noma.spigot.phase

import network.cow.minigame.noma.api.Game
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.phase.Phase
import org.bukkit.entity.Player

/**
 * @author Benedikt Wüller
 */
abstract class SpigotPhase<ResultType : Any>(game: Game<Player>, config: PhaseConfig<Player>) : Phase<Player, ResultType>(game, config)
