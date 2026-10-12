package com.abeant.gretel

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.abeant.gretel.catalog.AppCatalog
import com.abeant.gretel.data.AssignedAppStore
import com.abeant.gretel.hatch.HatchReason
import com.abeant.gretel.home.HomeRole
import com.abeant.gretel.launch.DoorLauncher
import com.abeant.gretel.ui.AboutFragment
import com.abeant.gretel.ui.ActivityMotion
import com.abeant.gretel.ui.ChoiceFragment
import com.abeant.gretel.ui.HatchLessonFragment
import com.abeant.gretel.ui.HatchSettingsFragment
import com.abeant.gretel.ui.LicenceFragment
import com.abeant.gretel.ui.PagedColumn
import com.abeant.gretel.ui.PickDoorFragment
import com.abeant.gretel.ui.SetHomeFragment
import com.abeant.gretel.ui.ThemeMode
import com.abeant.gretel.ui.WelcomeFragment

class HatchActivity : AppCompatActivity(), HatchHost {

    override lateinit var store: AssignedAppStore
        private set

    override lateinit var catalog: AppCatalog
        private set

    override lateinit var homeRole: HomeRole
        private set

    private var hatchReason: HatchReason = HatchReason.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = application as GretelApp
        store = app.store
        catalog = app.catalog
        homeRole = HomeRole(this)
        val snapshot = store.snapshot()
        val black = ThemeMode.isBlack(snapshot.themeMode, ThemeMode.isNight(this))
        setTheme(if (black) R.style.Theme_Gretel_Black else R.style.Theme_Gretel)
        super.onCreate(savedInstanceState)
        ActivityMotion.suppressOpen(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_hatch)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            // Only a fresh delivery carries a reason. A recreate (theme change,
            // rotation) must not resurrect a banner the user already saw.
            hatchReason = reasonFrom(intent)
            if (!snapshot.onboardingDone) {
                show(Screen.Welcome, addToBackStack = false)
            } else {
                show(Screen.Settings, addToBackStack = false)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        hatchReason = reasonFrom(intent)
        val snapshot = store.snapshot()
        if (snapshot.onboardingDone) {
            show(Screen.Settings, addToBackStack = false)
        }
    }

    /** Read once by the screen that shows it, so it does not resurface on later navigation. */
    override fun consumeHatchReason(): HatchReason {
        val reason = hatchReason
        hatchReason = HatchReason.NONE
        return reason
    }

    private fun reasonFrom(intent: Intent?): HatchReason {
        val raw = intent?.getStringExtra(EXTRA_REASON) ?: return HatchReason.NONE
        return HatchReason.entries.firstOrNull { it.name == raw } ?: HatchReason.NONE
    }

    override fun finish() {
        super.finish()
        ActivityMotion.suppressClose(this)
    }

    /** Page keys reach the activity only when no row consumed them. */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val fragmentView = supportFragmentManager.findFragmentById(R.id.container)?.view
        val pager = fragmentView?.let(PagedColumn::find)
        if (pager != null && pager.handleKey(keyCode)) return true
        return super.onKeyDown(keyCode, event)
    }

    override fun show(screen: Screen, addToBackStack: Boolean) {
        val fragment: Fragment = when (screen) {
            Screen.Welcome -> WelcomeFragment()
            Screen.PickDoor -> PickDoorFragment()
            Screen.SetHome -> SetHomeFragment()
            Screen.HatchLesson -> HatchLessonFragment()
            Screen.Settings -> HatchSettingsFragment()
            Screen.About -> AboutFragment()
            Screen.Licence -> LicenceFragment()
            Screen.Timing -> ChoiceFragment.timing()
            Screen.Theme -> ChoiceFragment.theme()
        }
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(0, 0, 0, 0)
            .replace(R.id.container, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(screen.name)
        }
        transaction.commit()
    }

    override fun launchAssignedAndLeave() {
        val app = application as GretelApp
        val assigned = store.snapshot().assignedPackage
        if (!assigned.isNullOrBlank()) {
            app.relaunchGuard.reset()
            DoorLauncher.launch(this, assigned)
        }
        finish()
    }

    companion object {
        const val EXTRA_REASON = "hatch_reason"
    }
}

enum class Screen {
    Welcome,
    PickDoor,
    SetHome,
    HatchLesson,
    Settings,
    About,
    Licence,
    Timing,
    Theme,
}

interface HatchHost {
    val store: AssignedAppStore
    val catalog: AppCatalog
    val homeRole: HomeRole
    fun consumeHatchReason(): HatchReason
    fun show(screen: Screen, addToBackStack: Boolean = true)
    fun launchAssignedAndLeave()
}
