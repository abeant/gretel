package com.abeant.gretel.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.abeant.gretel.HatchHost
import com.abeant.gretel.R
import com.abeant.gretel.databinding.FragmentSetHomeBinding

class SetHomeFragment : Fragment() {
    private var binding: FragmentSetHomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = FragmentSetHomeBinding.inflate(inflater, container, false)
        binding = view
        val host = requireActivity() as HatchHost
        StepMark.bind(view.stepRow.root as ViewGroup, 4)

        view.setHomeButton.setOnClickListener {
            host.homeRole.requestHomeRole(requireActivity())
        }
        view.homeSettingsButton.setOnClickListener {
            host.homeRole.openSystemHomeSettings(requireActivity())
        }
        view.skipButton.setOnClickListener {
            host.store.setOnboardingDone(true)
            host.launchAssignedAndLeave()
        }
        view.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        return view.root
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        val view = binding ?: return
        val host = requireActivity() as HatchHost
        val isHome = host.homeRole.isDefaultHome()
        if (isHome) {
            view.title.setText(R.string.youre_all_set)
            view.body.setText(R.string.home_already_body)
            view.setHomeButton.visibility = View.GONE
            view.homeSettingsButton.visibility = View.VISIBLE
            view.skipButton.setText(R.string.done_label)
            view.skipButton.layoutParams = view.skipButton.layoutParams.apply {
                width = LinearLayout.LayoutParams.MATCH_PARENT
            }
            view.skipButton.setBackgroundResource(R.drawable.btn_filled)
            view.skipButton.setTextColor(requireContext().getColorStateList(R.color.btn_filled_text))
        } else {
            view.title.setText(R.string.set_home_title)
            view.body.setText(R.string.set_home_body)
            view.setHomeButton.visibility = View.VISIBLE
            view.homeSettingsButton.visibility = View.GONE
            view.skipButton.setText(R.string.skip_for_now)
            view.skipButton.layoutParams = view.skipButton.layoutParams.apply {
                width = LinearLayout.LayoutParams.MATCH_PARENT
            }
            view.skipButton.setBackgroundResource(R.drawable.btn_outline)
            view.skipButton.setTextColor(requireContext().getColorStateList(R.color.btn_text))
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
