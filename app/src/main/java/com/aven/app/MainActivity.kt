package com.aven.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.aven.app.androidintegration.accessibility.AvenAccessibilityService
import com.aven.app.androidintegration.appdetection.SimulatedAppLaunchDetector
import com.aven.app.ui.AvenApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleInterventionIntent(intent)
        setContent {
            AvenApp()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleInterventionIntent(intent)
    }

    private fun handleInterventionIntent(intent: Intent?) {
        if (intent?.action == AvenAccessibilityService.ACTION_INTERVENTION) {
            val pkg = intent.getStringExtra(AvenAccessibilityService.EXTRA_PACKAGE_NAME) ?: return
            val name = intent.getStringExtra(AvenAccessibilityService.EXTRA_APP_NAME) ?: pkg
            lifecycleScope.launch {
                SimulatedAppLaunchDetector.instance.triggerLaunch(pkg, name)
            }
        }
    }
}
