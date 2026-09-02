package com.abeant.gretel.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.abeant.gretel.HatchHost
import com.abeant.gretel.R
import com.abeant.gretel.databinding.FragmentChoiceBinding

/** One setting, a few options, one page. Used for double-Home timing and theme. */
class ChoiceFragment : Fragment() {
    private var binding: FragmentChoiceBinding? = null

    private data class Option(val id: String, val title: Int, val description: Int)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = FragmentChoiceBinding.inflate(inflater, container, false)
        binding = view
        val host = requireActivity() as HatchHost
        val kind = requireArguments().getString(ARG_KIND) ?: KIND_TIMING
        val snapshot = host.store.snapshot()

        val options: List<Option>
        val current: String
        when (kind) {
            KIND_THEME -> {
                view.title.setText(R.string.theme_title)
                view.body.setText(R.string.theme_body)
                options = listOf(
                    Option(ThemeMode.AUTO, R.string.theme_auto, R.string.theme_auto_desc),
                    Option(ThemeMode.WHITE, R.string.theme_white, R.string.theme_white_desc),
                    Option(ThemeMode.BLACK, R.string.theme_black, R.string.theme_black_desc),
                )
                current = ThemeMode.normalize(snapshot.themeMode)
            }
            else -> {
                view.title.setText(R.string.timing_title)
                view.body.setText(R.string.timing_body)
                options = listOf(
                    Option("500", R.string.timing_quick, R.string.timing_quick_desc),
                    Option("800", R.string.timing_normal, R.string.timing_normal_desc),
                    Option("1200", R.string.timing_relaxed, R.string.timing_relaxed_desc),
                )
                current = snapshot.hatchWindowMs.toString()
            }
        }

        view.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        for (option in options) {
            val row = inflater.inflate(R.layout.item_choice, view.choicePager, false)
            AccessibilitySemantics.asButton(row)
            val title = getString(option.title)
            row.findViewById<TextView>(R.id.choiceTitle).text = title
            row.findViewById<TextView>(R.id.choiceDescription).setText(option.description)
            val selected = option.id == current
            row.isSelected = selected
            row.findViewById<View>(R.id.selectedBadge).visibility = if (selected) View.VISIBLE else View.GONE
            row.contentDescription = if (selected) getString(R.string.app_item_selected, title) else title
            row.setOnClickListener { choose(kind, option.id) }
            view.choicePager.addView(row)
        }
        return view.root
    }

    private fun choose(kind: String, id: String) {
        val host = requireActivity() as HatchHost
        when (kind) {
            KIND_THEME -> {
                val changed = ThemeMode.normalize(host.store.snapshot().themeMode) != id
                host.store.setThemeMode(id)
                parentFragmentManager.popBackStackImmediate()
                if (changed) requireActivity().recreate()
            }
            else -> {
                host.store.setHatchWindowMs(id.toLong())
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_KIND = "kind"
        private const val KIND_TIMING = "timing"
        private const val KIND_THEME = "theme"

        fun timing(): ChoiceFragment = ChoiceFragment().apply {
            arguments = bundleOf(ARG_KIND to KIND_TIMING)
        }

        fun theme(): ChoiceFragment = ChoiceFragment().apply {
            arguments = bundleOf(ARG_KIND to KIND_THEME)
        }
    }
}
