package com.quantumfighter.gymtracker

import android.content.pm.PackageManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** T-01/T-15 foundation subset only; the complete workout scenario is later scope. */
@RunWith(AndroidJUnit4::class)
class FoundationSmokeTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun launcherRendersComposeAndSurvivesActivityRecreation() {
        compose.onNodeWithText("Gym Tracker").assertIsDisplayed()
        compose.onNodeWithText("Your workout space, on your device.").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Gym Tracker").assertIsDisplayed()
    }

    @Test
    fun installedApplicationHasNoNetworkPermission() {
        val context = compose.activity
        val info =
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()),
            )
        assertTrue(info.requestedPermissions.orEmpty().none { it == "android.permission.INTERNET" })
    }
}
