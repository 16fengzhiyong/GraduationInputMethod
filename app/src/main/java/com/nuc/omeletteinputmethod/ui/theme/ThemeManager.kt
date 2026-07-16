package com.nuc.omeletteinputmethod.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

enum class NightModeStrategy { SYSTEM, TIME, LIGHT_SENSOR }

enum class KeyShapeType { ROUNDED, CIRCLE, SHARP }

data class KeyShapeConfig(val shapeType: KeyShapeType, val radiusDp: Float = 6f)

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences("omelette_theme_prefs", Context.MODE_PRIVATE)

    private val _currentSkin = MutableStateFlow(SkinPack.builtinDefault())
    val currentSkin: StateFlow<SkinPack> = _currentSkin.asStateFlow()

    private val _isNightMode = MutableStateFlow(false)
    val isNightMode: StateFlow<Boolean> = _isNightMode.asStateFlow()

    private val _nightStrategy = MutableStateFlow(NightModeStrategy.SYSTEM)
    val nightStrategy: StateFlow<NightModeStrategy> = _nightStrategy.asStateFlow()

    private val _autoNightMode = MutableStateFlow(true)
    val autoNightMode: StateFlow<Boolean> = _autoNightMode.asStateFlow()

    private val _nightSkinId = MutableStateFlow("night")
    val nightSkinId: StateFlow<String> = _nightSkinId.asStateFlow()

    private val _keyShape = MutableStateFlow(KeyShapeConfig(KeyShapeType.ROUNDED, 6f))
    val keyShape: StateFlow<KeyShapeConfig> = _keyShape.asStateFlow()

    private val _keySpacing = MutableStateFlow(4)
    val keySpacingDp: StateFlow<Int> = _keySpacing.asStateFlow()

    private var lightSensorListener: SensorEventListener? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var currentNightSkin: SkinPack = SkinPack.builtinNight()
    private var currentDaySkin: SkinPack = SkinPack.builtinDefault()
    private var builtinSkins: List<SkinPack> = SkinPack.BUILTIN_SKINS

    init {
        loadPreferences()
        startNightModeDetection()
    }

    private fun loadPreferences() {
        val skinId = prefs.getString(PREF_SKIN_ID, "default") ?: "default"
        val nightSkinIdStr = prefs.getString(PREF_NIGHT_SKIN_ID, "night") ?: "night"
        val strategyStr = prefs.getString(PREF_NIGHT_STRATEGY, "SYSTEM") ?: "SYSTEM"
        val autoNight = prefs.getBoolean(PREF_AUTO_NIGHT, true)
        val shapeStr = prefs.getString(PREF_KEY_SHAPE, "ROUNDED") ?: "ROUNDED"
        val shapeRadius = prefs.getFloat(PREF_KEY_RADIUS, 6f)
        val spacing = prefs.getInt(PREF_KEY_SPACING, 4)

        val shapeType = try { KeyShapeType.valueOf(shapeStr) } catch (_: Exception) { KeyShapeType.ROUNDED }
        val strategy = try { NightModeStrategy.valueOf(strategyStr) } catch (_: Exception) { NightModeStrategy.SYSTEM }

        _nightSkinId.value = nightSkinIdStr
        _nightStrategy.value = strategy
        _autoNightMode.value = autoNight
        _keyShape.value = KeyShapeConfig(shapeType, shapeRadius)
        _keySpacing.value = spacing

        currentDaySkin = builtinSkins.find { it.skinId == skinId } ?: SkinPack.builtinDefault()
        currentNightSkin = builtinSkins.find { it.skinId == nightSkinIdStr } ?: SkinPack.builtinNight()

        _isNightMode.value = false
        _currentSkin.value = currentDaySkin
    }

    fun setSkin(skin: SkinPack) {
        currentDaySkin = skin
        prefs.edit().putString(PREF_SKIN_ID, skin.skinId).apply()
        if (!_isNightMode.value) {
            _currentSkin.value = skin
        }
    }

    fun setNightSkin(skin: SkinPack) {
        currentNightSkin = skin
        _nightSkinId.value = skin.skinId
        prefs.edit().putString(PREF_NIGHT_SKIN_ID, skin.skinId).apply()
        if (_isNightMode.value) {
            _currentSkin.value = skin
        }
    }

    fun setCustomColors(primary: Color, surface: Color = Color(0xFFFAFAFA)) {
        val colors = SkinPack.deriveFromPrimary(primary, surface)
        val customSkin = SkinPack(skinId = "custom", skinName = "自定义", colors = colors)
        setSkin(customSkin)
    }

    fun setNightStrategy(strategy: NightModeStrategy) {
        _nightStrategy.value = strategy
        prefs.edit().putString(PREF_NIGHT_STRATEGY, strategy.name).apply()
        rescheduleNightDetection()
    }

    fun setAutoNightMode(enabled: Boolean) {
        _autoNightMode.value = enabled
        prefs.edit().putBoolean(PREF_AUTO_NIGHT, enabled).apply()
        if (!enabled) {
            _isNightMode.value = false
            _currentSkin.value = currentDaySkin
        } else {
            rescheduleNightDetection()
        }
    }

    fun setKeyShape(shape: KeyShapeConfig) {
        _keyShape.value = shape
        prefs.edit().putString(PREF_KEY_SHAPE, shape.shapeType.name).putFloat(PREF_KEY_RADIUS, shape.radiusDp).apply()
    }

    fun setKeySpacing(dp: Int) {
        _keySpacing.value = dp
        prefs.edit().putInt(PREF_KEY_SPACING, dp).apply()
    }

    fun getAllBuiltinSkins(): List<SkinPack> = builtinSkins

    fun getCurrentColorScheme() = _currentSkin.value.toColorScheme()

    private fun startNightModeDetection() {
        when (_nightStrategy.value) {
            NightModeStrategy.TIME -> startTimeBasedDetection()
            NightModeStrategy.LIGHT_SENSOR -> startLightSensorDetection()
            NightModeStrategy.SYSTEM -> observeSystemDarkMode()
        }
    }

    private fun rescheduleNightDetection() {
        stopLightSensor()
        startNightModeDetection()
    }

    private fun startTimeBasedDetection() {
        scope.launch {
            while (true) {
                val now = LocalTime.now()
                val isNight = now.hour >= 18 || now.hour < 6
                applyNightModeChange(isNight)
                kotlinx.coroutines.delay(60_000L)
            }
        }
    }

    private fun startLightSensorDetection() {
        val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) ?: return

        lightSensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || event.values.isEmpty()) return
                val lux = event.values[0]
                applyNightModeChange(lux < 10f)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun observeSystemDarkMode() {
        scope.launch {
            val configFlow = kotlinx.coroutines.flow.callbackFlow {
                val receiver = object : android.content.BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: android.content.Intent?) {
                        val nightModeFlags = appContext.resources.configuration.uiMode and
                            android.content.res.Configuration.UI_MODE_NIGHT_MASK
                        val isDark = nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
                        try { channel.trySend(isDark) } catch (_: Exception) {}
                    }
                }
                val filter = android.content.IntentFilter(android.content.Intent.ACTION_CONFIGURATION_CHANGED)
                appContext.registerReceiver(receiver, filter)
                awaitClose { appContext.unregisterReceiver(receiver) }
            }
            configFlow.collect { isDark ->
                applyNightModeChange(isDark)
            }
        }
        val nightModeFlags = appContext.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val isDark = nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
        applyNightModeChange(isDark)
    }

    private fun applyNightModeChange(isNight: Boolean) {
        if (!_autoNightMode.value) return
        if (_isNightMode.value == isNight) return
        _isNightMode.value = isNight
        _currentSkin.value = if (isNight) currentNightSkin else currentDaySkin
    }

    private fun stopLightSensor() {
        lightSensorListener?.let { listener ->
            val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            sensorManager?.unregisterListener(listener)
            lightSensorListener = null
        }
    }

    fun onDestroy() {
        stopLightSensor()
    }

    fun exportCurrentSkinJson(): String = _currentSkin.value.toJson()

    fun importSkinFromJson(json: String): SkinPack? {
        val parsed = SkinPack.fromJson(json) ?: return null
        if (parsed.colors.size < 10) return null
        return parsed
    }

    companion object {
        private const val PREF_SKIN_ID = "theme_skin_id"
        private const val PREF_NIGHT_SKIN_ID = "theme_night_skin_id"
        private const val PREF_NIGHT_STRATEGY = "theme_night_strategy"
        private const val PREF_AUTO_NIGHT = "theme_auto_night"
        private const val PREF_KEY_SHAPE = "theme_key_shape"
        private const val PREF_KEY_RADIUS = "theme_key_radius"
        private const val PREF_KEY_SPACING = "theme_key_spacing"
    }
}