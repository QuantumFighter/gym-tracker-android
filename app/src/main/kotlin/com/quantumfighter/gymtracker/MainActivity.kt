package com.quantumfighter.gymtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.quantumfighter.gymtracker.app.GymTrackerApp

/** Android launcher that hosts [GymTrackerApp] and delegates screen rendering to Compose. */
class MainActivity : ComponentActivity() {
    /** Installs edge-to-edge Compose content after the activity's saved state is restored. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { GymTrackerApp() }
    }
}
