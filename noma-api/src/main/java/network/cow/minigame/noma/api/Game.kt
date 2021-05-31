package network.cow.minigame.noma.api

import network.cow.minigame.noma.api.actor.Actor
import network.cow.minigame.noma.api.actor.ActorProvider
import network.cow.minigame.noma.api.config.ActorProviderConfig
import network.cow.minigame.noma.api.config.GameConfig
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.config.PoolConfig
import network.cow.minigame.noma.api.phase.Phase
import network.cow.minigame.noma.api.pool.Pool
import network.cow.minigame.noma.api.state.Store

/**
 * @author Benedikt Wüller
 */
abstract class Game<PlayerType : Any>(
    val config: GameConfig<PlayerType>,
    val phaseConfigs: List<PhaseConfig<PlayerType>>,
    val poolConfigs: List<PoolConfig<PlayerType>>
) {

    private val actors = mutableMapOf<String, Actor<PlayerType>>()

    private val phases = LinkedHashMap<String, Phase<PlayerType>>()
    private lateinit var currentPhaseKey: String

    private val pools = mutableMapOf<String, Pool<PlayerType, *>>()

    private var switchTimer: CountdownTimer? = null
    private var timeoutTimer: CountdownTimer? = null

    var state: GameState = GameState(); private set

    var isStopping = false; private set

    val actorProvider: ActorProvider<PlayerType> = this.config.actorProvider.kind
        .getDeclaredConstructor(Game::class.java, ActorProviderConfig::class.java)
        .newInstance(this, this.config.actorProvider)

    val store: Store = Store()

    init {
        if (this.phaseConfigs.isEmpty()) error("There must be at least one phase configured.")

        this.poolConfigs.forEach {
            this.pools[it.key] = it.kind.getDeclaredConstructor(Game::class.java, PoolConfig::class.java).newInstance(this, it)
        }

        this.phaseConfigs.forEach {
            this.phases[it.key] = it.kind.getDeclaredConstructor(Game::class.java, PhaseConfig::class.java).newInstance(this, it)
        }
    }

    fun start() {
        if (this::currentPhaseKey.isInitialized) return
        // Set initial phase.
        this.nextPhase()
    }

    fun updateState(state: GameState) {
        this.state = state
        // TODO: trigger instance update
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

        currentPhase?.stop()
        val duration = if (skipCountdown) 0 else (currentPhase?.config?.phaseEndCountdown?.duration ?: 0)
        this.switchTimer = this.createCountdownTimer(duration, Translations.COUNTDOWN_MESSAGE_PHASE_END_BASE).onDone {
            this.onSetPhase(currentPhase, phase)
            phase.start()

            this.state.currentPhase = Phase(key, phase.config.timeout.duration)
            this.updateState(this.state)

            this.timeoutTimer = this.createCountdownTimer(phase.config.timeout.duration, Translations.COUNTDOWN_MESSAGE_PHASE_TIMEOUT_BASE)
                .silent(phase.config.timeout.silent)
                .onDone {
                    phase.timeout()
                    this.nextPhase()
                }
                .onTick {
                    this.state.currentPhase?.secondsLeft = it
                    this.updateState(this.state)
                }
                .start()
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

    fun getPhase(key: String) = this.phases[key] ?: error("The phase with key '$key' does not exist for 'phases.*.key'.")

    fun getCurrentPhase() = this.getPhase(this.currentPhaseKey)

    fun getPool(key: String) = this.pools[key] ?: error("The pool with the given key '$key' does not exist for 'pools.*.key'.")

    fun <T : Any> getTypedPool(key: String) = this.getPool(key) as Pool<PlayerType, T>

    fun stop(skipCountdown: Boolean = false) {
        if (this.isStopping) return
        this.isStopping = true

        this.switchTimer?.reset()
        this.switchTimer = null

        val currentPhase = if (this::currentPhaseKey.isInitialized) this.getPhase(this.currentPhaseKey) else null

        // Stop current phase
        currentPhase?.stop()
        val duration = if (skipCountdown) 0 else (currentPhase?.config?.phaseEndCountdown?.duration ?: 0)
        this.switchTimer = this.createCountdownTimer(duration, Translations.COUNTDOWN_MESSAGE_SHUTDOWN_BASE).onDone {
            this.onSetPhase(currentPhase, null)

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

    fun getIngamePlayers() : Set<PlayerType> {
        val players = mutableSetOf<PlayerType>()
        this.actors.forEach { players.addAll(it.value.getPlayers()) }
        return players
    }

    internal fun addActor(actor: Actor<PlayerType>) { this.actors[actor.key] = actor }

    internal fun removeActor(actor: Actor<PlayerType>) = this.actors.values.remove(actor)

    fun getActors() = this.actors.values.toTypedArray()

    fun getActor(key: String) = this.actors[key]

    fun getActor(player: PlayerType) = this.actors.entries.firstOrNull { it.value.getPlayers().contains(player) }?.value

    protected abstract fun onStop()

    protected abstract fun onSetPhase(oldPhase: Phase<PlayerType>?, newPhase: Phase<PlayerType>?)

    protected abstract fun createCountdownTimer(duration: Long, baseTranslationKey: String = Translations.COUNTDOWN_MESSAGE_GENERIC_BASE) : CountdownTimer

}
