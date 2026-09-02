package com.abeant.gretel.launch

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.abeant.gretel.ui.ActivityMotion

object DoorLauncher {
    /** The launch intent for [packageName], or null when it cannot be opened. */
    fun launchIntent(context: Context, packageName: String): Intent? {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return null
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        return intent
    }

    fun start(context: Context, intent: Intent): Boolean {
        return try {
            context.startActivity(intent)
            if (context is Activity) {
                ActivityMotion.suppressOpen(context)
            }
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    fun launch(context: Context, packageName: String): Boolean {
        val intent = launchIntent(context, packageName) ?: return false
        return start(context, intent)
    }
}
