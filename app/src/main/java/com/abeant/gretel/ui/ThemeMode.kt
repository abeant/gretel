package com.abeant.gretel.ui

import android.content.Context
import android.content.res.Configuration
import com.abeant.gretel.R

object ThemeMode {
    const val AUTO = "auto"
    const val WHITE = "white"
    const val BLACK = "black"

    fun normalize(raw: String?): String = when (raw) {
        WHITE, BLACK, AUTO -> raw
        else -> AUTO
    }

    fun isNight(context: Context): Boolean {
        val night = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return night == Configuration.UI_MODE_NIGHT_YES
    }

    fun isBlack(mode: String, night: Boolean): Boolean = when (mode) {
        BLACK -> true
        WHITE -> false
        else -> night
    }

    fun next(mode: String): String = when (normalize(mode)) {
        AUTO -> WHITE
        WHITE -> BLACK
        else -> AUTO
    }

    fun labelRes(mode: String): Int = when (normalize(mode)) {
        WHITE -> R.string.theme_white
        BLACK -> R.string.theme_black
        else -> R.string.theme_auto
    }
}
