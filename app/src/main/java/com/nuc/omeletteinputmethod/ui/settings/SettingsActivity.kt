package com.nuc.omeletteinputmethod.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nuc.omeletteinputmethod.ui.theme.OmeletteIMETheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkOverlayPermission()
        
        setContent {
            OmeletteIMETheme {
                // Determine starting screen from intent
                val startScreen = intent.getStringExtra("start_screen") ?: "dashboard"
                SettingsScreen(
                    startDestination = startScreen,
                    onCheckPermission = { checkOverlayPermission() }
                )
            }
        }
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, 1)
            }
        }
    }
}
