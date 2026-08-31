package com.abeant.gretel.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView
import kotlin.math.roundToInt

/**
 * E-ink: no smooth scroll, no overscroll.
 * Overflow of a sliver is not a second page: clamp to 0.
 * One-to-two pages: on lift, jump to top or end.
 * Two pages or more: jump by viewport height.
 */
class SnapScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ScrollView(context, attrs, defStyle) {

    init {
        isSmoothScrollingEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
    }

    override fun setSmoothScrollingEnabled(smoothScrollingEnabled: Boolean) {
        super.setSmoothScrollingEnabled(false)
    }

    override fun isSmoothScrollingEnabled(): Boolean = false

    private fun overflowPx(): Int {
        val child = getChildAt(0) ?: return 0
        return (child.height - height).coerceAtLeast(0)
    }

    private fun slopPx(): Int =
        (SLOP_DP * resources.displayMetrics.density).roundToInt()

    override fun fling(velocityY: Int) {
        // E-ink: never fling. Snap on lift instead.
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (overflowPx() <= slopPx()) return false
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (overflowPx() <= slopPx()) {
            if (scrollY != 0) scrollTo(0, 0)
            return false
        }
        val handled = super.onTouchEvent(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_UP -> {
                performClick()
                snap()
            }
            MotionEvent.ACTION_CANCEL -> snap()
        }
        return handled
    }

    override fun performClick(): Boolean = super.performClick()

    override fun scrollTo(x: Int, y: Int) {
        val max = overflowPx()
        val destY = if (max <= slopPx()) 0 else y.coerceIn(0, max)
        super.scrollTo(x, destY)
    }

    private fun snap() {
        val page = height
        if (page <= 0) return
        val max = overflowPx()
        if (max <= slopPx()) {
            scrollTo(0, 0)
            return
        }
        val dest = if (max < page) {
            // Content between 1 and 2 pages: top or end, never a fake page.
            if (scrollY * 2 >= max) max else 0
        } else {
            val pageIndex = (scrollY + page / 2) / page
            (pageIndex * page).coerceIn(0, max)
        }
        if (dest != scrollY) scrollTo(0, dest)
    }

    companion object {
        private const val SLOP_DP = 8f
    }
}
