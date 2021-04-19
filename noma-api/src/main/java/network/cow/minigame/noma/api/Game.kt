package network.cow.minigame.noma.api

import network.cow.minigame.noma.api.actor.Actor
import network.cow.minigame.noma.api.actor.ActorProvider
import network.cow.minigame.noma.api.config.GameConfig
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.phase.Phase

/**
 * @author Benedikt Wüller
 */
abstract class Game<PlayerType : Any>(val config: GameConfig<PlayerType>, val phaseConfigs: List<PhaseConfig<PlayerType, *>>) {

    private val actors = mutableListOf<Actor<PlayerType>>()

    private val phases = LinkedHashMap<String, Phase<PlayerType, *>>()
    private lateinit var currentPhaseKey: String

    val actorProvider: ActorProvider<PlayerType> = this.config.actorProvider.kind.getDeclaredConstructor(Game::class.java).newInstance(this)

    init {
        if (this.phaseConfigs.isEmpty()) error("There must be at least one phase configured.")

        this.phaseConfigs.forEach {
            this.phases[it.key] = it.kind.getDeclaredConstructor(Game::class.java, PhaseConfig::class.java).newInstance(this, it)
        }
    }

    fun start() {
        if (this::currentPhaseKey.isInitialized) return
        // Set initial phase.
        this.nextPhase()
    }

    fun setPhase(key: String, skipCountdown: Boolean = false) {
        val currentPhase = if (this::currentPhaseKey.isInitialized) this.getPhase(this.currentPhaseKey) else null
        val phase = this.getPhase(key)
        this.currentPhaseKey = key

        // TODO: handle phase end countdown

        currentPhase?.stop()
        phase.start()

        // TODO: handle phase timeout
    }

    fun nextPhase(skipCountdown: Boolean = false) {
        val key = this.getNextPhaseKey()

        // Stop the game if there is no phase left.
        if (key == null) {
            this.stop()
            return
        }

        this.setPhase(key, skipCountdown)
    }

    fun getPhase(key: String) = this.phases[key] ?: error("The given key does not exist for 'phases.*.key'.")

    fun getCurrentPhase() = this.getPhase(this.currentPhaseKey)

    fun stop(skipCountdown: Boolean = false) {
        // Stop current phase
        val currentPhase = if (this::currentPhaseKey.isInitialized) this.getPhase(this.currentPhaseKey) else null
        currentPhase?.stop() // TODO: handle phase end countdown

        // Cleanup and stop the server.
        this.onStop()
    }

    protected open fun getNextPhaseKey() : String? {
        val keys = arrayListOf(*this.phases.keys.toTypedArray())
        val currentIndex = if (this::currentPhaseKey.isInitialized) keys.indexOf(this.currentPhaseKey) else -1
        return keys[currentIndex + 1]
    }

    internal fun addActor(actor: Actor<PlayerType>) = this.actors.add(actor)

    internal fun removeActor(actor: Actor<PlayerType>) = this.actors.remove(actor)

    fun getActors() = this.actors.toTypedArray()

    protected abstract fun onStop()

}
