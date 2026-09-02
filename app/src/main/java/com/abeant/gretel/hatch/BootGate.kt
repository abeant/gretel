package com.abeant.gretel.hatch

import android.os.SystemClock
import com.abeant.gretel.data.AssignedAppStore
import kotlin.math.abs

/**
 * Detects the first Home delivery after a reboot without a boot receiver.
 *
 * Two persisted facts: the boot identity (wall clock minus uptime, which only
 * moves on a reboot or a clock correction) and the uptime at the last Home
 * press. Uptime going backwards is a reboot for certain. A boot-identity shift
 * counts as a reboot only when it is at least as large as the previous
 * session's uptime, because a reboot cannot shift it by less than that; a
 * smaller shift is a clock correction, such as an NTP sync during a session.
 *
 * Known limit: a clock correction soon after boot on a device without a
 * battery-backed clock looks exactly like a reboot, because the clock error
 * equals the time the device was off. Without a boot receiver the two cannot
 * be told apart. The only effect is one extra landing screen when "open after
 * a restart" is off; with it on, nothing changes.
 */
class BootGate(
    private val store: AssignedAppStore,
    private val wallClock: () -> Long = { System.currentTimeMillis() },
    private val upClock: () -> Long = { SystemClock.elapsedRealtime() },
    private val toleranceMs: Long = DEFAULT_TOLERANCE_MS,
) {
    /**
     * True on the first call after a reboot, or on the very first call ever.
     * Records the boot so later calls in the same session return false.
     */
    fun consumeFirstHomeSinceBoot(): Boolean {
        val uptime = upClock()
        val bootId = wallClock() - uptime
        val storedBoot = store.lastBootId()
        val storedUptime = store.lastUptimeMs() ?: 0L

        val rebooted = when {
            storedBoot == null -> true
            uptime < storedUptime -> true
            else -> {
                val shift = abs(bootId - storedBoot)
                shift > toleranceMs && shift + toleranceMs >= storedUptime
            }
        }
        if (rebooted) store.setLastBootId(bootId)
        store.setLastUptimeMs(uptime)
        return rebooted
    }

    companion object {
        const val DEFAULT_TOLERANCE_MS = 5_000L
    }
}
