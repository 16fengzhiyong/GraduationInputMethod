package com.nuc.omeletteinputmethod.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shortcuts")
data class ShortcutItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String, // Associated App Package
    val content: String, // The text shortcut
    val label: String // Display label
)
