package com.nuc.omeletteinputmethod.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuc.omeletteinputmethod.data.local.UserDictionaryDao
import com.nuc.omeletteinputmethod.data.model.InputStatDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class InputStatsUiState(
    val totalChars: Int = 0,
    val dailyAvg: Int = 0,
    val activeDays: Int = 0,
    val topWords: List<String> = emptyList(),
    val dailyStats: List<DailyStatItem> = emptyList(),
    val heatmapData: Map<String, Int> = emptyMap(),
    val currentMonth: String = "",
    val isLoading: Boolean = true,
)

data class DailyStatItem(
    val date: String,
    val totalChars: Int,
    val totalWords: Int,
)

@HiltViewModel
class InputStatsViewModel @Inject constructor(
    private val inputStatDao: InputStatDao,
    private val userDictionaryDao: UserDictionaryDao,
) : ViewModel() {
    private val _state = MutableStateFlow(InputStatsUiState())
    val state: StateFlow<InputStatsUiState> = _state.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init {
        loadStats()
    }

    fun loadStats() {
        val cal = Calendar.getInstance()
        val currentMonth = dateFormat.format(cal.time)
        _state.value = _state.value.copy(currentMonth = currentMonth)

        viewModelScope.launch {
            val totalChars = inputStatDao.totalInputChars().firstOrNull() ?: 0
            val activeDays = inputStatDao.activeDays().firstOrNull() ?: 0
            val dailyAvg = if (activeDays > 0) totalChars / activeDays else 0

            val dailyStats = inputStatDao.getDailyStats(30).firstOrNull().orEmpty()
            val dailyStatItems = dailyStats.map {
                DailyStatItem(it.date, it.totalChars, it.totalWords)
            }

            val monthPrefix = "$currentMonth-"
            val heatmapRaw = inputStatDao.getHeatmapData(monthPrefix).firstOrNull().orEmpty()
            val heatmap = heatmapRaw.associate { it.date to it.totalChars }

            val topWords = userDictionaryDao.getTopNWords(20, minFreq = 1).firstOrNull().orEmpty()
            val topWordStrings = topWords.map { it.word }

            _state.value =
                _state.value.copy(
                    totalChars = totalChars,
                    dailyAvg = dailyAvg,
                    activeDays = activeDays,
                    topWords = topWordStrings,
                    dailyStats = dailyStatItems,
                    heatmapData = heatmap,
                    isLoading = false,
                )
        }
    }

    fun navigateMonth(increment: Int) {
        val cal = Calendar.getInstance()
        val parts = _state.value.currentMonth.split("-")
        if (parts.size == 2) {
            cal.set(Calendar.YEAR, parts[0].toInt())
            cal.set(Calendar.MONTH, parts[1].toInt() - 1)
            cal.add(Calendar.MONTH, increment)
            val newMonth = dateFormat.format(cal.time)
            _state.value = _state.value.copy(currentMonth = newMonth, isLoading = true)
            loadStats()
        }
    }
}