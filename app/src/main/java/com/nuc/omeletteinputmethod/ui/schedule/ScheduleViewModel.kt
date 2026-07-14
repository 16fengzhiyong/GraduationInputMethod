package com.nuc.omeletteinputmethod.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuc.omeletteinputmethod.data.model.ScheduleItem
import com.nuc.omeletteinputmethod.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {

    val schedules: StateFlow<List<ScheduleItem>> = repository.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSchedule(title: String, description: String, startTime: Long, endTime: Long) {
        viewModelScope.launch {
            repository.addSchedule(
                ScheduleItem(
                    title = title,
                    description = description,
                    startTime = startTime,
                    endTime = endTime
                )
            )
        }
    }
    
    fun toggleDone(item: ScheduleItem) {
        viewModelScope.launch {
            repository.updateSchedule(item.copy(isDone = !item.isDone))
        }
    }

    fun deleteSchedule(item: ScheduleItem) {
        viewModelScope.launch {
            repository.deleteSchedule(item)
        }
    }
}
