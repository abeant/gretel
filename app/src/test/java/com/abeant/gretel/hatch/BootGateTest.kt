package com.abeant.gretel.hatch

import com.abeant.gretel.MemoryPreferences
import com.abeant.gretel.data.AssignedAppStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BootGateTest {
    private var wall = 1_700_000_000_000L
    private var up = 30_000L

    private fun gate(store: AssignedAppStore = AssignedAppStore(MemoryPreferences())) =
        BootGate(store, wallClock = { wall }, upClock = { up })

    @Test
    fun firstEverHomeCountsAsBoot() {
        val gate = gate()
        assertTrue(gate.consumeFirstHomeSinceBoot())
        assertFalse(gate.consumeFirstHomeSinceBoot())
    }

    @Test
    fun laterPressesInSameBootAreNotBoot() {
        val gate = gate()
        gate.consumeFirstHomeSinceBoot()
        wall += 3_600_000L
        up += 3_600_000L
        assertFalse(gate.consumeFirstHomeSinceBoot())
    }

    @Test
    fun survivesProcessDeath() {
        val store = AssignedAppStore(MemoryPreferences())
        gate(store).consumeFirstHomeSinceBoot()
        wall += 10_000L
        up += 10_000L
        assertFalse(gate(store).consumeFirstHomeSinceBoot())
    }

    @Test
    fun rebootMovesBootIdentity() {
        val store = AssignedAppStore(MemoryPreferences())
        val gate = gate(store)
        gate.consumeFirstHomeSinceBoot()
        // Device was off for a minute, then booted; uptime starts over.
        wall += 60_000L + 45_000L
        up = 45_000L
        assertTrue(gate.consumeFirstHomeSinceBoot())
        assertFalse(gate.consumeFirstHomeSinceBoot())
    }

    @Test
    fun lateUnlockStillCountsAsBoot() {
        val store = AssignedAppStore(MemoryPreferences())
        gate(store).consumeFirstHomeSinceBoot()
        // Reboot, then the user leaves it on the lock screen for ten minutes.
        wall += 120_000L + 600_000L
        up = 600_000L
        assertTrue(gate(store).consumeFirstHomeSinceBoot())
    }

    @Test
    fun smallClockCorrectionIsNotABoot() {
        val gate = gate()
        gate.consumeFirstHomeSinceBoot()
        wall += 2_000L
        assertFalse(gate.consumeFirstHomeSinceBoot())
    }

    @Test
    fun clockSyncDuringALongSessionIsNotABoot() {
        val gate = gate()
        gate.consumeFirstHomeSinceBoot()
        // Three hours in, Wi-Fi connects and NTP moves the clock forward twenty minutes.
        wall += 3 * 3_600_000L
        up += 3 * 3_600_000L
        assertFalse(gate.consumeFirstHomeSinceBoot())
        wall += 20 * 60_000L
        assertFalse(gate.consumeFirstHomeSinceBoot())
    }

    @Test
    fun rebootAfterALongSessionIsCaughtByUptimeGoingBackwards() {
        val store = AssignedAppStore(MemoryPreferences())
        val gate = gate(store)
        gate.consumeFirstHomeSinceBoot()
        wall += 3 * 3_600_000L
        up += 3 * 3_600_000L
        assertFalse(gate.consumeFirstHomeSinceBoot())
        // Reboot; first press ten minutes later. The clock was even set back by hand.
        wall -= 3_600_000L
        up = 600_000L
        assertTrue(gate.consumeFirstHomeSinceBoot())
        assertFalse(gate.consumeFirstHomeSinceBoot())
    }
}
