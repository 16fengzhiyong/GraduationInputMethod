package com.nuc.omeletteinputmethod.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.nuc.omeletteinputmethod.data.model.UserDictionary
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDictionaryDao {
    @Query("SELECT * FROM user_dictionary ORDER BY frequency DESC")
    fun getAllWords(): Flow<List<UserDictionary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: UserDictionary)

    @Query("SELECT * FROM user_dictionary WHERE word = :word LIMIT 1")
    suspend fun getWord(word: String): UserDictionary?

    @Query("UPDATE user_dictionary SET frequency = frequency + 1, lastUsedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateFrequency(
        id: Long,
        timestamp: Long,
    )

    @Query("SELECT * FROM user_dictionary WHERE word IN (:words)")
    suspend fun getFrequencies(words: List<String>): List<UserDictionary>

    @Query("SELECT pinned FROM user_dictionary WHERE word = :word")
    suspend fun isPinned(word: String): Boolean?

    @Query("SELECT * FROM user_dictionary WHERE frequency >= :threshold ORDER BY frequency DESC")
    fun getHighFrequencyWords(threshold: Int): Flow<List<UserDictionary>>

    @Query("SELECT * FROM user_dictionary WHERE frequency >= :minFreq ORDER BY frequency DESC LIMIT :n")
    fun getTopNWords(n: Int, minFreq: Int = 0): Flow<List<UserDictionary>>

    @Query("SELECT * FROM user_dictionary ORDER BY lastUsedTimestamp DESC LIMIT :limit")
    fun getRecentlyUsed(limit: Int): Flow<List<UserDictionary>>

    @Transaction
    suspend fun updateFrequencyBatch(words: List<String>) {
        val now = System.currentTimeMillis()
        for (word in words) {
            updateFrequencyByWord(word, now)
        }
    }

    @Query("UPDATE user_dictionary SET frequency = frequency + 1, lastUsedTimestamp = :timestamp WHERE word = :word")
    suspend fun updateFrequencyByWord(word: String, timestamp: Long)

    @Query("UPDATE user_dictionary SET pinned = :pinned WHERE word = :word")
    suspend fun updatePin(word: String, pinned: Boolean)

    // ── 云同步用：返回 List 而非 Flow ──

    @Query("SELECT * FROM user_dictionary")
    suspend fun getAllWordsList(): List<UserDictionary>
}
