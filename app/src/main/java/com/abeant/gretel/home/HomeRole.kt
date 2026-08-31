package com.abeant.gretel.home

import android.app.Activity
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.abeant.gretel.R

/**
 * ROLE_HOME on stock API 29+. Several e-reader firmwares (Onyx BOOX) accept
 * the role-request intent and then do nothing. ACTION_HOME_SETTINGS is the
 * path that actually lets the user pick a home app on those devices, so
 * [requestHomeRole] always opens that settings screen.
 */
class HomeRole(
    private val context: Context,
) {
    fun isDefaultHome(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) return true
            }
        }
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = context.packageManager.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
        return resolved?.activityInfo?.packageName == context.packageName
    }

    /** Opens the system Home-app picker. User must confirm. */
    fun requestHomeRole(activity: Activity) {
        openSystemHomeSettings(activity)
    }

    fun openLegacyHomeChooser(activity: Activity) {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        tryStart(
            activity,
            Intent.createChooser(home, activity.getString(R.string.choose_home_app)),
        )
    }

    fun openSystemHomeSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
        if (intent.resolveActivity(activity.packageManager) != null) {
            if (tryStart(activity, intent)) return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = activity.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (tryStart(activity, roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))) {
                    return
                }
            }
        }
        openLegacyHomeChooser(activity)
    }

    private fun tryStart(activity: Activity, intent: Intent): Boolean {
        return try {
            activity.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }
}
