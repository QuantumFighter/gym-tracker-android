package com.quantumfighter.gymtracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** B-01 / NFR-01, NFR-06: enforce boundaries without inventing product rules. */
class FoundationPolicyTest {
    private val appDir = File(checkNotNull(System.getProperty("app.projectDir")))
    private val sources = File(appDir, "src/main/kotlin/com/quantumfighter/gymtracker")

    @Test
    fun pureRulesCannotDependOnAndroidStorageOrUi() {
        assertImportsExclude(
            "domain",
            listOf(
                "android.",
                "androidx.",
                "com.quantumfighter.gymtracker.data",
                "com.quantumfighter.gymtracker.feature",
                "com.quantumfighter.gymtracker.ui",
                "com.quantumfighter.gymtracker.app",
            ),
        )
    }

    @Test
    fun storageCannotDependOnUiAndScreensCannotAccessStorage() {
        assertImportsExclude(
            "data",
            listOf(
                "androidx.compose.",
                "com.quantumfighter.gymtracker.feature",
                "com.quantumfighter.gymtracker.ui",
                "com.quantumfighter.gymtracker.app",
            ),
        )
        assertImportsExclude("feature", listOf("androidx.room.", "android.database."))
        assertImportsExclude("feature", listOf("com.quantumfighter.gymtracker.data")) { it.name.endsWith("Screen.kt") }
    }

    @Test
    fun manifestKeepsRuntimeOfflineAndAutomaticBackupDisabled() {
        val manifest =
            DocumentBuilderFactory
                .newInstance()
                .apply { isNamespaceAware = true }
                .newDocumentBuilder()
                .parse(File(appDir, "src/main/AndroidManifest.xml"))
        val permissions = manifest.getElementsByTagName("uses-permission")
        assertEquals("B-01 needs no runtime permissions", 0, permissions.length)
        val application = manifest.getElementsByTagName("application").item(0)
        assertEquals("false", application.attributes.getNamedItem("android:allowBackup").nodeValue)
    }

    private fun assertImportsExclude(
        directory: String,
        forbidden: List<String>,
        include: (File) -> Boolean = { true },
    ) {
        val boundary = File(sources, directory)
        assertTrue("Missing boundary: $directory", boundary.isDirectory)
        boundary.walkTopDown().filter { it.extension == "kt" && include(it) }.forEach { file ->
            val imports = file.readLines().map(String::trim).filter { it.startsWith("import ") }
            forbidden.forEach { dependency ->
                assertFalse("${file.name} imports forbidden $dependency", imports.any { it.startsWith("import $dependency") })
            }
        }
    }
}
