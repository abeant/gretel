package com.abeant.gretel.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.abeant.gretel.ui.ThemeMode

/**
 * Small, synchronous preference store for Gretel's Home hot path.
 *
 * SharedPreferences is intentional here: the app is single-process, the state is
 * tiny, reads happen while dispatching Home, and [SharedPreferences.Editor.apply]
 * updates the in-memory value immediately while persisting it asynchronously.
 */
class AssignedAppStore(
    private val preferences: SharedPreferences,
) {
    data class Snapshot(
        val assignedPackage: String?,
        val onboardingDone: Boolean,
        val hatchWindowMs: Long,
        val themeMode: String,
        val relaunchOnClose: Boolean,
        val openOnBoot: Boolean,
    )

    fun snapshot(): Snapshot {
        val assigned = preferences.getString(ASSIGNED_PACKAGE, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        val rawWindow = preferences.getLong(HATCH_WINDOW_MS, DEFAULT_HATCH_WINDOW_MS)
        val window = rawWindow.takeIf { it in ALLOWED_HATCH_WINDOWS }
            ?: DEFAULT_HATCH_WINDOW_MS
        val theme = preferences.getString(THEME_MODE, null)?.let(ThemeMode::normalize)
            ?: if (preferences.getBoolean(TRUE_BLACK, false)) ThemeMode.BLACK else ThemeMode.AUTO
        return Snapshot(
            assignedPackage = assigned,
            onboardingDone = preferences.getBoolean(ONBOARDING_DONE, false),
            hatchWindowMs = window,
            themeMode = theme,
            relaunchOnClose = preferences.getBoolean(RELAUNCH_ON_CLOSE, true),
            openOnBoot = preferences.getBoolean(OPEN_ON_BOOT, true),
        )
    }

    fun setAssignedPackage(packageName: String?) {
        val trimmed = packageName?.trim().orEmpty()
        preferences.edit {
            if (trimmed.isEmpty()) remove(ASSIGNED_PACKAGE) else putString(ASSIGNED_PACKAGE, trimmed)
        }
    }

    fun setOnboardingDone(done: Boolean) {
        preferences.edit { putBoolean(ONBOARDING_DONE, done) }
    }

    fun setHatchWindowMs(windowMs: Long) {
        val safe = windowMs.takeIf { it in ALLOWED_HATCH_WINDOWS } ?: DEFAULT_HATCH_WINDOW_MS
        preferences.edit { putLong(HATCH_WINDOW_MS, safe) }
    }

    fun setThemeMode(mode: String) {
        preferences.edit {
            putString(THEME_MODE, ThemeMode.normalize(mode))
            remove(TRUE_BLACK)
        }
    }

    fun setRelaunchOnClose(enabled: Boolean) {
        preferences.edit { putBoolean(RELAUNCH_ON_CLOSE, enabled) }
    }

    fun setOpenOnBoot(enabled: Boolean) {
        preferences.edit { putBoolean(OPEN_ON_BOOT, enabled) }
    }

    companion object {
        const val PREFERENCES_NAME = "gretel"
        const val ASSIGNED_PACKAGE = "assigned_package"
        const val ONBOARDING_DONE = "onboarding_done"
        const val HATCH_WINDOW_MS = "hatch_window_ms"
        const val THEME_MODE = "theme_mode"
        const val TRUE_BLACK = "true_black"
        const val RELAUNCH_ON_CLOSE = "relaunch_on_close"
        const val OPEN_ON_BOOT = "open_on_boot"

        const val DEFAULT_HATCH_WINDOW_MS = 800L
        val ALLOWED_HATCH_WINDOWS = listOf(500L, 800L, 1200L)
    }
}
