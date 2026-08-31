package com.abeant.gretel.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.abeant.gretel.HatchHost
import com.abeant.gretel.R
import com.abeant.gretel.Screen
import com.abeant.gretel.databinding.FragmentHatchLessonBinding

class HatchLessonFragment : Fragment() {
    private var binding: FragmentHatchLessonBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = FragmentHatchLessonBinding.inflate(inflater, container, false)
        binding = view
        val host = requireActivity() as HatchHost
        val snapshot = host.store.snapshot()
        view.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        if (snapshot.onboardingDone) {
            view.stepRow.root.visibility = View.GONE
            view.finishButton.setText(R.string.done_label)
            view.finishButton.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        } else {
            StepMark.bind(view.stepRow.root as ViewGroup, 3)
            view.finishButton.setText(R.string.continue_label)
            view.finishButton.setOnClickListener {
                host.show(Screen.SetHome)
            }
        }
        return view.root
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
