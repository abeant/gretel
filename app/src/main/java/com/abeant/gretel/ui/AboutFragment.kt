package com.abeant.gretel.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.abeant.gretel.BuildConfig
import com.abeant.gretel.HatchHost
import com.abeant.gretel.R
import com.abeant.gretel.Screen
import com.abeant.gretel.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {
    private var binding: FragmentAboutBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = FragmentAboutBinding.inflate(inflater, container, false)
        binding = view
        val host = requireActivity() as HatchHost
        view.version.text = getString(
            R.string.about_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE,
        )
        view.sourceRow.setOnClickListener {
            val uri = getString(R.string.source_repo_url).toUri()
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (_: Exception) {
                // No browser on some e-readers. The licence text itself is bundled.
            }
        }
        view.licenceRow.setOnClickListener { host.show(Screen.Licence) }
        view.replayIntroRow.setOnClickListener {
            host.store.setOnboardingDone(false)
            host.show(Screen.Welcome)
        }
        view.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        return view.root
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
