package com.abeant.gretel.hatch

import android.os.SystemClock

/**
 * Stops "reopen if it quits" from looping forever on an app that crashes at
 * startup. Only short-lived launches count: an app that quits within
 * [shortLifeMs] of being opened earns a strike, a longer life clears them, and
 * [maxStrikes] strikes in a row deny the next automatic relaunch. Backing out
 * of a working app a few times therefore never trips the guard.
 *
 * Process-scoped like [HatchDetector]. A deliberate launch from settings calls
 * [reset] so the user can always try again.
 */
class RelaunchGuard(
    private val clock: () -> Long = { SystemClock.elapsedRealtime() },
    private val maxStrikes: Int = DEFAULT_MAX_STRIKES,
    private val shortLifeMs: Long = DEFAULT_SHORT_LIFE_MS,
) {
    private var lastLaunchAt: Long = NONE
    private var strikes = 0

    /** Records a launch the user asked for, so its lifetime can be judged if it quits. */
    fun noteLaunch() {
        lastLaunchAt = clock()
    }

    /** Called when the app has quit and would be relaunched. False when it keeps dying young. */
    fun allowRelaunch(): Boolean {
        val now = clock()
        val shortLived = lastLaunchAt != NONE && now - lastLaunchAt < shortLifeMs
        strikes = if (shortLived) strikes + 1 else 0
        if (strikes >= maxStrikes) {
            reset()
            return false
        }
        lastLaunchAt = now
        return true
    }

    fun reset() {
        strikes = 0
        lastLaunchAt = NONE
    }

    companion object {
        const val DEFAULT_MAX_STRIKES = 3
        const val DEFAULT_SHORT_LIFE_MS = 20_000L
        private const val NONE = -1L
    }
}
