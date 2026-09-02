package com.abeant.gretel.ui

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import com.abeant.gretel.R
import kotlin.math.abs
import kotlin.math.max

/**
 * E-ink pagination. Children are rows; the container measures them, packs whole
 * rows onto pages, and shows one page at a time. Nothing scrolls, nothing
 * animates. A footer with Previous, "1 / 3", and Next appears only when the
 * rows overflow one page. A row taller than a page is shown in slices, cut at
 * text line boundaries when the row is a TextView.
 *
 * Rows hide themselves with GONE. The pager uses INVISIBLE for rows that are on
 * another page, so rows must not use INVISIBLE for their own purposes.
 */
class PagedColumn @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ViewGroup(context, attrs, defStyle) {

    class LayoutParams : MarginLayoutParams {
        var keepWithNext: Boolean = false

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            context.withStyledAttributes(attrs, R.styleable.PagedColumn_Layout) {
                keepWithNext = getBoolean(R.styleable.PagedColumn_Layout_layout_keepWithNext, false)
            }
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: ViewGroup.LayoutParams) : super(source)
    }

    private class Placement(val view: View, val sliceTop: Int, val sliceBottom: Int)

    private val footer: View
    private val prevButton: Button
    private val nextButton: Button
    private val pageLabel: TextView

    private var pages: List<List<Placement>> = emptyList()
    private var currentPage: List<Placement> = emptyList()
    private var footerShown = false
    private val swipeSlop = ViewConfiguration.get(context).scaledTouchSlop * 3
    private var downX = 0f
    private var downY = 0f
    private var swiping = false

    var page: Int = 0
        private set

    val pageCount: Int
        get() = pages.size

    init {
        footer = LayoutInflater.from(context).inflate(R.layout.include_pager, this, false)
        addView(footer)
        prevButton = footer.findViewById(R.id.pagerPrev)
        nextButton = footer.findViewById(R.id.pagerNext)
        pageLabel = footer.findViewById(R.id.pagerLabel)
        prevButton.setOnClickListener { previous() }
        nextButton.setOnClickListener { next() }
    }

    // Rows

    private fun rows(): List<View> =
        (0 until childCount).map(::getChildAt).filter { it !== footer }

    fun removeRows() {
        rows().forEach(::removeView)
        page = 0
    }

    fun goTo(index: Int) {
        val target = index.coerceIn(0, max(0, pageCount - 1))
        if (target == page) return
        page = target
        requestLayout()
    }

    fun next(): Boolean {
        if (page >= pageCount - 1) return false
        goTo(page + 1)
        return true
    }

    fun previous(): Boolean {
        if (page <= 0) return false
        goTo(page - 1)
        return true
    }

    /** Page keys, dpad, and e-reader page buttons. Only active when there is more than one page. */
    fun handleKey(keyCode: Int): Boolean {
        if (pageCount <= 1) return false
        return when (keyCode) {
            KeyEvent.KEYCODE_PAGE_DOWN,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            -> {
                next()
                true
            }
            KeyEvent.KEYCODE_PAGE_UP,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_VOLUME_UP,
            -> {
                previous()
                true
            }
            else -> false
        }
    }

    // Measure and layout

    // Pagination happens in the measure pass by design; the few small lists it
    // allocates are far cheaper than a second layout pass on an e-ink panel.
    @Suppress("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val innerWidth = max(0, width - paddingLeft - paddingRight)
        val unbounded = heightMode == MeasureSpec.UNSPECIFIED
        val innerHeight = if (unbounded) Int.MAX_VALUE / 2 else max(0, heightSize - paddingTop - paddingBottom)

        val visibleRows = rows().filter { it.visibility != GONE }
        var total = 0
        val packRows = visibleRows.map { row ->
            val lp = row.layoutParams as LayoutParams
            val childWidthSpec = getChildMeasureSpec(
                MeasureSpec.makeMeasureSpec(innerWidth, MeasureSpec.EXACTLY),
                lp.leftMargin + lp.rightMargin,
                lp.width,
            )
            val childHeightSpec = getChildMeasureSpec(
                MeasureSpec.makeMeasureSpec(innerHeight, MeasureSpec.UNSPECIFIED),
                lp.topMargin + lp.bottomMargin,
                lp.height,
            )
            row.measure(childWidthSpec, childHeightSpec)
            total += row.measuredHeight + lp.topMargin + lp.bottomMargin
            PagePacker.Row(
                height = row.measuredHeight,
                topMargin = lp.topMargin,
                bottomMargin = lp.bottomMargin,
                keepWithNext = lp.keepWithNext,
                snap = { from, to -> snapToLine(row, from, to) },
            )
        }

        val footerLp = footer.layoutParams as MarginLayoutParams
        footer.measure(
            getChildMeasureSpec(
                MeasureSpec.makeMeasureSpec(innerWidth, MeasureSpec.EXACTLY),
                footerLp.leftMargin + footerLp.rightMargin,
                footerLp.width,
            ),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
        )
        val footerHeight = footer.measuredHeight + footerLp.topMargin + footerLp.bottomMargin

        footerShown = !unbounded && total > innerHeight
        val pageHeight = if (footerShown) max(0, innerHeight - footerHeight) else innerHeight
        pages = PagePacker.pack(packRows, pageHeight).map { slices ->
            slices.map { Placement(visibleRows[it.row], it.top, it.bottom) }
        }
        page = page.coerceIn(0, max(0, pages.size - 1))
        currentPage = pages.getOrNull(page).orEmpty()

        for (row in visibleRows) {
            val wanted = if (currentPage.any { it.view === row }) VISIBLE else INVISIBLE
            if (row.visibility != wanted) row.visibility = wanted
        }
        footer.visibility = if (footerShown) VISIBLE else INVISIBLE
        updateFooter()

        val measuredHeight = if (unbounded || heightMode == MeasureSpec.AT_MOST && total + paddingTop + paddingBottom < heightSize) {
            total + paddingTop + paddingBottom
        } else {
            heightSize
        }
        setMeasuredDimension(width, measuredHeight)
    }

    /** Moves a cut inside a text row up to the top of the line it would split. */
    private fun snapToLine(row: View, from: Int, to: Int): Int {
        if (row !is TextView) return to
        val layout = row.layout ?: return to
        val offset = row.totalPaddingTop
        val textBottom = to - offset
        if (textBottom <= 0 || textBottom >= layout.height) return to
        val line = layout.getLineForVertical(textBottom)
        val lineTop = layout.getLineTop(line) + offset
        return if (lineTop > from) lineTop else to
    }

    @Suppress("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val row = getChildAt(i)
            if (row === footer) continue
            if (currentPage.none { it.view === row }) {
                row.layout(0, 0, 0, 0)
                row.clipBounds = null
            }
        }
        var top = paddingTop
        for (placement in currentPage) {
            val row = placement.view
            val lp = row.layoutParams as LayoutParams
            val first = placement.sliceTop == 0
            val last = placement.sliceBottom >= row.measuredHeight
            val left = paddingLeft + lp.leftMargin
            val rowTop = top + (if (first) lp.topMargin else 0) - placement.sliceTop
            row.layout(left, rowTop, left + row.measuredWidth, rowTop + row.measuredHeight)
            row.clipBounds = if (first && last) {
                null
            } else {
                Rect(0, placement.sliceTop, row.measuredWidth, placement.sliceBottom)
            }
            top = rowTop + placement.sliceBottom + (if (last) lp.bottomMargin else 0)
        }
        if (footerShown) {
            val lp = footer.layoutParams as MarginLayoutParams
            val bottom = b - t - paddingBottom - lp.bottomMargin
            val left = paddingLeft + lp.leftMargin
            footer.layout(left, bottom - footer.measuredHeight, left + footer.measuredWidth, bottom)
        } else {
            footer.layout(0, 0, 0, 0)
        }
    }

    private fun updateFooter() {
        val label = context.getString(R.string.page_indicator, page + 1, max(1, pageCount))
        // The label is a polite live region, so setting it is what announces a
        // page turn. Only assign on a real change, or every measure would talk.
        if (pageLabel.text.toString() != label) {
            pageLabel.text = label
            pageLabel.contentDescription = pageDescription()
        }
        // On a 1-bit screen a disabled outline is indistinguishable from an
        // enabled one, so an unavailable direction is hidden rather than dimmed.
        // INVISIBLE, not GONE, so the indicator stays centred.
        prevButton.isEnabled = page > 0
        nextButton.isEnabled = page < pageCount - 1
        prevButton.visibility = if (prevButton.isEnabled) VISIBLE else INVISIBLE
        nextButton.visibility = if (nextButton.isEnabled) VISIBLE else INVISIBLE
    }

    private fun pageDescription(): String =
        context.getString(R.string.page_progress, page + 1, max(1, pageCount))

    override fun shouldDelayChildPressedState(): Boolean = false

    // Swipe: horizontal or vertical, no animation, only when there is something to page to.

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                swiping = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!swiping && pageCount > 1 && exceededSlop(ev)) {
                    swiping = true
                    return true
                }
            }
        }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                swiping = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!swiping && pageCount > 1 && exceededSlop(ev)) swiping = true
            }
            MotionEvent.ACTION_UP -> {
                if (swiping) {
                    val dx = ev.x - downX
                    val dy = ev.y - downY
                    val forward = if (abs(dx) > abs(dy)) dx < 0 else dy < 0
                    if (forward) next() else previous()
                } else {
                    performClick()
                }
                swiping = false
            }
            MotionEvent.ACTION_CANCEL -> swiping = false
        }
        return true
    }

    override fun performClick(): Boolean = super.performClick()

    private fun exceededSlop(ev: MotionEvent): Boolean =
        abs(ev.x - downX) > swipeSlop || abs(ev.y - downY) > swipeSlop

    // State

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable(STATE_SUPER, super.onSaveInstanceState())
            putInt(STATE_PAGE, page)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            @Suppress("DEPRECATION")
            super.onRestoreInstanceState(state.getParcelable(STATE_SUPER))
            page = state.getInt(STATE_PAGE, 0)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    // LayoutParams plumbing

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams =
        LayoutParams(context, attrs)

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams =
        LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams =
        LayoutParams(p)

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean = p is LayoutParams

    companion object {
        private const val STATE_SUPER = "super"
        private const val STATE_PAGE = "page"

        /** The first pager in [root]'s tree, for key dispatch from the activity. */
        fun find(root: View): PagedColumn? {
            if (root is PagedColumn) return root
            if (root is ViewGroup) {
                for (i in 0 until root.childCount) {
                    find(root.getChildAt(i))?.let { return it }
                }
            }
            return null
        }
    }
}
