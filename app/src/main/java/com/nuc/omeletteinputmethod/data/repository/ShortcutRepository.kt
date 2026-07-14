package com.nuc.omeletteinputmethod.data.repository

import com.nuc.omeletteinputmethod.data.local.ShortcutDao
import com.nuc.omeletteinputmethod.data.model.ShortcutItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShortcutRepository @Inject constructor(
    private val shortcutDao: ShortcutDao
) {
    fun getShortcuts(packageName: String): Flow<List<ShortcutItem>> = 
        shortcutDao.getShortcutsForPackage(packageName)

    suspend fun addShortcut(item: ShortcutItem) = shortcutDao.insertShortcut(item)
    suspend fun deleteShortcut(item: ShortcutItem) = shortcutDao.deleteShortcut(item)
}
