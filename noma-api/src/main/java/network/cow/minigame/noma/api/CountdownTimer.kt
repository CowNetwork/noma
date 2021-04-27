package network.cow.minigame.noma.api

/**
 * @author Benedikt Wüller
 */
abstract class CountdownTimer(var duration: Long, baseTranslationKey: String = Translations.COUNTDOWN_MESSAGE_GENERIC_BASE) {

    protected val singularTranslationKey = "$baseTranslationKey.singular"
    protected val pluralTranslationKey = "$baseTranslationKey.plural"

    var onDone: () -> Unit = {}
    var onTick: (Long) -> Unit = {}

    var isSilent: Boolean = false

    var displayIntervals: Array<Long> = arrayOf(
        3600, 1800, 1200, 600, 300, 240, 180, 120, 60, // minutes: 60, 30, 20, 10, 5, 4, 3, 2, 1
        30, 20, 10, 5, 4, 3, 2, 1 // seconds: 30, 20, 10, 5, 4, 3, 2, 1
    )

    var secondsLeft = this.duration; private set

    var isRunning = false; private set

    fun onDone(callback: () -> Unit) : CountdownTimer {
        this.onDone = callback
        return this
    }

    fun onTick(callback: (Long) -> Unit) : CountdownTimer {
        this.onTick = callback
        return this
    }

    fun silent(silent: Boolean = true) : CountdownTimer {
        this.isSilent = silent
        return this
    }

    fun displayIntervals(vararg intervals: Long) : CountdownTimer {
        this.displayIntervals = intervals.toTypedArray()
        return this
    }

    fun start() : CountdownTimer {
        if (this.isRunning) return this
        this.isRunning = true

        if (this.secondsLeft <= 0) {
            this.onTick(this.secondsLeft)
            this.onDone()
            this.reset()
        } else {
            if (this.displayIntervals.contains(this.duration) && !this.isSilent) {
                this.displayTime(this.duration)
            }
            this.onStartTimer()
        }

        return this
    }

    fun reset() {
        if (!this.isRunning) return
        this.onResetTimer()
        this.secondsLeft = this.duration
        this.isRunning = false
    }

    fun restart() {
        this.reset()
        this.start()
    }

    protected fun decrement() {
        if (!this.isRunning) return
        this.secondsLeft -= 1
        this.onTick(this.secondsLeft)

        if (this.secondsLeft <= 0) {
            this.onDone()
            this.reset()
            return
        }

        if (!this.isSilent && this.displayIntervals.contains(this.secondsLeft)) {
            this.displayTime(this.secondsLeft)
        }
    }

    protected abstract fun displayTime(secondsLeft: Long)

    protected abstract fun onStartTimer()

    protected abstract fun onResetTimer()

}
