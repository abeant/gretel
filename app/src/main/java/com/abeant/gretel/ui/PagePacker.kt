package com.abeant.gretel.ui

import kotlin.math.max
import kotlin.math.min

/**
 * Pure page packing for [PagedColumn], kept free of Android types so it can be
 * unit tested.
 *
 * Rows are packed whole, in order. A row marked keepWithNext travels with the
 * row after it whenever the pair fits on one page. A row taller than a page is
 * sliced across pages; [Row.snap] lets text rows move a cut to a line boundary.
 */
object PagePacker {
    class Row(
        val height: Int,
        val topMargin: Int = 0,
        val bottomMargin: Int = 0,
        val keepWithNext: Boolean = false,
        /** Given a proposed cut inside the row, returns the cut to use. Must stay in (from, to]. */
        val snap: (from: Int, to: Int) -> Int = { _, to -> to },
    ) {
        val total: Int get() = height + topMargin + bottomMargin
    }

    /** One row, or a vertical slice of it, placed on a page. [top]..[bottom] are row coordinates. */
    data class Slice(val row: Int, val top: Int, val bottom: Int)

    fun pack(rows: List<Row>, pageHeight: Int): List<List<Slice>> {
        if (rows.isEmpty()) return emptyList()
        if (pageHeight <= 0) {
            // No room to show anything, whether this is a bootstrap pass or a
            // window squeezed to nothing. Return one finite page rather than
            // thousands of one-pixel slices; the container clips it regardless.
            return listOf(rows.indices.map { Slice(it, 0, rows[it].height) })
        }

        val pages = mutableListOf<List<Slice>>()
        var current = mutableListOf<Slice>()
        var used = 0

        fun flush() {
            if (current.isNotEmpty()) {
                pages += current
                current = mutableListOf()
                used = 0
            }
        }

        var index = 0
        while (index < rows.size) {
            val row = rows[index]

            if (row.total > pageHeight) {
                flush()
                var sliceTop = 0
                var first = true
                while (sliceTop < row.height) {
                    val budget = pageHeight - (if (first) row.topMargin else 0)
                    var sliceBottom = min(row.height, sliceTop + max(1, budget))
                    if (sliceBottom < row.height) {
                        val snapped = row.snap(sliceTop, sliceBottom)
                        if (snapped > sliceTop && snapped <= sliceBottom) sliceBottom = snapped
                    }
                    current += Slice(index, sliceTop, sliceBottom)
                    val last = sliceBottom >= row.height
                    used = (sliceBottom - sliceTop) +
                        (if (first) row.topMargin else 0) +
                        (if (last) row.bottomMargin else 0)
                    if (!last) flush()
                    sliceTop = sliceBottom
                    first = false
                }
                index++
                continue
            }

            val next = rows.getOrNull(index + 1)
            val pair = row.keepWithNext && next != null && row.total + next.total <= pageHeight
            val needed = if (pair) row.total + next!!.total else row.total
            if (current.isNotEmpty() && used + needed > pageHeight) flush()
            current += Slice(index, 0, row.height)
            used += row.total
            index++
        }
        flush()
        return pages
    }
}
