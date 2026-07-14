package com.nuc.omeletteinputmethod.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nuc.omeletteinputmethod.data.model.ShortcutItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcuts WHERE packageName = :packageName OR packageName = 'global'")
    fun getShortcutsForPackage(packageName: String): Flow<List<ShortcutItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(item: ShortcutItem)

    @Delete
    suspend fun deleteShortcut(item: ShortcutItem)
}
