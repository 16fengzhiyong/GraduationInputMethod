package com.nuc.omeletteinputmethod.data.model

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "user_bigram",
    primaryKeys = ["prevWord", "nextWord"],
)
data class UserBigram(
    val prevWord: String,
    val nextWord: String,
    val frequency: Int = 1,
    val lastUsed: Long = System.currentTimeMillis(),
)

@Dao
interface UserBigramDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBigram(bigram: UserBigram)

    @Query("SELECT frequency FROM user_bigram WHERE prevWord = :prev AND nextWord = :next LIMIT 1")
    suspend fun getBigramFreq(prev: String, next: String): Int?

    @Query("SELECT * FROM user_bigram ORDER BY frequency DESC LIMIT :limit")
    fun getTopBigrams(limit: Int): Flow<List<UserBigram>>
}