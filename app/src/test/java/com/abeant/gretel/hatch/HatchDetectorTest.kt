package com.abeant.gretel.hatch

import org.junit.Assert.assertEquals
import org.junit.Test

class HatchDetectorTest {

    @Test
    fun singleHomeLaunchesAssigned() {
        var now = 1_000L
        val detector = HatchDetector { now }
        assertEquals(HatchDetector.Decision.LAUNCH_ASSIGNED, detector.onHomeDelivery(800))
    }

    @Test
    fun doubleHomeWithinWindowOpensHatch() {
        var now = 1_000L
        val detector = HatchDetector { now }
        assertEquals(HatchDetector.Decision.LAUNCH_ASSIGNED, detector.onHomeDelivery(800))
        now = 1_800L
        assertEquals(HatchDetector.Decision.OPEN_HATCH, detector.onHomeDelivery(800))
    }

    @Test
    fun doubleHomeAtExactWindowOpensHatch() {
        var now = 1_000L
        val detector = HatchDetector { now }
        detector.onHomeDelivery(800)
        now = 1_800L
        assertEquals(HatchDetector.Decision.OPEN_HATCH, detector.onHomeDelivery(800))
    }

    @Test
    fun doubleHomeOutsideWindowLaunchesAssigned() {
        var now = 1_000L
        val detector = HatchDetector { now }
        assertEquals(HatchDetector.Decision.LAUNCH_ASSIGNED, detector.onHomeDelivery(800))
        now = 1_801L
        assertEquals(HatchDetector.Decision.LAUNCH_ASSIGNED, detector.onHomeDelivery(800))
    }

    @Test
    fun afterHatchNextPressIsSingle() {
        var now = 1_000L
        val detector = HatchDetector { now }
        detector.onHomeDelivery(800)
        now = 1_400L
        assertEquals(HatchDetector.Decision.OPEN_HATCH, detector.onHomeDelivery(800))
        now = 1_500L
        assertEquals(HatchDetector.Decision.LAUNCH_ASSIGNED, detector.onHomeDelivery(800))
    }

    @Test
    fun window500() {
        var now = 10_000L
        val detector = HatchDetector { now }
        detector.onHomeDelivery(500)
        now = 10_500L
        assertEquals(HatchDetector.Decision.OPEN_HATCH, detector.onHomeDelivery(500))
    }

    @Test
    fun window1200() {
        var now = 10_000L
        val detector = HatchDetector { now }
        detector.onHomeDelivery(1200)
        now = 11_200L
        assertEquals(HatchDetector.Decision.OPEN_HATCH, detector.onHomeDelivery(1200))
        detector.reset()
        now = 20_000L
        detector.onHomeDelivery(1200)
        now = 21_201L
        assertEquals(HatchDetector.Decision.LAUNCH_ASSIGNED, detector.onHomeDelivery(1200))
    }
}
