package com.abeant.gretel.data

import android.content.SharedPreferences
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

private class MemoryPreferences : SharedPreferences {
    private val values = linkedMapOf<String, Any?>()
    private val listeners = linkedSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    override fun getAll(): Map<String, *> = values.toMap()
    override fun getString(key: String, defValue: String?): String? = values[key] as? String ?: defValue
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? =
        (values[key] as? Set<String>)?.toSet() ?: defValues
    override fun getInt(key: String, defValue: Int): Int = values[key] as? Int ?: defValue
    override fun getLong(key: String, defValue: Long): Long = values[key] as? Long ?: defValue
    override fun getFloat(key: String, defValue: Float): Float = values[key] as? Float ?: defValue
    override fun getBoolean(key: String, defValue: Boolean): Boolean = values[key] as? Boolean ?: defValue
    override fun contains(key: String): Boolean = values.containsKey(key)
    override fun edit(): SharedPreferences.Editor = Editor()
    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ) {
        listeners += listener
    }
    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ) {
        listeners -= listener
    }

    private inner class Editor : SharedPreferences.Editor {
        private val updates = linkedMapOf<String, Any?>()
        private val removals = linkedSetOf<String>()
        private var clearFirst = false

        override fun putString(key: String, value: String?) = update(key, value)
        override fun putStringSet(key: String, values: Set<String>?) = update(key, values?.toSet())
        override fun putInt(key: String, value: Int) = update(key, value)
        override fun putLong(key: String, value: Long) = update(key, value)
        override fun putFloat(key: String, value: Float) = update(key, value)
        override fun putBoolean(key: String, value: Boolean) = update(key, value)
        override fun remove(key: String): SharedPreferences.Editor = apply {
            removals += key
            updates -= key
        }
        override fun clear(): SharedPreferences.Editor = apply { clearFirst = true }
        override fun commit(): Boolean {
            applyChanges()
            return true
        }
        override fun apply() = applyChanges()

        private fun update(key: String, value: Any?): SharedPreferences.Editor = apply {
            updates[key] = value
            removals -= key
        }

        private fun applyChanges() {
            val changed = linkedSetOf<String>()
            if (clearFirst) {
                changed += values.keys
                values.clear()
            }
            removals.forEach {
                if (values.remove(it) != null) changed += it
            }
            updates.forEach { (key, value) ->
                if (value == null) values.remove(key) else values[key] = value
                changed += key
            }
            changed.forEach { key -> listeners.forEach { it.onSharedPreferenceChanged(this@MemoryPreferences, key) } }
        }
    }
}
