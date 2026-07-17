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
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.nuc.omeletteinputmethod.data.repository.AuthRepository
import com.nuc.omeletteinputmethod.data.repository.ClipboardRepository
import com.nuc.omeletteinputmethod.ui.keyboard.KeyType
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardEffect
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardScreen
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardViewModel
import com.nuc.omeletteinputmethod.ui.multimodal.HandwritingViewModel
import com.nuc.omeletteinputmethod.ui.multimodal.StrokeInputViewModel
import com.nuc.omeletteinputmethod.ui.multimodal.VoiceInputViewModel
import com.nuc.omeletteinputmethod.ui.theme.OmeletteIMEKeyboardTheme
import com.nuc.omeletteinputmethod.ui.theme.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import com.nuc.omeletteinputmethod.R
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

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var authRepository: AuthRepository

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
                        KeyType.SWIPE -> HapticFeedbackConstants.KEYBOARD_PRESS  // distinct feedback for swipe-up
                    }
                composeView?.performHapticFeedback(hapticConstant)
            }
            is KeyboardEffect.PlaySound -> {
                soundPool?.play(tickSoundId, effect.volume, effect.volume, 1, 0, 1.0f)
            }
            is KeyboardEffect.SwitchIME -> {
                // In-app IME switcher: open system input method picker
                window?.window?.also {
                    val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                    imm?.showInputMethodPicker()
                }
            }
            is KeyboardEffect.OpenSettings -> {
                // Open the app's settings / dashboard activity
                val intent = android.content.Intent(this, com.nuc.omeletteinputmethod.ui.settings.SettingsActivity::class.java)
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            is KeyboardEffect.HideKeyboard -> {
                requestHideSelf(0)
            }
            is KeyboardEffect.CursorLeft -> {
                inputConnection?.sendKeyEvent(
                    android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_DOWN,
                        android.view.KeyEvent.KEYCODE_DPAD_LEFT,
                    ),
                )
                inputConnection?.sendKeyEvent(
                    android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_UP,
                        android.view.KeyEvent.KEYCODE_DPAD_LEFT,
                    ),
                )
            }
            is KeyboardEffect.CursorRight -> {
                inputConnection?.sendKeyEvent(
                    android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_RIGHT),
                )
                inputConnection?.sendKeyEvent(
                    android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_RIGHT),
                )
            }
            is KeyboardEffect.CursorUp -> {
                inputConnection?.sendKeyEvent(
                    android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_UP),
                )
                inputConnection?.sendKeyEvent(
                    android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_UP),
                )
            }
            is KeyboardEffect.CursorDown -> {
                inputConnection?.sendKeyEvent(
                    android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_DOWN),
                )
                inputConnection?.sendKeyEvent(
                    android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_DOWN),
                )
            }
            is KeyboardEffect.Home -> {
                inputConnection?.sendKeyEvent(
                    android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MOVE_HOME),
                )
                inputConnection?.sendKeyEvent(
                    android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_MOVE_HOME),
                )
            }
            is KeyboardEffect.End -> {
                inputConnection?.sendKeyEvent(
                    android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MOVE_END),
                )
                inputConnection?.sendKeyEvent(
                    android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_MOVE_END),
                )
            }
            is KeyboardEffect.Copy -> {
                inputConnection?.performContextMenuAction(android.R.id.copy)
            }
            is KeyboardEffect.Cut -> {
                inputConnection?.performContextMenuAction(android.R.id.cut)
            }
            is KeyboardEffect.PasteFromClipboard -> {
                inputConnection?.performContextMenuAction(android.R.id.paste)
            }
            is KeyboardEffect.SelectAll -> {
                inputConnection?.performContextMenuAction(android.R.id.selectAll)
            }
            is KeyboardEffect.Undo -> {
                // Android 8.0+ 支持系统撤销栈，通过 context menu action 触发
                inputConnection?.performContextMenuAction(android.R.id.undo)
            }
            is KeyboardEffect.Redo -> {
                // Android 8.0+ 支持系统重做栈
                inputConnection?.performContextMenuAction(android.R.id.redo)
            }
            is KeyboardEffect.DeleteForward -> {
                // 删除光标后一个字符（前删已在 DeleteBackward 中处理）
                inputConnection?.deleteSurroundingText(0, 1)
            }
        }
    }

    override fun onCreateInputView(): View {
        val cv = ComposeView(this)
        composeView = cv

        // Set on ComposeView itself
        cv.setViewTreeLifecycleOwner(this)
        cv.setViewTreeViewModelStoreOwner(this)
        cv.setViewTreeSavedStateRegistryOwner(this)

        // Give the keyboard a minimum height so the system allocates
        // enough vertical space for the IME window.
        cv.minimumHeight = 240.dpToPx()

        // Eagerly apply lifecycle owners to the IME window root decor view.
        // InputMethodService creates its Dialog lazily — touching `window`
        // here forces creation so decorView exists before setInputView→addView
        // triggers ComposeView.onAttachedToWindow, which traverses the
        // ancestor chain looking for ViewTreeLifecycleOwner.
        applyOwnerToWindow()

        cv.setContent {
            OmeletteIMEKeyboardTheme(themeManager) {
                KeyboardScreen(
                    viewModel = viewModel,
                    handwritingViewModel = handwritingViewModel,
                    voiceViewModel = voiceViewModel,
                    strokeViewModel = strokeViewModel,
                    themeManager = themeManager,
                )
            }
        }
        return cv
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()

/**
     * Spread our LifecycleOwner / ViewModelStoreOwner / SavedStateRegistryOwner
     * to the IME window's root decor view so Compose can find them when traversing
     * the view-tree ancestor chain (parentPanel → … → ComposeView).
     *
     * Called both from onCreate (pre-apply for the initial Dialog window) and
     * onWindowShown (re-apply whenever the IME window is reshown).
     */
    private fun applyOwnerToWindow() {
        val decorView = window?.window?.decorView ?: return
        decorView.setViewTreeLifecycleOwner(this)
        decorView.setViewTreeViewModelStoreOwner(this)
        decorView.setViewTreeSavedStateRegistryOwner(this)
    }

    override fun onWindowShown() {
        super.onWindowShown()
        applyOwnerToWindow()
    }

    override fun onStartInput(
        attribute: android.view.inputmethod.EditorInfo?,
        restarting: Boolean,
    ) {
        super.onStartInput(attribute, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        val isPasswordField = attribute?.let {
            val inputType = it.inputType
            (inputType and android.text.InputType.TYPE_MASK_CLASS) == android.text.InputType.TYPE_CLASS_TEXT &&
            (inputType and android.text.InputType.TYPE_MASK_VARIATION).let { variation ->
                variation == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                variation == android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            }
        } ?: false

        viewModel.setSecureMode(isPasswordField)

        if (!isPasswordField) {
            clipboardRepository.startMonitoring()
        }

        // Retry dictionary init on every input session in case first attempt failed
        viewModel.retryDictionaryInit()

        effectCollectionJob?.cancel()
        effectCollectionJob =
            lifecycleScope.launch {
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

override fun onEvaluateInputViewShown(): Boolean = true

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

    override val lifecycle: Lifecycle get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
}
