package com.nuc.omeletteinputmethod.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nuc.omeletteinputmethod.ui.theme.OmeletteIMETheme
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    @Inject
    lateinit var keyboardViewModel: KeyboardViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkOverlayPermission()

        val startScreen = intent.getStringExtra("start_screen") ?: Routes.DASHBOARD

        setContent {
            OmeletteIMETheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SettingsScreen(
                        startDestination = startScreen,
                        onCheckPermission = { checkOverlayPermission() },
                        keyboardViewModel = keyboardViewModel
                    )
                }
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