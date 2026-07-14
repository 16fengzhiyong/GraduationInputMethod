package com.nuc.omeletteinputmethod.kernel

import android.inputmethodservice.InputMethodService
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardScreen
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardViewModel
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardEffect
import com.nuc.omeletteinputmethod.ui.theme.OmeletteIMETheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

@AndroidEntryPoint
class OmeletteIME : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    @Inject
    lateinit var viewModel: KeyboardViewModel

    // Lifecycle/ViewModel boilerplate for Compose in Service
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        lifecycleRegistry.lifecycleScope.launch {
            viewModel.effects.collect { effect ->
                val inputConnection = currentInputConnection
                if (inputConnection != null) {
                    when (effect) {
                        is KeyboardEffect.CommitText -> inputConnection.commitText(effect.text, 1)
                        is KeyboardEffect.DeleteBackward -> inputConnection.deleteSurroundingText(1, 0)
                    }
                }
            }
        }
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this)
        
        // Bind Lifecycle/ViewModel owners for Compose
        ViewTreeLifecycleOwner.set(composeView, this)
        ViewTreeViewModelStoreOwner.set(composeView, this)
        ViewTreeSavedStateRegistryOwner.set(composeView, this)

        composeView.setContent {
            OmeletteIMETheme {
                KeyboardScreen(viewModel = viewModel)
            }
        }
        return composeView
    }
    
    override fun onStartInput(attribute: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }
    
    override fun onFinishInput() {
        super.onFinishInput()
        // lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP) 
        // Be careful stopping lifecycle repeatedly in IME as it might kill ViewModel 
        // Usually IME service stays alive, but input session changes.
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry
    override fun getViewModelStore(): ViewModelStore = store
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
}
