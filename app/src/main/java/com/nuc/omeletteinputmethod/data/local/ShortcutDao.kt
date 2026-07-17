package com.nuc.omeletteinputmethod.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nuc.omeletteinputmethod.data.model.ShortcutItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcuts WHERE packageName = :packageName OR packageName = 'global' ORDER BY sortOrder ASC, updatedAt DESC")
    fun getShortcutsForPackage(packageName: String): Flow<List<ShortcutItem>>

    @Query("SELECT * FROM shortcuts WHERE packageName = :packageName OR packageName = 'global' ORDER BY sortOrder ASC, updatedAt DESC")
    fun getAllShortcuts(packageName: String): Flow<List<ShortcutItem>>

    @Query(
        "SELECT * FROM shortcuts WHERE (packageName = :packageName OR packageName = 'global') AND (label LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') ORDER BY sortOrder ASC, updatedAt DESC",
    )
    fun searchShortcuts(
        packageName: String,
        query: String,
    ): Flow<List<ShortcutItem>>

    @Query("SELECT DISTINCT category FROM shortcuts WHERE category != '' ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(item: ShortcutItem): Long

    @Update
    suspend fun updateShortcut(item: ShortcutItem)

    @Delete
    suspend fun deleteShortcut(item: ShortcutItem)

    // ── 云同步用：返回 List 而非 Flow ──

    @Query("SELECT * FROM shortcuts")
    suspend fun getAllShortcutsList(): List<ShortcutItem>
}
