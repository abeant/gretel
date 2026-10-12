package com.abeant.gretel.data

import android.content.SharedPreferences
import com.abeant.gretel.MemoryPreferences
import com.abeant.gretel.ui.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AssignedAppStoreTest {
    private fun store(preferences: SharedPreferences = MemoryPreferences()) =
        AssignedAppStore(preferences)

    @Test
    fun defaults() {
        val snap = store().snapshot()
        assertNull(snap.assignedPackage)
        assertFalse(snap.onboardingDone)
        assertEquals(AssignedAppStore.DEFAULT_HATCH_WINDOW_MS, snap.hatchWindowMs)
        assertEquals(ThemeMode.AUTO, snap.themeMode)
        assertTrue(snap.relaunchOnClose)
        assertTrue(snap.openOnBoot)
    }

    @Test
    fun persistAssignedPackage() {
        val store = store()
        store.setAssignedPackage("org.ko.reader")
        assertEquals("org.ko.reader", store.snapshot().assignedPackage)
        store.setAssignedPackage("  ")
        assertNull(store.snapshot().assignedPackage)
    }

    @Test
    fun persistOnboarding() {
        val store = store()
        store.setOnboardingDone(true)
        assertTrue(store.snapshot().onboardingDone)
        store.setOnboardingDone(false)
        assertFalse(store.snapshot().onboardingDone)
    }

    @Test
    fun persistHatchWindow() {
        val store = store()
        store.setHatchWindowMs(500L)
        assertEquals(500L, store.snapshot().hatchWindowMs)
        store.setHatchWindowMs(1200L)
        assertEquals(1200L, store.snapshot().hatchWindowMs)
        store.setHatchWindowMs(999L)
        assertEquals(AssignedAppStore.DEFAULT_HATCH_WINDOW_MS, store.snapshot().hatchWindowMs)
    }

    @Test
    fun persistThemeMode() {
        val store = store()
        store.setThemeMode(ThemeMode.BLACK)
        assertEquals(ThemeMode.BLACK, store.snapshot().themeMode)
        store.setThemeMode(ThemeMode.WHITE)
        assertEquals(ThemeMode.WHITE, store.snapshot().themeMode)
        store.setThemeMode("nope")
        assertEquals(ThemeMode.AUTO, store.snapshot().themeMode)
    }

    @Test
    fun migrateTrueBlackToThemeMode() {
        val preferences = MemoryPreferences()
        preferences.edit().putBoolean(AssignedAppStore.TRUE_BLACK, true).apply()
        assertEquals(ThemeMode.BLACK, store(preferences).snapshot().themeMode)
    }

    @Test
    fun persistBootId() {
        val store = store()
        assertNull(store.lastBootId())
        store.setLastBootId(123_456L)
        assertEquals(123_456L, store.lastBootId())
        assertNull(store.lastUptimeMs())
        store.setLastUptimeMs(42L)
        assertEquals(42L, store.lastUptimeMs())
    }

    @Test
    fun persistBehaviorToggles() {
        val store = store()
        store.setOpenOnBoot(false)
        store.setRelaunchOnClose(false)
        assertFalse(store.snapshot().openOnBoot)
        assertFalse(store.snapshot().relaunchOnClose)
        store.setOpenOnBoot(true)
        store.setRelaunchOnClose(true)
        assertTrue(store.snapshot().openOnBoot)
        assertTrue(store.snapshot().relaunchOnClose)
    }
}
