package com.nuc.omeletteinputmethod.kernel

import android.inputmethodservice.InputMethodService
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.nuc.omeletteinputmethod.data.repository.ClipboardRepository
import com.nuc.omeletteinputmethod.ui.keyboard.KeyType
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardEffect
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardScreen
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardViewModel
import com.nuc.omeletteinputmethod.ui.multimodal.HandwritingViewModel
import com.nuc.omeletteinputmethod.ui.multimodal.StrokeInputViewModel
import com.nuc.omeletteinputmethod.ui.multimodal.VoiceInputViewModel
import com.nuc.omeletteinputmethod.ui.theme.OmeletteIMETheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OmeletteIME : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    @Inject
    lateinit var viewModel: KeyboardViewModel

    @Inject
    lateinit var handwritingViewModel: HandwritingViewModel

    @Inject
    lateinit var voiceViewModel: VoiceInputViewModel

    @Inject
    lateinit var strokeViewModel: StrokeInputViewModel

    @Inject
    lateinit var clipboardRepository: ClipboardRepository

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private var effectCollectionJob: Job? = null

    // Added by Agent B — SoundPool for key click audio
    private var soundPool: SoundPool? = null
    private var tickSoundId: Int = 0
    private var composeView: ComposeView? = null

    companion object {}

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        // Added by Agent B — initialize SoundPool for key click sounds
        val audioAttributes =
            AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

        soundPool =
            SoundPool
                .Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build()

        tickSoundId = soundPool?.load(this, R.raw.tick, 1) ?: 0
    }

    private suspend fun handleEffect(effect: KeyboardEffect) {
        val inputConnection = currentInputConnection
        when (effect) {
            is KeyboardEffect.CommitText -> {
                inputConnection?.commitText(effect.text, 1)
            }
            is KeyboardEffect.DeleteBackward -> {
                inputConnection?.deleteSurroundingText(1, 0)
            }
            is KeyboardEffect.Paste -> {
                inputConnection?.commitText(effect.text, 1)
            }
            is KeyboardEffect.Vibrate -> {
                val hapticConstant =
                    when (effect.keyType) {
                        KeyType.NORMAL -> HapticFeedbackConstants.KEYBOARD_TAP
                        KeyType.SPECIAL -> HapticFeedbackConstants.KEYBOARD_PRESS
                        KeyType.LONG_PRESS -> HapticFeedbackConstants.LONG_PRESS
                    }
                composeView?.performHapticFeedback(hapticConstant)
            }
            is KeyboardEffect.PlaySound -> {
                soundPool?.play(tickSoundId, effect.volume, effect.volume, 1, 0, 1.0f)
            }
        }
    }

    override fun onCreateInputView(): View {
        val cv = ComposeView(this)
        composeView = cv

        ViewTreeLifecycleOwner.set(cv, this)
        ViewTreeViewModelStoreOwner.set(cv, this)
        ViewTreeSavedStateRegistryOwner.set(cv, this)

        cv.setContent {
            OmeletteIMETheme {
                KeyboardScreen(viewModel = viewModel)
            }
        }
        return cv
    }

    override fun onStartInput(
        attribute: android.view.inputmethod.EditorInfo?,
        restarting: Boolean,
    ) {
        super.onStartInput(attribute, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        clipboardRepository.startMonitoring()

        effectCollectionJob?.cancel()
        effectCollectionJob =
            lifecycleRegistry.lifecycleScope.launch {
                launch {
                    viewModel.effects.collect { effect -> handleEffect(effect) }
                }
                launch {
                    handwritingViewModel.effects.collect { effect -> handleEffect(effect) }
                }
                launch {
                    voiceViewModel.effects.collect { effect -> handleEffect(effect) }
                }
                launch {
                    strokeViewModel.effects.collect { effect -> handleEffect(effect) }
                }
            }
    }

    override fun onFinishInput() {
        super.onFinishInput()
        effectCollectionJob?.cancel()
        effectCollectionJob = null
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        effectCollectionJob?.cancel()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModel.onCleared()
        store.clear()
        // Added by Agent B — release SoundPool
        soundPool?.release()
        soundPool = null
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    override fun getViewModelStore(): ViewModelStore = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
}
