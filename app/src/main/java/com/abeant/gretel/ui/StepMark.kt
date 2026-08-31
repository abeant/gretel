package com.abeant.gretel.ui

import android.view.View
import android.view.ViewGroup
import com.abeant.gretel.R

object StepMark {
    fun bind(row: ViewGroup?, current: Int) {
        if (row == null) return
        for (i in 0 until row.childCount) {
            row.getChildAt(i).apply {
                isSelected = i < current
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
        }
        row.contentDescription = row.context.getString(
            R.string.step_progress,
            current,
            row.childCount,
        )
        row.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }
}
