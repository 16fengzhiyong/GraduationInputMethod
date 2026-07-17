package com.nuc.omeletteinputmethod.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nuc.omeletteinputmethod.data.model.ClipboardItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_history ORDER BY pinned DESC, timestamp DESC")
    fun getAllPinnedFirst(): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_history WHERE content LIKE '%' || :query || '%' ORDER BY pinned DESC, timestamp DESC")
    fun searchContent(query: String): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_history ORDER BY pinned DESC, timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<ClipboardItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ClipboardItem): Long

    /** 云同步用：REPLACE 策略插入（远程较新时覆盖本地） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(item: ClipboardItem)

    @Query("UPDATE clipboard_history SET pinned = 1 WHERE id = :id")
    suspend fun pinItem(id: Long)

    @Query("UPDATE clipboard_history SET pinned = 0 WHERE id = :id")
    suspend fun unpinItem(id: Long)

    @Delete
    suspend fun delete(item: ClipboardItem)

    @Query("DELETE FROM clipboard_history")
    suspend fun deleteAll()

    @Query(
        """
        DELETE FROM clipboard_history WHERE id IN (
            SELECT id FROM clipboard_history WHERE pinned = 0 ORDER BY timestamp ASC LIMIT (
                SELECT MAX(0, COUNT(*) - :keepN) FROM clipboard_history
            )
        )
    """,
    )
    suspend fun clearOldest(keepN: Int)

    @Query("SELECT content FROM clipboard_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestContent(): String?

    // ── 云同步用：返回 List 而非 Flow ──

    @Query("SELECT * FROM clipboard_history")
    suspend fun getAllItemsList(): List<ClipboardItem>
}
