package com.abeant.gretel.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.abeant.gretel.HatchHost
import com.abeant.gretel.Screen
import com.abeant.gretel.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {
    private var binding: FragmentWelcomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = FragmentWelcomeBinding.inflate(inflater, container, false)
        binding = view
        StepMark.bind(view.stepRow.root as ViewGroup, 1)
        view.continueButton.setOnClickListener {
            (requireActivity() as HatchHost).show(Screen.PickDoor)
        }
        return view.root
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
