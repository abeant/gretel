package com.abeant.gretel.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.abeant.gretel.R

/**
 * One settings row: label, optional value, and either a chevron or a checkbox.
 * A full-width 48 dp target with a focus outline and a proper accessibility role.
 */
class SettingRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attrs, defStyle) {

    private val labelView: TextView
    private val valueView: TextView
    private val chevronView: TextView
    private val checkBox: CheckBox
    private val toggle: Boolean
    private var checkedListener: ((Boolean) -> Unit)? = null

    var label: CharSequence
        get() = labelView.text
        set(text) {
            labelView.text = text
            describe()
        }

    var value: CharSequence?
        get() = valueView.text.takeIf { valueView.isVisible }
        set(text) {
            valueView.text = text
            valueView.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
            describe()
        }

    init {
        orientation = HORIZONTAL
        gravity = android.view.Gravity.CENTER_VERTICAL
        minimumHeight = resources.getDimensionPixelSize(R.dimen.settings_row)
        setBackgroundResource(R.drawable.row_background)
        isClickable = true
        isFocusable = true
        LayoutInflater.from(context).inflate(R.layout.view_setting_row, this, true)
        labelView = findViewById(R.id.rowLabel)
        valueView = findViewById(R.id.rowValue)
        chevronView = findViewById(R.id.rowChevron)
        checkBox = findViewById(R.id.rowCheck)

        val a = context.obtainStyledAttributes(attrs, R.styleable.SettingRow)
        toggle = a.getBoolean(R.styleable.SettingRow_rowToggle, false)
        val chevron = a.getBoolean(R.styleable.SettingRow_rowChevron, !toggle)
        labelView.text = a.getText(R.styleable.SettingRow_rowLabel)
        value = a.getText(R.styleable.SettingRow_rowValue)
        a.recycle()

        chevronView.visibility = if (chevron) View.VISIBLE else View.GONE
        checkBox.visibility = if (toggle) View.VISIBLE else View.GONE
        if (toggle) {
            AccessibilitySemantics.asToggle(this) { checkBox.isChecked }
            super.setOnClickListener { setChecked(!checkBox.isChecked, notify = true) }
        } else {
            AccessibilitySemantics.asButton(this)
        }
        describe()
    }

    fun setValue(resId: Int) {
        value = context.getString(resId)
    }

    var isChecked: Boolean
        get() = checkBox.isChecked
        set(checked) = setChecked(checked, notify = false)

    fun setOnCheckedChangeListener(listener: ((Boolean) -> Unit)?) {
        checkedListener = listener
    }

    private fun setChecked(checked: Boolean, notify: Boolean) {
        if (checkBox.isChecked != checked) {
            checkBox.isChecked = checked
        }
        if (notify) checkedListener?.invoke(checked)
    }

    private fun describe() {
        val value = value
        contentDescription = if (!toggle && !value.isNullOrEmpty()) {
            context.getString(R.string.setting_with_value, labelView.text, value)
        } else {
            labelView.text
        }
    }
}
