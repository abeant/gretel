package com.abeant.gretel

import android.app.NotificationManager
import android.app.Application
import android.content.Context
import com.abeant.gretel.catalog.AppCatalog
import com.abeant.gretel.data.AssignedAppStore
import com.abeant.gretel.hatch.HatchDetector

class GretelApp : Application() {
    lateinit var store: AssignedAppStore
        private set

    lateinit var catalog: AppCatalog
        private set

    val hatchDetector = HatchDetector()

    @Volatile
    var bootLaunchConsumed: Boolean = false

    override fun onCreate() {
        super.onCreate()
        store = AssignedAppStore(
            getSharedPreferences(AssignedAppStore.PREFERENCES_NAME, Context.MODE_PRIVATE),
        )
        catalog = AppCatalog(packageManager, packageName)

        // Version 0.1 briefly posted a permanent settings shortcut. It was not
        // portable across e-reader firmware and was not an ongoing user task.
        val notifications = getSystemService(NotificationManager::class.java)
        notifications?.cancel(LEGACY_SETTINGS_NOTICE_ID)
        notifications?.deleteNotificationChannel(LEGACY_SETTINGS_NOTICE_CHANNEL_ID)
    }

    private companion object {
        const val LEGACY_SETTINGS_NOTICE_ID = 1
        const val LEGACY_SETTINGS_NOTICE_CHANNEL_ID = "gretel_settings"
    }
}
