package com.abeant.gretel.launch

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.abeant.gretel.ui.ActivityMotion

object DoorLauncher {
    fun launch(context: Context, packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
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
}
