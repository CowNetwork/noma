package network.cow.minigame.noma.api

import network.cow.minigame.noma.api.actor.Actor
import network.cow.minigame.noma.api.actor.ActorProvider
import network.cow.minigame.noma.api.config.ActorProviderConfig
import network.cow.minigame.noma.api.config.GameConfig
import network.cow.minigame.noma.api.config.PhaseConfig
import network.cow.minigame.noma.api.config.PoolConfig
import network.cow.minigame.noma.api.phase.Phase
import network.cow.minigame.noma.api.pool.Pool

/**
 * @author Benedikt Wüller
 */
abstract class Game<PlayerType : Any>(
    val config: GameConfig<PlayerType>,
    val phaseConfigs: List<PhaseConfig<PlayerType>>,
    val poolConfigs: List<PoolConfig<PlayerType>>
) {

    private val actors = mutableListOf<Actor<PlayerType>>()

    private val phases = LinkedHashMap<String, Phase<PlayerType, *>>()
    private lateinit var currentPhaseKey: String

    private val pools = mutableMapOf<String, Pool<PlayerType, *>>()

    private var switchTimer: CountdownTimer? = null
    private var timeoutTimer: CountdownTimer? = null

    var isStopping = false; private set

    val actorProvider: ActorProvider<PlayerType> = this.config.actorProvider.kind
        .getDeclaredConstructor(Game::class.java, ActorProviderConfig::class.java)
        .newInstance(this, this.config.actorProvider)

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
        this.switchTimer = this.createCountdownTimer(duration, currentPhase?.config?.key).onDone {
            currentPhase?.stop()
            this.onSetPhase(currentPhase, phase)
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

    fun <T : Any> getPhase(key: String, type: Class<out Phase<PlayerType, T>>) : Phase<PlayerType, T> = type.cast(this.getPhase(key))

    fun getCurrentPhase() = this.getPhase(this.currentPhaseKey)

    fun getPhaseResult(key: String) = this.getPhase(key).getResult()

    fun getPool(key: String) = this.pools[key] ?: error("The given key does not exist for 'pools.*.key'.")

    fun <T : Any> getTypedPool(key: String) = this.getPool(key) as Pool<PlayerType, T>

    fun <T : Any> getPhaseResult(key: String, type: Class<out Phase<PlayerType, T>>) : T = this.getPhase(key, type).getResult()

    fun stop(skipCountdown: Boolean = false) {
        if (this.isStopping) return
        this.isStopping = true

        this.switchTimer?.reset()
        this.switchTimer = null

        val currentPhase = if (this::currentPhaseKey.isInitialized) this.getPhase(this.currentPhaseKey) else null

        val duration = if (skipCountdown) 0 else (currentPhase?.config?.phaseEndCountdown?.duration ?: 0)
        this.switchTimer = this.createCountdownTimer(duration, currentPhase?.config?.key).onDone {
            // Stop current phase
            currentPhase?.stop()
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

    fun getPlayers() : Set<PlayerType> {
        val players = mutableSetOf<PlayerType>()
        this.actors.forEach { players.addAll(it.getPlayers()) }
        return players
    }

    internal fun addActor(actor: Actor<PlayerType>) = this.actors.add(actor)

    internal fun removeActor(actor: Actor<PlayerType>) = this.actors.remove(actor)

    fun getActors() = this.actors.toTypedArray()

    fun getActor(player: PlayerType) = this.actors.firstOrNull { it.getPlayers().contains(player) }

    protected abstract fun onStop()

    protected abstract fun onSetPhase(oldPhase: Phase<PlayerType, *>?, newPhase: Phase<PlayerType, *>?)

    protected abstract fun createCountdownTimer(duration: Long, name: String? = null) : CountdownTimer

}
