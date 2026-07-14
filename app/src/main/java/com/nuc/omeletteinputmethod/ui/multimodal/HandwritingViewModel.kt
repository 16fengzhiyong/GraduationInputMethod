package com.nuc.omeletteinputmethod.ui.multimodal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.sqrt

data class HandwritingState(
    val strokes: List<List<Offset>> = emptyList(),
    val candidates: List<String> = emptyList(),
    val resultPreview: String = "",
    val isRecognizing: Boolean = false,
)

@HiltViewModel
class HandwritingViewModel
@Inject
constructor(
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(HandwritingState())
    val state: StateFlow<HandwritingState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<KeyboardEffect>(replay = 0, extraBufferCapacity = 16)
    val effects: SharedFlow<KeyboardEffect> = _effects.asSharedFlow()

    private var tfliteInterpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private var currentStroke: MutableList<Offset> = mutableListOf()
    private var canvasWidth: Float = 280f
    private var canvasHeight: Float = 280f
    private var modelLoaded: Boolean = false

    data class BoundingBox(
        val minX: Float,
        val maxX: Float,
        val minY: Float,
        val maxY: Float,
    )

    fun setCanvasSize(width: Float, height: Float) {
        canvasWidth = width
        canvasHeight = height
    }

    fun loadModel() {
        if (modelLoaded) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val modelPath = "ml/handwriting_cnn.tflite"
                val assetFileDescriptor = appContext.assets.openFd(modelPath)
                val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
                val fileChannel = inputStream.channel
                val startOffset = assetFileDescriptor.startOffset
                val declaredLength = assetFileDescriptor.declaredLength
                val mappedBuffer: MappedByteBuffer =
                    fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

                tfliteInterpreter = Interpreter(mappedBuffer)

                val labelPath = "ml/handwriting_labels.txt"
                labels =
                    appContext.assets.open(labelPath).bufferedReader().readLines()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .take(3755)

                modelLoaded = true
            } catch (_: Exception) {
            }
        }
    }

    fun onTouchStart(offset: Offset) {
        if (!modelLoaded) loadModel()
        currentStroke.clear()
        currentStroke.add(offset)
    }

    fun onTouchMove(offset: Offset) {
        currentStroke.add(offset)
    }

    fun onTouchEnd() {
        if (currentStroke.isNotEmpty()) {
            _state.update { st ->
                st.copy(strokes = st.strokes + listOf(currentStroke.toList()))
            }
            currentStroke.clear()
        }
    }

    fun recognize() {
        val strokes = _state.value.strokes
        if (strokes.isEmpty()) return

        _state.update { it.copy(isRecognizing = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val candidates =
                    if (tfliteInterpreter != null && modelLoaded) {
                        recognizeWithTFLite(strokes)
                    } else {
                        recognizeWithShapeMatching(strokes)
                    }
                withContext(Dispatchers.Main) {
                    _state.update {
                        it.copy(
                            candidates = candidates,
                            resultPreview = candidates.firstOrNull() ?: "",
                            isRecognizing = false,
                        )
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _state.update { it.copy(isRecognizing = false) }
                }
            }
        }
    }

    private fun recognizeWithTFLite(strokes: List<List<Offset>>): List<String> {
        val interpreter = tfliteInterpreter ?: return emptyList()
        val bbox = computeBoundingBox(strokes)
        val bitmap = rasterizeStrokes(strokes, bbox, 64, 64)
        val inputBuffer =
            ByteBuffer.allocateDirect(64 * 64 * 1 * 4)
                .order(ByteOrder.nativeOrder())

        for (y in 0 until 64) {
            for (x in 0 until 64) {
                val pixel = bitmap.getPixel(x, y)
                val gray = (Color.red(pixel) * 0.299f + Color.green(pixel) * 0.587f + Color.blue(pixel) * 0.114f) / 255.0f
                inputBuffer.putFloat(gray)
            }
        }
        inputBuffer.rewind()

        val outputArray = Array(1) { FloatArray(labels.size) }
        interpreter.run(inputBuffer, outputArray)

        val scores = outputArray[0]
        val indexedScores = scores.mapIndexed { index, score -> index to score }
            .sortedByDescending { it.second }
            .take(10)

        return indexedScores.mapNotNull { (index, _) ->
            if (index < labels.size) labels[index] else null
        }
    }

    private fun recognizeWithShapeMatching(strokes: List<List<Offset>>): List<String> {
        val strokePattern = strokes.map { stroke ->
            val directions = mutableListOf<Char>()
            for (i in 1 until stroke.size) {
                val dx = stroke[i].x - stroke[i - 1].x
                val dy = stroke[i].y - stroke[i - 1].y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 10f) continue
                val angle = Math.toDegrees(atan2D(dy.toDouble(), dx.toDouble()))
                directions.add(
                    when {
                        angle in -22.5..22.5 -> '0'
                        angle in 22.5..67.5 -> '1'
                        angle in 67.5..112.5 -> '2'
                        angle in 112.5..157.5 -> '3'
                        angle in -67.5..-22.5 -> '7'
                        angle in -112.5..-67.5 -> '6'
                        angle in -157.5..-112.5 -> '5'
                        else -> '4'
                    }
                )
            }
            compactDirections(directions)
        }
        val code = strokePattern.joinToString("")

        return loadStrokeMapMatches(code).take(10)
    }

    private fun compactDirections(dirs: List<Char>): String {
        if (dirs.isEmpty()) return ""
        val sb = StringBuilder()
        var prev = dirs[0]
        var count = 1
        for (i in 1 until dirs.size) {
            if (dirs[i] == prev) {
                count++
            } else {
                sb.append(prev)
                prev = dirs[i]
                count = 1
            }
        }
        sb.append(prev)
        return sb.toString()
    }

    private fun loadStrokeMapMatches(code: String): List<String> {
        return try {
            val stream = appContext.assets.open("stroke_map.txt")
            val results = mutableListOf<String>()
            stream.bufferedReader().use { reader ->
                reader.forEachLine { line ->
                    val parts = line.split("\t")
                    if (parts.size >= 2) {
                        val key = parts[0].trim()
                        val chars = parts[1].trim()
                        if (key.startsWith(code) || matchWildcard(key, code)) {
                            results.addAll(chars.toCharArray().map { it.toString() })
                        }
                    }
                }
            }
            results.take(10)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun matchWildcard(key: String, code: String): Boolean {
        if (key.length != code.length) return false
        for (i in key.indices) {
            if (code[i] != '*' && key[i] != code[i]) return false
        }
        return true
    }

    private fun computeBoundingBox(strokes: List<List<Offset>>): BoundingBox {
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE
        for (stroke in strokes) {
            for (pt in stroke) {
                if (pt.x < minX) minX = pt.x
                if (pt.x > maxX) maxX = pt.x
                if (pt.y < minY) minY = pt.y
                if (pt.y > maxY) maxY = pt.y
            }
        }
        if (maxX - minX < 10f) {
            maxX = minX + 10f
        }
        if (maxY - minY < 10f) {
            maxY = minY + 10f
        }
        return BoundingBox(minX, maxX, minY, maxY)
    }

    private fun rasterizeStrokes(
        strokes: List<List<Offset>>,
        bbox: BoundingBox,
        targetW: Int,
        targetH: Int,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint =
            Paint().apply {
                color = Color.BLACK
                strokeWidth = 4f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
                style = Paint.Style.STROKE
            }

        val scaleX = targetW.toFloat() / (bbox.maxX - bbox.minX)
        val scaleY = targetH.toFloat() / (bbox.maxY - bbox.minY)
        val scale = min(scaleX, scaleY) * 0.85f
        val padX = (targetW - (bbox.maxX - bbox.minX) * scale) / 2f
        val padY = (targetH - (bbox.maxY - bbox.minY) * scale) / 2f
        val offsetX = padX - bbox.minX * scale
        val offsetY = padY - bbox.minY * scale

        for (stroke in strokes) {
            if (stroke.size < 2) continue
            val path = Path()
            path.moveTo(stroke[0].x * scale + offsetX, stroke[0].y * scale + offsetY)
            for (i in 1 until stroke.size) {
                path.lineTo(stroke[i].x * scale + offsetX, stroke[i].y * scale + offsetY)
            }
            canvas.drawPath(path, paint)
        }

        return bitmap
    }

    private fun atan2D(y: Double, x: Double): Double {
        val rad = kotlin.math.atan2(y, x)
        return rad
    }

    fun onCandidateSelected(candidate: String) {
        _state.update { it.copy(strokes = emptyList(), candidates = emptyList(), resultPreview = "") }
        viewModelScope.launch { _effects.emit(KeyboardEffect.CommitText(candidate)) }
    }

    fun clearStrokes() {
        _state.update { it.copy(strokes = emptyList(), candidates = emptyList(), resultPreview = "") }
    }

    fun commitSpace() {
        viewModelScope.launch { _effects.emit(KeyboardEffect.CommitText(" ")) }
    }

    fun commitEnter() {
        viewModelScope.launch { _effects.emit(KeyboardEffect.CommitText("\n")) }
    }

    fun deleteLastStroke() {
        val strokes = _state.value.strokes
        if (strokes.isNotEmpty()) {
            _state.update { it.copy(strokes = strokes.dropLast(1), candidates = emptyList(), resultPreview = "") }
        } else {
            viewModelScope.launch { _effects.emit(KeyboardEffect.DeleteBackward) }
        }
    }

    override fun onCleared() {
        tfliteInterpreter?.close()
        tfliteInterpreter = null
        super.onCleared()
    }
}