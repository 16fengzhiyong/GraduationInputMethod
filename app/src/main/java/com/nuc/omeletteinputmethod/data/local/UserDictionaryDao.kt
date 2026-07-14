package com.nuc.omeletteinputmethod.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
    suspend fun updateFrequency(id: Long, timestamp: Long)
}
