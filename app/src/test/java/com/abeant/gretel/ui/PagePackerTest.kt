package com.abeant.gretel.ui

import com.abeant.gretel.ui.PagePacker.Row
import com.abeant.gretel.ui.PagePacker.Slice
import org.junit.Assert.assertEquals
import org.junit.Test

class PagePackerTest {

    private fun rowsOn(pages: List<List<Slice>>) = pages.map { page -> page.map { it.row } }

    @Test
    fun emptyIsEmpty() {
        assertEquals(emptyList<List<Slice>>(), PagePacker.pack(emptyList(), 100))
    }

    @Test
    fun everythingFitsOnOnePage() {
        val rows = List(3) { Row(height = 30) }
        assertEquals(listOf(listOf(0, 1, 2)), rowsOn(PagePacker.pack(rows, 100)))
    }

    @Test
    fun wholeRowsNeverSplitAcrossPages() {
        val rows = List(5) { Row(height = 30) }
        assertEquals(listOf(listOf(0, 1, 2), listOf(3, 4)), rowsOn(PagePacker.pack(rows, 100)))
    }

    @Test
    fun marginsCountTowardsThePage() {
        val rows = List(3) { Row(height = 30, topMargin = 5, bottomMargin = 5) }
        assertEquals(listOf(listOf(0, 1), listOf(2)), rowsOn(PagePacker.pack(rows, 100)))
    }

    @Test
    fun headerMovesWithItsFirstRow() {
        val rows = listOf(
            Row(height = 40),
            Row(height = 40),
            Row(height = 20, keepWithNext = true),
            Row(height = 40),
        )
        assertEquals(listOf(listOf(0, 1), listOf(2, 3)), rowsOn(PagePacker.pack(rows, 100)))
    }

    @Test
    fun headerPairTallerThanAPageIsNotStrandedAlone() {
        val rows = listOf(
            Row(height = 60, keepWithNext = true),
            Row(height = 60),
            Row(height = 20),
        )
        assertEquals(listOf(listOf(0), listOf(1, 2)), rowsOn(PagePacker.pack(rows, 100)))
    }

    @Test
    fun oversizedRowIsSlicedAndTailSharesItsPage() {
        val rows = listOf(
            Row(height = 20),
            Row(height = 250),
            Row(height = 30),
        )
        val pages = PagePacker.pack(rows, 100)
        assertEquals(
            listOf(
                listOf(Slice(0, 0, 20)),
                listOf(Slice(1, 0, 100)),
                listOf(Slice(1, 100, 200)),
                listOf(Slice(1, 200, 250), Slice(2, 0, 30)),
            ),
            pages,
        )
    }

    @Test
    fun sliceCutsSnapToLineBoundaries() {
        val lineTops = listOf(0, 24, 48, 72, 96, 120, 144)
        val snap: (Int, Int) -> Int = { from, to ->
            lineTops.filter { it in (from + 1)..to }.maxOrNull() ?: to
        }
        val rows = listOf(Row(height = 168, snap = snap))
        assertEquals(
            listOf(listOf(Slice(0, 0, 96)), listOf(Slice(0, 96, 168))),
            PagePacker.pack(rows, 100),
        )
    }

    @Test
    fun firstSliceHonoursTopMargin() {
        val rows = listOf(Row(height = 150, topMargin = 10))
        assertEquals(
            listOf(listOf(Slice(0, 0, 90)), listOf(Slice(0, 90, 150))),
            PagePacker.pack(rows, 100),
        )
    }

    @Test
    fun zeroPageHeightStillTerminates() {
        val rows = listOf(Row(height = 50), Row(height = 300))
        assertEquals(listOf(listOf(0, 1)), rowsOn(PagePacker.pack(rows, 0)))
    }
}
