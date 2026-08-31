package com.abeant.gretel.ui

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abeant.gretel.HatchHost
import com.abeant.gretel.R
import com.abeant.gretel.Screen
import com.abeant.gretel.catalog.LaunchableApp
import com.abeant.gretel.databinding.FragmentPickDoorBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PickDoorFragment : Fragment() {
    private var binding: FragmentPickDoorBinding? = null
    private var selectedPackage: String? = null
    private var allApps: List<LaunchableApp> = emptyList()
    private lateinit var adapter: AppAdapter

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
        selectedPackage = snapshot.assignedPackage

        adapter = AppAdapter(emptyList(), selectedPackage) { app ->
            selectedPackage = app.packageName
            adapter.selectPackage(selectedPackage)
            view.confirmButton.isEnabled = true
        }
        view.appList.layoutManager = LinearLayoutManager(requireContext())
        view.appList.adapter = adapter
        applyFilter("")

        view.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                applyFilter(s?.toString().orEmpty())
            }
        })

        view.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        view.confirmButton.isEnabled = selectedPackage != null
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
            allApps = loaded
            if (selectedPackage == null) {
                selectedPackage = host.catalog.preferredDefault(loaded)?.packageName
            }
            adapter.selectPackage(selectedPackage)
            applyFilter(view.search.text?.toString().orEmpty())
            view.confirmButton.isEnabled = selectedPackage != null
        }
        return view.root
    }

    private fun applyFilter(query: String) {
        val needle = query.trim()
        val filtered = if (needle.isEmpty()) {
            allApps
        } else {
            allApps.filter {
                it.label.contains(needle, ignoreCase = true) ||
                    it.packageName.contains(needle, ignoreCase = true)
            }
        }
        adapter.submit(filtered)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
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

private class AppAdapter(

    private var items: List<LaunchableApp>,
    private var selectedPackage: String?,
    private val onClick: (LaunchableApp) -> Unit,
) : RecyclerView.Adapter<AppAdapter.Holder>() {

    fun submit(next: List<LaunchableApp>) {
        val previous = items
        val selected = selectedPackage
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = previous.size
            override fun getNewListSize(): Int = next.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                previous[oldItemPosition].packageName == next[newItemPosition].packageName

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = previous[oldItemPosition]
                val new = next[newItemPosition]
                return old.packageName == new.packageName &&
                    old.label == new.label &&
                    (old.packageName == selected) == (new.packageName == selected)
            }
        })
        items = next
        diff.dispatchUpdatesTo(this)
    }

    fun selectPackage(packageName: String?) {
        if (selectedPackage == packageName) return
        val oldPosition = items.indexOfFirst { it.packageName == selectedPackage }
        selectedPackage = packageName
        val newPosition = items.indexOfFirst { it.packageName == selectedPackage }
        if (oldPosition >= 0) notifyItemChanged(oldPosition)
        if (newPosition >= 0 && newPosition != oldPosition) notifyItemChanged(newPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        AccessibilitySemantics.asButton(view)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val app = items[position]
        holder.label.text = app.label
        holder.packageName.text = app.packageName
        val selected = app.packageName == selectedPackage
        holder.itemView.isSelected = selected
        holder.itemView.contentDescription = if (selected) {
            holder.itemView.context.getString(R.string.app_item_selected, app.label)
        } else {
            app.label
        }
        holder.badge.visibility = if (selected) View.VISIBLE else View.GONE
        holder.badge.setText(R.string.selected_mark)
        if (app.icon != null) {
            holder.icon.setImageDrawable(app.icon)
            holder.icon.colorFilter = EINK_ICON_FILTER
            holder.icon.visibility = View.VISIBLE
        } else {
            holder.icon.setImageDrawable(null)
            holder.icon.colorFilter = null
            holder.icon.visibility = View.INVISIBLE
        }
        holder.itemView.setOnClickListener { onClick(app) }
    }

    override fun getItemCount(): Int = items.size

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val label: TextView = view.findViewById(R.id.appLabel)
        val packageName: TextView = view.findViewById(R.id.appPackage)
        val badge: TextView = view.findViewById(R.id.suggestedBadge)
    }
}
