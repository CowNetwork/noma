package network.cow.minigame.noma.api

import network.cow.minigame.noma.api.actor.Actor
import network.cow.minigame.noma.api.actor.ActorProvider
import network.cow.minigame.noma.api.config.ActorProviderConfig
import network.cow.minigame.noma.api.config.GameConfig
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.phase.Phase

/**
 * @author Benedikt Wüller
 */
abstract class Game<PlayerType : Any>(val config: GameConfig<PlayerType>, val phaseConfigs: List<PhaseConfig<PlayerType>>) {

    private val actors = mutableListOf<Actor<PlayerType>>()

    private val phases = LinkedHashMap<String, Phase<PlayerType, *>>()
    private lateinit var currentPhaseKey: String

    private var switchTimer: CountdownTimer? = null
    private var timeoutTimer: CountdownTimer? = null

    var isStopping = false; private set

    val actorProvider: ActorProvider<PlayerType> = this.config.actorProvider.kind
        .getDeclaredConstructor(Game::class.java, ActorProviderConfig::class.java)
        .newInstance(this, this.config.actorProvider)

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
        if (this.isStopping) return

        this.switchTimer?.reset()
        this.switchTimer = null

        this.timeoutTimer?.reset()
        this.timeoutTimer = null

        val currentPhase = if (this::currentPhaseKey.isInitialized) this.getPhase(this.currentPhaseKey) else null
        val phase = this.getPhase(key)
        this.currentPhaseKey = key

        val duration = if (skipCountdown) 0 else (currentPhase?.config?.phaseEndCountdown?.duration ?: 0)
        this.switchTimer = this.createCountdownTimer(duration).onDone {
            currentPhase?.stop()
            phase.start()

            this.timeoutTimer = this.createCountdownTimer(phase.config.timeout.duration).silent().onDone {
                phase.timeout()
                this.nextPhase()
            }.start()
        }.start()
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
        if (this.isStopping) return
        this.isStopping = true

        this.switchTimer?.reset()
        this.switchTimer = null

        val currentPhase = if (this::currentPhaseKey.isInitialized) this.getPhase(this.currentPhaseKey) else null

        val duration = if (skipCountdown) 0 else (currentPhase?.config?.phaseEndCountdown?.duration ?: 0)
        this.switchTimer = this.createCountdownTimer(duration).onDone {
            // Stop current phase
            currentPhase?.stop()

            // Cleanup and stop the server.
            this.onStop()
        }.start()
    }

    protected open fun getNextPhaseKey() : String? {
        val keys = arrayListOf(*this.phases.keys.toTypedArray())
        val currentIndex = if (this::currentPhaseKey.isInitialized) keys.indexOf(this.currentPhaseKey) else -1
        val nextIndex = currentIndex + 1
        return if (nextIndex > keys.lastIndex) null else keys[nextIndex]
    }

    fun getPlayers() : Set<PlayerType> {
        val players = mutableSetOf<PlayerType>()
        this.actors.forEach { players.addAll(it.getPlayers()) }
        return players
    }

    internal fun addActor(actor: Actor<PlayerType>) = this.actors.add(actor)

    internal fun removeActor(actor: Actor<PlayerType>) = this.actors.remove(actor)

    fun getActors() = this.actors.toTypedArray()

    protected abstract fun onStop()

    protected abstract fun createCountdownTimer(duration: Long) : CountdownTimer

}
