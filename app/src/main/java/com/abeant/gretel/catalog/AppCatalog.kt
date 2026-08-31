package com.abeant.gretel.catalog

import android.content.Intent
import android.graphics.drawable.Drawable
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import java.text.Collator

data class LaunchableApp(
    val packageName: String,
    val label: String,
    val icon: Drawable? = null,
)

/**
 * MAIN+LAUNCHER catalog. Hides Gretel. KOReader aliases first, then A–Z.
 */
class AppCatalog(
    private val packageManager: PackageManager,
    private val selfPackage: String,
) {
    fun listLaunchable(): List<LaunchableApp> {
        val homeApps = queryHomePackages()
        val apps = queryResolveInfos().mapNotNull { info ->
            val packageName = info.activityInfo?.packageName ?: return@mapNotNull null
            if (packageName == selfPackage) return@mapNotNull null
            if (packageName in homeApps) return@mapNotNull null
            val label = info.loadLabel(packageManager).toString().ifBlank { packageName }
            LaunchableApp(
                packageName = packageName,
                label = label,
                icon = info.loadIcon(packageManager),
            )
        }
        return sortLaunchable(apps)
    }

    fun labelFor(packageName: String): String? {
        return try {
            val info = if (Build.VERSION.SDK_INT >= 33) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
            packageManager.getApplicationLabel(info).toString().ifBlank { packageName }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun isInstalled(packageName: String): Boolean {
        return try {
            val info = if (Build.VERSION.SDK_INT >= 33) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
            info.enabled
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun preferredDefault(listed: List<LaunchableApp> = listLaunchable()): LaunchableApp? {
        val preferred = preferredKoreader(listed.map { it.packageName }) ?: return null
        return listed.firstOrNull { it.packageName == preferred }
    }

    private fun queryHomePackages(): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return queryIntent(intent).mapNotNull { it.activityInfo?.packageName }.toSet()
    }

    private fun queryResolveInfos(): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return queryIntent(intent)
    }

    private fun queryIntent(intent: Intent): List<ResolveInfo> {
        return if (Build.VERSION.SDK_INT >= 33) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }
    }

    companion object {
        val KOREADER_PACKAGES = listOf(
            "org.ko.reader",
            "org.koreader.launcher",
            "org.koreader.launcher.npi",
            "org.koreader.launcher.fdroid",
        )

        fun isKoreaderPackage(packageName: String): Boolean =
            packageName in KOREADER_PACKAGES || packageName.startsWith("org.koreader.")

        fun preferredKoreader(installedPackages: Collection<String>): String? {
            val installed = installedPackages.toSet()
            KOREADER_PACKAGES.firstOrNull { it in installed }?.let { return it }
            return installed.firstOrNull { isKoreaderPackage(it) }
        }

        fun sortLaunchable(apps: List<LaunchableApp>): List<LaunchableApp> {
            val byPackage = apps.associateBy { it.packageName }
            val head = KOREADER_PACKAGES.mapNotNull { byPackage[it] }
            val headPackages = head.map { it.packageName }.toSet()
            val collator = Collator.getInstance().apply { strength = Collator.PRIMARY }
            val tail = apps
                .filter { it.packageName !in headPackages }
                .sortedWith { left, right ->
                    collator.compare(left.label, right.label).takeIf { it != 0 }
                        ?: left.packageName.compareTo(right.packageName)
                }
            return head + tail
        }
    }
}
