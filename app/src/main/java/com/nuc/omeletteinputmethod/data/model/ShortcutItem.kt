package com.nuc.omeletteinputmethod.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shortcuts")
data class ShortcutItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String = "global",
    val content: String,
    val label: String,
    val category: String = "",
    val sortOrder: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
)
