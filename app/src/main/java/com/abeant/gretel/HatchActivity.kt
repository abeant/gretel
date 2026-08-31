package com.abeant.gretel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.abeant.gretel.catalog.AppCatalog
import com.abeant.gretel.data.AssignedAppStore
import com.abeant.gretel.home.HomeRole
import com.abeant.gretel.launch.DoorLauncher
import com.abeant.gretel.ui.AboutFragment
import com.abeant.gretel.ui.ActivityMotion
import com.abeant.gretel.ui.HatchLessonFragment
import com.abeant.gretel.ui.HatchSettingsFragment
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

    override var missingDoor: Boolean = false
        private set

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

        missingDoor = intent.getBooleanExtra(EXTRA_MISSING_DOOR, false)

        if (savedInstanceState == null) {
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
        missingDoor = intent.getBooleanExtra(EXTRA_MISSING_DOOR, false)
        val snapshot = store.snapshot()
        if (snapshot.onboardingDone) {
            show(Screen.Settings, addToBackStack = false)
        }
    }

    override fun finish() {
        super.finish()
        ActivityMotion.suppressClose(this)
    }

    override fun show(screen: Screen, addToBackStack: Boolean) {
        val fragment: Fragment = when (screen) {
            Screen.Welcome -> WelcomeFragment()
            Screen.PickDoor -> PickDoorFragment()
            Screen.SetHome -> SetHomeFragment()
            Screen.HatchLesson -> HatchLessonFragment()
            Screen.Settings -> HatchSettingsFragment()
            Screen.About -> AboutFragment()
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
        val snapshot = store.snapshot()
        val assigned = snapshot.assignedPackage
        if (!assigned.isNullOrBlank()) {
            DoorLauncher.launch(this, assigned)
        }
        finish()
    }

    companion object {
        const val EXTRA_MISSING_DOOR = "missing_door"
    }
}

enum class Screen {
    Welcome,
    PickDoor,
    SetHome,
    HatchLesson,
    Settings,
    About,
}

interface HatchHost {
    val store: AssignedAppStore
    val catalog: AppCatalog
    val homeRole: HomeRole
    val missingDoor: Boolean
    fun show(screen: Screen, addToBackStack: Boolean = true)
    fun launchAssignedAndLeave()
}
