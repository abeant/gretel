package com.abeant.gretel.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.abeant.gretel.HatchHost
import com.abeant.gretel.R
import com.abeant.gretel.Screen
import com.abeant.gretel.databinding.FragmentHatchSettingsBinding
import com.abeant.gretel.launch.DoorLauncher

class HatchSettingsFragment : Fragment() {
    private var binding: FragmentHatchSettingsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = FragmentHatchSettingsBinding.inflate(inflater, container, false)
        binding = view
        val host = requireActivity() as HatchHost

        listOf(
            view.pickNewDoorBanner,
            view.changeDoorButton,
            view.openNowButton,
            view.getBackRow,
            view.replayIntroRow,
            view.themeRow,
            view.homeRow,
            view.androidSettingsRow,
            view.aboutButton,
        ).forEach(AccessibilitySemantics::asButton)
        AccessibilitySemantics.asToggle(view.bootRow) { view.bootToggle.isChecked }
        AccessibilitySemantics.asToggle(view.relaunchRow) { view.relaunchToggle.isChecked }
        view.openNowButton.contentDescription = getString(R.string.open_now)
        view.bootRow.contentDescription = getString(R.string.open_on_boot)
        view.relaunchRow.contentDescription = getString(R.string.relaunch_on_close)
        view.getBackRow.contentDescription = getString(R.string.hatch_lesson_title)
        view.replayIntroRow.contentDescription = getString(R.string.replay_intro)
        view.androidSettingsRow.contentDescription = getString(R.string.android_settings)
        view.aboutButton.contentDescription = getString(R.string.about_license)

        view.changeDoorButton.setOnClickListener { host.show(Screen.PickDoor) }
        view.homeRow.setOnClickListener {
            host.homeRole.requestHomeRole(requireActivity())
        }
        view.openNowButton.setOnClickListener {
            val assigned = host.store.snapshot().assignedPackage
            if (!assigned.isNullOrBlank()) {
                DoorLauncher.launch(requireContext(), assigned)
            } else {
                host.show(Screen.PickDoor)
            }
        }
        view.androidSettingsRow.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        view.aboutButton.setOnClickListener { host.show(Screen.About) }
        view.getBackRow.setOnClickListener { host.show(Screen.HatchLesson) }
        view.replayIntroRow.setOnClickListener {
            host.store.setOnboardingDone(false)
            host.show(Screen.Welcome)
        }
        view.bootRow.setOnClickListener { view.bootToggle.toggle() }
        view.relaunchRow.setOnClickListener { view.relaunchToggle.toggle() }
        view.themeRow.setOnClickListener {
            val current = host.store.snapshot().themeMode
            host.store.setThemeMode(ThemeMode.next(current))
            requireActivity().recreate()
        }

        view.window500.setOnClickListener { setWindow(500L) }
        view.window800.setOnClickListener { setWindow(800L) }
        view.window1200.setOnClickListener { setWindow(1200L) }

        view.pickNewDoorBanner.setOnClickListener { host.show(Screen.PickDoor) }
        view.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        view.relaunchToggle.setOnCheckedChangeListener { _, checked ->
            host.store.setRelaunchOnClose(checked)
        }
        view.bootToggle.setOnCheckedChangeListener { _, checked ->
            host.store.setOpenOnBoot(checked)
        }
        return view.root
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val view = binding ?: return
        val host = requireActivity() as HatchHost
        val snapshot = host.store.snapshot()
        val assigned = snapshot.assignedPackage
        val installed = assigned != null && host.catalog.isInstalled(assigned)
        val missing = host.missingDoor || assigned == null || !installed
        val isHome = host.homeRole.isDefaultHome()

        view.pickNewDoorBanner.visibility = if (missing) View.VISIBLE else View.GONE

        val doorLabel = when {
            assigned == null -> getString(R.string.door_none)
            !installed -> getString(R.string.door_missing, assigned)
            else -> host.catalog.labelFor(assigned) ?: assigned
        }
        view.doorValue.text = doorLabel
        view.changeDoorButton.contentDescription = getString(
            R.string.setting_with_value,
            getString(R.string.section_door),
            doorLabel,
        )

        view.homeValue.text = getString(
            if (isHome) R.string.home_status_yes else R.string.home_status_no,
        )
        view.homeRow.contentDescription = getString(
            R.string.setting_with_value,
            getString(R.string.section_home),
            view.homeValue.text,
        )

        selectWindow(view, snapshot.hatchWindowMs)
        view.themeValue.setText(ThemeMode.labelRes(snapshot.themeMode))
        view.themeRow.contentDescription = getString(
            R.string.setting_with_value,
            getString(R.string.section_display),
            view.themeValue.text,
        )

        view.relaunchToggle.setOnCheckedChangeListener(null)
        view.relaunchToggle.isChecked = snapshot.relaunchOnClose
        view.relaunchToggle.setOnCheckedChangeListener { _, checked ->
            host.store.setRelaunchOnClose(checked)
        }
        view.bootToggle.setOnCheckedChangeListener(null)
        view.bootToggle.isChecked = snapshot.openOnBoot
        view.bootToggle.setOnCheckedChangeListener { _, checked ->
            host.store.setOpenOnBoot(checked)
        }
    }

    private fun selectWindow(view: FragmentHatchSettingsBinding, windowMs: Long) {
        view.window500.isSelected = windowMs == 500L
        view.window800.isSelected = windowMs == 800L
        view.window1200.isSelected = windowMs == 1200L
    }

    private fun setWindow(windowMs: Long) {
        val host = requireActivity() as HatchHost
        host.store.setHatchWindowMs(windowMs)
        refresh()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
