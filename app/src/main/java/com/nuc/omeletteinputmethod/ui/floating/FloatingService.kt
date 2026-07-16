package com.nuc.omeletteinputmethod.ui.floating

import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import android.provider.Settings
import android.widget.Toast
import com.nuc.omeletteinputmethod.ui.theme.OmeletteIMETheme
class FloatingService : LifecycleService(), ViewModelStoreOwner, SavedStateRegistryOwner {
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var composeView: ComposeView

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
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
                setViewTreeLifecycleOwner(this@FloatingService)
                setViewTreeViewModelStoreOwner(this@FloatingService)
                setViewTreeSavedStateRegistryOwner(this@FloatingService)

                setContent {
                    OmeletteIMETheme {
                        FloatingWindowContent(
                            onDrag = { dx, dy ->
                                val lp = this@FloatingService.layoutParams
                                lp.x += dx.toInt()
                                lp.y += dy.toInt()
                                this@FloatingService.windowManager.updateViewLayout(this@FloatingService.composeView, lp)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "需要「悬浮窗」权限才能使用此功能", Toast.LENGTH_LONG).show()
            stopSelf()
            return
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
            try {
                windowManager.removeView(composeView)
            } catch (_: Exception) { }
        }
        store.clear()
    }
}
