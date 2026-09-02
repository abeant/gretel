package com.abeant.gretel.ui

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.abeant.gretel.HatchHost
import com.abeant.gretel.R
import com.abeant.gretel.Screen
import com.abeant.gretel.catalog.LaunchableApp
import com.abeant.gretel.databinding.FragmentPickDoorBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * App picker. Every app is a row inside a [PagedColumn]; search hides rows
 * rather than rebuilding them, so paging stays stable and nothing animates.
 */
class PickDoorFragment : Fragment() {
    private var binding: FragmentPickDoorBinding? = null
    private var selectedPackage: String? = null
    private var rows: List<AppRow> = emptyList()

    private class AppRow(val app: LaunchableApp, val view: View) {
        val badge: TextView = view.findViewById(R.id.selectedBadge)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = FragmentPickDoorBinding.inflate(inflater, container, false)
        binding = view
        val host = requireActivity() as HatchHost

        StepMark.bind(view.stepRow.root as ViewGroup, 2)
        val snapshot = host.store.snapshot()
        selectedPackage = savedInstanceState?.getString(STATE_SELECTED) ?: snapshot.assignedPackage
        view.confirmButton.isEnabled = selectedPackage != null

        view.search.doAfterTextChanged { applyFilter(it?.toString().orEmpty()) }
        view.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        view.confirmButton.setOnClickListener {
            val chosen = selectedPackage ?: return@setOnClickListener
            host.store.setAssignedPackage(chosen)
            if (snapshot.onboardingDone) {
                host.show(Screen.Settings, addToBackStack = false)
            } else {
                host.show(Screen.HatchLesson)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val loaded = withContext(Dispatchers.Default) {
                host.catalog.listLaunchable()
            }
            if (selectedPackage == null) {
                selectedPackage = host.catalog.preferredDefault(loaded)?.packageName
            }
            buildRows(loaded)
            applyFilter(view.search.text?.toString().orEmpty())
            view.confirmButton.isEnabled = selectedPackage != null
        }
        return view.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_SELECTED, selectedPackage)
    }

    private fun buildRows(apps: List<LaunchableApp>) {
        val view = binding ?: return
        val pager = view.appPager
        val inflater = LayoutInflater.from(requireContext())
        rows = apps.map { app ->
            val rowView = inflater.inflate(R.layout.item_app, pager, false)
            AccessibilitySemantics.asButton(rowView)
            rowView.findViewById<TextView>(R.id.appLabel).text = app.label
            val icon = rowView.findViewById<ImageView>(R.id.appIcon)
            if (app.icon != null) {
                icon.setImageDrawable(app.icon)
                icon.colorFilter = EINK_ICON_FILTER
            } else {
                icon.visibility = View.INVISIBLE
            }
            rowView.setOnClickListener { select(app.packageName) }
            pager.addView(rowView)
            AppRow(app, rowView)
        }
        rows.forEach { bindSelection(it) }
    }

    private fun select(packageName: String) {
        if (selectedPackage == packageName) return
        selectedPackage = packageName
        rows.forEach { bindSelection(it) }
        binding?.confirmButton?.isEnabled = true
    }

    private fun bindSelection(row: AppRow) {
        val selected = row.app.packageName == selectedPackage
        row.view.isSelected = selected
        row.badge.visibility = if (selected) View.VISIBLE else View.GONE
        row.view.contentDescription = if (selected) {
            getString(R.string.app_item_selected, row.app.label)
        } else {
            row.app.label
        }
    }

    private fun applyFilter(query: String) {
        val view = binding ?: return
        val needle = query.trim()
        var shown = 0
        rows.forEach { row ->
            val matches = needle.isEmpty() ||
                row.app.label.contains(needle, ignoreCase = true) ||
                row.app.packageName.contains(needle, ignoreCase = true)
            row.view.visibility = if (matches) View.VISIBLE else View.GONE
            if (matches) shown++
        }
        view.emptyLabel.visibility = if (rows.isNotEmpty() && shown == 0) View.VISIBLE else View.GONE
        view.appPager.goTo(0)
    }

    override fun onDestroyView() {
        binding = null
        rows = emptyList()
        super.onDestroyView()
    }

    private companion object {
        const val STATE_SELECTED = "selected"
    }
}

private val EINK_ICON_FILTER: ColorMatrixColorFilter = run {
    val grey = ColorMatrix().apply { setSaturation(0f) }
    val contrast = 2.2f
    val scale = contrast
    val translate = (-0.5f * scale + 0.5f) * 255f
    val boost = ColorMatrix(
        floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f,
        ),
    )
    grey.postConcat(boost)
    ColorMatrixColorFilter(grey)
}
