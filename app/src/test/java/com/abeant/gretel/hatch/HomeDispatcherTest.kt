package com.abeant.gretel.hatch

import com.abeant.gretel.MemoryPreferences
import com.abeant.gretel.data.AssignedAppStore
import com.abeant.gretel.hatch.HomeDispatcher.Decision
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeDispatcherTest {
    private var now = 100_000L
    private val store = AssignedAppStore(MemoryPreferences())
    private val detector = HatchDetector { now }
    private val bootGate = BootGate(store, wallClock = { 1_700_000_000_000L + now }, upClock = { now })
    private val dispatcher = HomeDispatcher(detector, bootGate)

    private fun snapshot(
        assigned: String? = "org.koreader.launcher",
        onboardingDone: Boolean = true,
        openOnBoot: Boolean = true,
        windowMs: Long = 800L,
    ) = AssignedAppStore.Snapshot(
        assignedPackage = assigned,
        onboardingDone = onboardingDone,
        hatchWindowMs = windowMs,
        themeMode = "auto",
        relaunchOnClose = true,
        openOnBoot = openOnBoot,
    )

    private val installed: (String) -> Boolean = { true }

    @Test
    fun onboardingFirst() {
        assertEquals(
            Decision.OpenHatch(HatchReason.ONBOARDING),
            dispatcher.decide(snapshot(onboardingDone = false), installed),
        )
    }

    @Test
    fun noAppChosen() {
        assertEquals(
            Decision.OpenHatch(HatchReason.MISSING_APP),
            dispatcher.decide(snapshot(assigned = null), installed),
        )
    }

    @Test
    fun appCannotLaunch() {
        assertEquals(
            Decision.OpenHatch(HatchReason.MISSING_APP),
            dispatcher.decide(snapshot()) { false },
        )
    }

    @Test
    fun singleHomeLaunches() {
        assertEquals(
            Decision.Launch("org.koreader.launcher"),
            dispatcher.decide(snapshot(), installed),
        )
    }

    @Test
    fun doubleHomeOpensHatch() {
        dispatcher.decide(snapshot(), installed)
        now += 500L
        assertEquals(Decision.OpenHatch(HatchReason.NONE), dispatcher.decide(snapshot(), installed))
    }

    @Test
    fun bootWithOpenOnBootLaunches() {
        assertEquals(Decision.Launch("org.koreader.launcher"), dispatcher.decide(snapshot(openOnBoot = true), installed))
    }

    @Test
    fun bootWithoutOpenOnBootLands() {
        assertEquals(
            Decision.OpenHatch(HatchReason.LANDING),
            dispatcher.decide(snapshot(openOnBoot = false), installed),
        )
        now += 5_000L
        assertEquals(
            Decision.Launch("org.koreader.launcher"),
            dispatcher.decide(snapshot(openOnBoot = false), installed),
        )
    }

    @Test
    fun bootPressDoesNotPairWithNextPress() {
        dispatcher.decide(snapshot(openOnBoot = false), installed)
        now += 300L
        assertEquals(
            Decision.Launch("org.koreader.launcher"),
            dispatcher.decide(snapshot(openOnBoot = false), installed),
        )
    }

    @Test
    fun bootIsConsumedEvenDuringOnboarding() {
        dispatcher.decide(snapshot(onboardingDone = false), installed)
        now += 5_000L
        assertEquals(
            Decision.Launch("org.koreader.launcher"),
            dispatcher.decide(snapshot(openOnBoot = false), installed),
        )
    }
}
