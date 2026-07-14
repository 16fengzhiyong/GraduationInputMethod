package com.nuc.omeletteinputmethod.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_dictionary")
data class UserDictionary(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val frequency: Int = 0,
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)
