package com.nuc.omeletteinputmethod.data.model

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "input_stats")
data class InputStat(
    @PrimaryKey
    val date: String,
    val totalChars: Int = 0,
    val totalWords: Int = 0,
    val uniqueChars: Int = 0,
    val sessionCount: Int = 0,
    val avgSpeed: Float = 0f,
)

@Dao
interface InputStatDao {
    @Query("SELECT * FROM input_stats WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    suspend fun getStatsByDateRange(start: String, end: String): List<InputStat>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun batchInsert(stats: List<InputStat>)

    @Query("SELECT date, totalChars, 0 AS totalWords, 0 AS uniqueChars, 0 AS sessionCount, 0.0 AS avgSpeed FROM input_stats WHERE date LIKE :monthPrefix || '%' ORDER BY date ASC")
    fun getHeatmapData(monthPrefix: String): Flow<List<InputStat>>

    @Query("SELECT * FROM input_stats ORDER BY date DESC LIMIT :limit")
    fun getDailyStats(limit: Int): Flow<List<InputStat>>

    @Query("SELECT SUM(totalChars) FROM input_stats")
    fun totalInputChars(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM input_stats")
    fun activeDays(): Flow<Int?>

    @Query("SELECT date FROM input_stats ORDER BY date ASC LIMIT 1")
    suspend fun getFirstStatDate(): String?
}