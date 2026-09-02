package com.abeant.gretel.hatch

import com.abeant.gretel.data.AssignedAppStore

/** Why the settings screen opened instead of the chosen app. */
enum class HatchReason {
    NONE,
    ONBOARDING,
    MISSING_APP,
    LANDING,
    RELAUNCH_LOOP,
}

/**
 * Pure decision for one Home delivery. [com.abeant.gretel.HomeActivity] only
 * turns the result into an activity start.
 */
class HomeDispatcher(
    private val hatchDetector: HatchDetector,
    private val bootGate: BootGate,
) {
    sealed interface Decision {
        data class Launch(val packageName: String) : Decision
        data class OpenHatch(val reason: HatchReason) : Decision
    }

    fun decide(
        snapshot: AssignedAppStore.Snapshot,
        canLaunch: (String) -> Boolean,
    ): Decision {
        val firstSinceBoot = bootGate.consumeFirstHomeSinceBoot()

        if (!snapshot.onboardingDone) {
            return Decision.OpenHatch(HatchReason.ONBOARDING)
        }

        val assigned = snapshot.assignedPackage
        if (assigned.isNullOrBlank() || !canLaunch(assigned)) {
            return Decision.OpenHatch(HatchReason.MISSING_APP)
        }

        if (firstSinceBoot && !snapshot.openOnBoot) {
            return Decision.OpenHatch(HatchReason.LANDING)
        }

        return when (hatchDetector.onHomeDelivery(snapshot.hatchWindowMs)) {
            HatchDetector.Decision.OPEN_HATCH -> Decision.OpenHatch(HatchReason.NONE)
            HatchDetector.Decision.LAUNCH_ASSIGNED -> Decision.Launch(assigned)
        }
    }
}
