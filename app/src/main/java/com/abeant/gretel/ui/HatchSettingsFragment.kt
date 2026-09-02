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
import com.abeant.gretel.hatch.HatchReason

class HatchSettingsFragment : Fragment() {
    private var binding: FragmentHatchSettingsBinding? = null
    private var reason: HatchReason = HatchReason.NONE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = FragmentHatchSettingsBinding.inflate(inflater, container, false)
        binding = view
        val host = requireActivity() as HatchHost
        reason = host.consumeHatchReason()

        AccessibilitySemantics.asButton(view.banner)

        view.appRow.setOnClickListener { host.show(Screen.PickDoor) }
        view.homeRow.setOnClickListener { host.homeRole.requestHomeRole(requireActivity()) }
        view.timingRow.setOnClickListener { host.show(Screen.Timing) }
        view.themeRow.setOnClickListener { host.show(Screen.Theme) }
        view.androidSettingsRow.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        view.getBackRow.setOnClickListener { host.show(Screen.HatchLesson) }
        view.aboutRow.setOnClickListener { host.show(Screen.About) }
        view.bootRow.setOnCheckedChangeListener { host.store.setOpenOnBoot(it) }
        view.relaunchRow.setOnCheckedChangeListener { host.store.setRelaunchOnClose(it) }
        view.primaryButton.setOnClickListener {
            if (host.store.snapshot().assignedPackage.isNullOrBlank()) {
                host.show(Screen.PickDoor)
            } else {
                host.launchAssignedAndLeave()
            }
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
        val missing = assigned == null || !installed
        val isHome = host.homeRole.isDefaultHome()
        val appLabel = if (assigned != null && installed) {
            host.catalog.labelFor(assigned) ?: assigned
        } else {
            null
        }

        view.appRow.value = when {
            assigned == null -> getString(R.string.door_none)
            !installed -> getString(R.string.door_missing, assigned)
            else -> appLabel
        }
        view.homeRow.setValue(if (isHome) R.string.home_status_yes else R.string.home_status_no)
        view.bootRow.isChecked = snapshot.openOnBoot
        view.relaunchRow.isChecked = snapshot.relaunchOnClose
        view.timingRow.setValue(timingLabel(snapshot.hatchWindowMs))
        view.themeRow.setValue(ThemeMode.labelRes(snapshot.themeMode))

        view.primaryButton.text = if (appLabel != null) {
            getString(R.string.return_to_app, appLabel)
        } else {
            getString(R.string.choose_app_label)
        }

        bindBanner(view, host, missing, isHome, appLabel)
    }

    private fun bindBanner(
        view: FragmentHatchSettingsBinding,
        host: HatchHost,
        missing: Boolean,
        isHome: Boolean,
        appLabel: String?,
    ) {
        val banner = view.banner
        when {
            missing -> {
                banner.text = getString(R.string.missing_door_banner)
                banner.setOnClickListener { host.show(Screen.PickDoor) }
            }
            reason == HatchReason.RELAUNCH_LOOP -> {
                banner.text = getString(R.string.relaunch_loop_banner, appLabel)
                banner.setOnClickListener { host.launchAssignedAndLeave() }
            }
            !isHome -> {
                banner.text = getString(R.string.not_home_banner)
                banner.setOnClickListener { host.homeRole.requestHomeRole(requireActivity()) }
            }
            else -> {
                banner.visibility = View.GONE
                return
            }
        }
        banner.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        fun timingLabel(windowMs: Long): Int = when (windowMs) {
            500L -> R.string.timing_quick
            1200L -> R.string.timing_relaxed
            else -> R.string.timing_normal
        }
    }
}
