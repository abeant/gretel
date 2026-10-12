package com.abeant.gretel.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.abeant.gretel.R
import com.abeant.gretel.databinding.FragmentLicenceBinding

/**
 * Apache 2.0 section 4(a) asks that recipients get a copy of the licence.
 * E-readers often have no browser, so the full text ships in assets. Each
 * paragraph is its own row so the pager can break between them.
 */
class LicenceFragment : Fragment() {
    private var binding: FragmentLicenceBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = FragmentLicenceBinding.inflate(inflater, container, false)
        binding = view
        view.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        val text = readBundledLicence() ?: getString(R.string.about_license_fallback)
        for (paragraph in paragraphs(text)) {
            val row = TextView(requireContext(), null, 0, R.style.Text_Gretel_Body)
            row.text = paragraph
            row.layoutParams = PagedColumn.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = resources.getDimensionPixelSize(R.dimen.paragraph_gap) }
            view.licencePager.addView(row)
        }
        return view.root
    }

    private fun readBundledLicence(): String? = try {
        requireContext().assets.open(LICENSE_ASSET).bufferedReader().use { it.readText() }
    } catch (_: java.io.IOException) {
        null
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val LICENSE_ASSET = "LICENSE.txt"

        /** Blank-line separated paragraphs, with hard wraps inside a paragraph joined. */
        fun paragraphs(text: String): List<String> =
            text.split(Regex("\\n[ \\t]*\\n"))
                .map { it.trim().replace(Regex("\\s*\\n\\s*"), " ") }
                .filter { it.isNotEmpty() }
    }
}
