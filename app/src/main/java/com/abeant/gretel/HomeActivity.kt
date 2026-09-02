package com.abeant.gretel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abeant.gretel.hatch.HatchReason
import com.abeant.gretel.hatch.HomeDispatcher
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
        if (assigned.isNullOrBlank()) return
        val intent = DoorLauncher.launchIntent(this, assigned)
        if (intent == null) {
            openHatch(HatchReason.MISSING_APP)
            return
        }
        if (!app.relaunchGuard.allowRelaunch()) {
            openHatch(HatchReason.RELAUNCH_LOOP)
            return
        }
        if (!DoorLauncher.start(this, intent)) {
            openHatch(HatchReason.MISSING_APP)
        }
    }

    override fun finish() {
        super.finish()
        ActivityMotion.suppressClose(this)
    }

    private fun handleHome() {
        val app = application as GretelApp
        val snapshot = app.store.snapshot()

        var launchIntent: Intent? = null
        val decision = app.dispatcher.decide(snapshot) { packageName ->
            launchIntent = DoorLauncher.launchIntent(this, packageName)
            launchIntent != null
        }

        when (decision) {
            is HomeDispatcher.Decision.OpenHatch -> openHatch(decision.reason)
            is HomeDispatcher.Decision.Launch -> {
                val intent = launchIntent
                val launched = intent != null && DoorLauncher.start(this, intent)
                if (!launched) {
                    openHatch(HatchReason.MISSING_APP)
                } else {
                    app.relaunchGuard.noteLaunch()
                    if (!snapshot.relaunchOnClose) finish()
                }
            }
        }
    }

    private fun openHatch(reason: HatchReason) {
        val intent = Intent(this, HatchActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            putExtra(HatchActivity.EXTRA_REASON, reason.name)
        }
        startActivity(intent)
        finish()
    }
}
