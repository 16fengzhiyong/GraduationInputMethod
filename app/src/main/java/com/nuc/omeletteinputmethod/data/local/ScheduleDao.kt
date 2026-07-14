package com.nuc.omeletteinputmethod.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nuc.omeletteinputmethod.data.model.ScheduleItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules ORDER BY startTime ASC")
    fun getAllSchedules(): Flow<List<ScheduleItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(item: ScheduleItem)
    
    @Update
    suspend fun updateSchedule(item: ScheduleItem)
    
    @Delete
    suspend fun deleteSchedule(item: ScheduleItem)
}
