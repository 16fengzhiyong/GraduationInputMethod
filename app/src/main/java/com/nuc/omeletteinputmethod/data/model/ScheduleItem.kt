package com.nuc.omeletteinputmethod.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val isDone: Boolean = false
)
