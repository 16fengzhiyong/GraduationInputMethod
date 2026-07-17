package com.nuc.omeletteinputmethod.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nuc.omeletteinputmethod.data.model.UserEntity

/**
 * 用户 DAO — 操作本地用户缓存
 *
 * 仅存储当前登录用户，登录时插入/更新，退出时清空。
 */
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserFlow(): kotlinx.coroutines.flow.Flow<UserEntity?>

    @Query("DELETE FROM user_profile")
    suspend fun deleteAll()
}
