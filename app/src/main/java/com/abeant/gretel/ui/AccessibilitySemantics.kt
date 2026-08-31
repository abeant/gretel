package com.abeant.gretel.ui

import android.view.View
import android.widget.Button
import android.widget.CheckBox
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

/** Adds standard control roles to the full-width custom rows used on e-ink. */
object AccessibilitySemantics {
    fun asButton(view: View) {
        ViewCompat.setAccessibilityDelegate(view, RoleDelegate(Button::class.java.name))
    }

    fun asToggle(view: View, isChecked: () -> Boolean) {
        ViewCompat.setAccessibilityDelegate(
            view,
            RoleDelegate(CheckBox::class.java.name, isChecked),
        )
    }

    private class RoleDelegate(
        private val className: String,
        private val isChecked: (() -> Boolean)? = null,
    ) : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(
            host: View,
            info: AccessibilityNodeInfoCompat,
        ) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.className = className
            isChecked?.let {
                info.isCheckable = true
                info.isChecked = it()
            }
        }
    }
}
