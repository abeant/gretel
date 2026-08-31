package com.abeant.gretel.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppCatalogTest {

    @Test
    fun prefersKoReaderInListedOrder() {
        val installed = listOf(
            "org.koreader.launcher.npi",
            "org.koreader.launcher",
            "org.ko.reader",
            "com.example.reader",
        )
        assertEquals("org.ko.reader", AppCatalog.preferredKoreader(installed))
    }

    @Test
    fun prefersSecondAliasWhenFirstMissing() {
        val installed = listOf("org.koreader.launcher", "com.example.reader")
        assertEquals("org.koreader.launcher", AppCatalog.preferredKoreader(installed))
    }

    @Test
    fun prefersNpiWhenOnlyNpiPresent() {
        assertEquals(
            "org.koreader.launcher.npi",
            AppCatalog.preferredKoreader(listOf("org.koreader.launcher.npi")),
        )
    }

    @Test
    fun prefersFdroidBuildWhenOnlyFdroidPresent() {
        assertEquals(
            "org.koreader.launcher.fdroid",
            AppCatalog.preferredKoreader(listOf("org.koreader.launcher.fdroid")),
        )
    }

    @Test
    fun noKoreaderMeansNoDefault() {
        assertNull(AppCatalog.preferredKoreader(listOf("com.example.reader")))
    }

    @Test
    fun sortPutsKoreaderAliasesFirstThenAz() {
        val apps = listOf(
            app("Zebra", "com.z"),
            app("KOReader NPI", "org.koreader.launcher.npi"),
            app("alpha", "com.a"),
            app("KOReader", "org.koreader.launcher"),
            app("Middle", "com.m"),
            app("KO", "org.ko.reader"),
        )
        val sorted = AppCatalog.sortLaunchable(apps).map { it.packageName }
        assertEquals(
            listOf(
                "org.ko.reader",
                "org.koreader.launcher",
                "org.koreader.launcher.npi",
                "com.a",
                "com.m",
                "com.z",
            ),
            sorted,
        )
    }

    @Test
    fun sortIgnoresMissingKoreaderAliases() {
        val apps = listOf(
            app("Books", "com.books"),
            app("KOReader", "org.koreader.launcher"),
        )
        val sorted = AppCatalog.sortLaunchable(apps).map { it.packageName }
        assertEquals(listOf("org.koreader.launcher", "com.books"), sorted)
    }

    private fun app(label: String, packageName: String) = LaunchableApp(
        packageName = packageName,
        label = label,
    )
}
