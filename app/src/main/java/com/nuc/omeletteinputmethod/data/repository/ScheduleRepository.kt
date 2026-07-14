package com.nuc.omeletteinputmethod.data.repository

import com.nuc.omeletteinputmethod.data.local.ScheduleDao
import com.nuc.omeletteinputmethod.data.model.ScheduleItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val scheduleDao: ScheduleDao
) {
    fun getAllSchedules(): Flow<List<ScheduleItem>> = scheduleDao.getAllSchedules()

    suspend fun addSchedule(item: ScheduleItem) {
         scheduleDao.insertSchedule(item)
    }
    
    suspend fun updateSchedule(item: ScheduleItem) {
        scheduleDao.updateSchedule(item)
    }

    suspend fun deleteSchedule(item: ScheduleItem) {
        scheduleDao.deleteSchedule(item)
    }
}
