package com.abeant.gretel

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import com.abeant.gretel.hatch.HatchDetector
import com.abeant.gretel.launch.DoorLauncher
import com.abeant.gretel.ui.ActivityMotion

/**
 * Trampoline HOME. A single Home delivery launches the assigned app.
 * Two deliveries within the hatch window open [HatchActivity].
 * When relaunch-on-close is on, this activity stays under the assigned
 * app and opens it again when that app exits.
 */
class HomeActivity : AppCompatActivity() {
    private var skipNextResume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityMotion.suppressOpen(this)
        skipNextResume = true
        handleHome()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        ActivityMotion.suppressOpen(this)
        skipNextResume = true
        handleHome()
    }

    override fun onResume() {
        super.onResume()
        if (skipNextResume) {
            skipNextResume = false
            return
        }
        val app = application as GretelApp
        val snapshot = app.store.snapshot()
        if (!snapshot.relaunchOnClose || !snapshot.onboardingDone) return
        val assigned = snapshot.assignedPackage
        if (assigned.isNullOrBlank() || !app.catalog.isInstalled(assigned)) return
        DoorLauncher.launch(this, assigned)
    }

    override fun finish() {
        super.finish()
        ActivityMotion.suppressClose(this)
    }

    private fun handleHome() {
        val app = application as GretelApp
        val snapshot = app.store.snapshot()

        if (!snapshot.onboardingDone) {
            openHatch(missingDoor = false)
            return
        }

        val assigned = snapshot.assignedPackage
        if (assigned.isNullOrBlank() || !app.catalog.isInstalled(assigned)) {
            openHatch(missingDoor = true)
            return
        }

        val isBoot = !app.bootLaunchConsumed && SystemClock.elapsedRealtime() < BOOT_WINDOW_MS
        if (isBoot) {
            app.bootLaunchConsumed = true
            if (!snapshot.openOnBoot) {
                openHatch(missingDoor = false)
                return
            }
        }

        when (app.hatchDetector.onHomeDelivery(snapshot.hatchWindowMs)) {
            HatchDetector.Decision.OPEN_HATCH -> openHatch(missingDoor = false)
            HatchDetector.Decision.LAUNCH_ASSIGNED -> {
                val launched = DoorLauncher.launch(this, assigned)
                if (!launched) {
                    openHatch(missingDoor = true)
                } else if (!snapshot.relaunchOnClose) {
                    finish()
                }
            }
        }
    }

    companion object {
        private const val BOOT_WINDOW_MS = 90_000L
    }

    private fun openHatch(missingDoor: Boolean) {
        val intent = Intent(this, HatchActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            if (missingDoor) {
                putExtra(HatchActivity.EXTRA_MISSING_DOOR, true)
            }
        }
        startActivity(intent)
        finish()
    }
}
