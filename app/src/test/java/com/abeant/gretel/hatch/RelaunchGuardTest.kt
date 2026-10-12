package com.abeant.gretel.hatch

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RelaunchGuardTest {
    private var now = 10_000L
    private val guard = RelaunchGuard(clock = { now }, maxStrikes = 3, shortLifeMs = 10_000L)

    private fun appLivesFor(ms: Long) {
        now += ms
    }

    @Test
    fun backingOutOfAWorkingAppIsAlwaysAllowed() {
        guard.noteLaunch()
        repeat(10) {
            appLivesFor(15_000L)
            assertTrue(guard.allowRelaunch())
        }
    }

    @Test
    fun crashLoopTripsAfterThreeShortLives() {
        guard.noteLaunch()
        appLivesFor(1_000L)
        assertTrue(guard.allowRelaunch())
        appLivesFor(1_000L)
        assertTrue(guard.allowRelaunch())
        appLivesFor(1_000L)
        assertFalse(guard.allowRelaunch())
    }

    @Test
    fun quickExitsSeparatedByLongLivesDoNotAccumulate() {
        guard.noteLaunch()
        appLivesFor(1_000L)
        assertTrue(guard.allowRelaunch())
        appLivesFor(1_000L)
        assertTrue(guard.allowRelaunch())
        appLivesFor(60_000L)
        assertTrue(guard.allowRelaunch())
        appLivesFor(1_000L)
        assertTrue(guard.allowRelaunch())
        appLivesFor(1_000L)
        assertTrue(guard.allowRelaunch())
    }

    @Test
    fun firstQuitWithoutKnownLaunchIsNotAStrike() {
        appLivesFor(500L)
        assertTrue(guard.allowRelaunch())
        appLivesFor(500L)
        assertTrue(guard.allowRelaunch())
        appLivesFor(500L)
        assertTrue(guard.allowRelaunch())
        appLivesFor(500L)
        assertFalse(guard.allowRelaunch())
    }

    @Test
    fun trippingAndResetBothStartOver() {
        guard.noteLaunch()
        repeat(2) {
            appLivesFor(500L)
            assertTrue(guard.allowRelaunch())
        }
        appLivesFor(500L)
        assertFalse(guard.allowRelaunch())
        appLivesFor(500L)
        assertTrue(guard.allowRelaunch())
        guard.reset()
        appLivesFor(500L)
        assertTrue(guard.allowRelaunch())
    }
}
