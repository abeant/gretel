package com.abeant.gretel.hatch

import android.os.SystemClock

/**
 * Double-Home window. Two CATEGORY_HOME deliveries within [windowMs] open the
 * Hatch; a single delivery launches the assigned app.
 *
 * The detector is process-scoped (see [com.abeant.gretel.GretelApp]) because
 * HomeActivity finishes after each delivery and the next Home is a new instance.
 */
class HatchDetector(
    private val clock: () -> Long = { SystemClock.elapsedRealtime() },
) {
    enum class Decision {
        LAUNCH_ASSIGNED,
        OPEN_HATCH,
    }

    private var lastDeliveryAt: Long = NONE

    fun onHomeDelivery(windowMs: Long): Decision {
        val now = clock()
        val last = lastDeliveryAt
        val isDouble = last != NONE && now >= last && (now - last) <= windowMs
        lastDeliveryAt = if (isDouble) NONE else now
        return if (isDouble) Decision.OPEN_HATCH else Decision.LAUNCH_ASSIGNED
    }

    fun reset() {
        lastDeliveryAt = NONE
    }

    companion object {
        private const val NONE = -1L
    }
}
