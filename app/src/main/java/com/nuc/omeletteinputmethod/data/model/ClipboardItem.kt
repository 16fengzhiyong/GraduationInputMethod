package com.nuc.omeletteinputmethod.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clipboard_history")
data class ClipboardItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sourcePkg: String? = null,
    val pinned: Boolean = false,
    val category: String = "",
)
