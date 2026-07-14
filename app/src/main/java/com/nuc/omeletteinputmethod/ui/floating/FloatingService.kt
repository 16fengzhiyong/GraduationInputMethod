package com.nuc.omeletteinputmethod.ui.floating

import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.nuc.omeletteinputmethod.ui.theme.OmeletteIMETheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FloatingService : LifecycleService() {
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var composeView: ComposeView

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        layoutParams =
            WindowManager.LayoutParams().apply {
                type =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        WindowManager.LayoutParams.TYPE_PHONE
                    }
                format = PixelFormat.TRANSLUCENT
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = 100
            }

        composeView =
            ComposeView(this).apply {
                ViewTreeLifecycleOwner.set(this, this@FloatingService)
                ViewTreeViewModelStoreOwner.set(this, this@FloatingService)
                ViewTreeSavedStateRegistryOwner.set(this, this@FloatingService)

                setContent {
                    OmeletteIMETheme {
                        FloatingWindowContent(
                            onDrag = { x, y ->
                                layoutParams.x += x.toInt()
                                layoutParams.y += y.toInt()
                                windowManager.updateViewLayout(composeView, layoutParams)
                            },
                            onClick = { /* Expand logic inside content for now */ },
                            onClose = { stopSelf() },
                            onNavigate = { screen ->
                                val intent =
                                    android
                                        .content
                                        .Intent(
                                            this@FloatingService,
                                            com
                                                .nuc
                                                .omeletteinputmethod
                                                .ui
                                                .settings
                                                .SettingsActivity::class.java,
                                        ).apply {
                                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                            putExtra("start_screen", screen)
                                        }
                                startActivity(intent)
                            },
                        )
                    }
                }
            }

        windowManager.addView(composeView, layoutParams)
    }

    private fun updatePosition(
        deltaX: Float,
        deltaY: Float,
    ) {
        layoutParams.x += deltaX.toInt()
        layoutParams.y += deltaY.toInt()
        windowManager.updateViewLayout(composeView, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::composeView.isInitialized) {
            windowManager.removeView(composeView)
        }
    }
}
