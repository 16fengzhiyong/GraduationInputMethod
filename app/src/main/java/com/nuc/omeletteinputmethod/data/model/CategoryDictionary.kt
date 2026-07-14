package com.nuc.omeletteinputmethod.data.model

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "category_dictionary")
data class CategoryDictionary(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val word: String,
    val pinyin: String,
    val freq: Int = 0,
    val source: String = "",
)

@Dao
interface CategoryDictionaryDao {
    @Query("SELECT * FROM category_dictionary WHERE category = :category ORDER BY freq DESC")
    fun getByCategory(category: String): Flow<List<CategoryDictionary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(words: List<CategoryDictionary>)

    @Query("DELETE FROM category_dictionary WHERE category = :category")
    suspend fun deleteCategory(category: String)

    @Query("SELECT DISTINCT category FROM category_dictionary")
    fun getAllCategories(): Flow<List<String>>
}