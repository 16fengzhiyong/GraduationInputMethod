package com.nuc.omeletteinputmethod.data.repository

import com.nuc.omeletteinputmethod.data.local.ShortcutDao
import com.nuc.omeletteinputmethod.data.model.ShortcutItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShortcutRepository
    @Inject
    constructor(
        private val shortcutDao: ShortcutDao,
    ) {
        fun getShortcuts(packageName: String = "global"): Flow<List<ShortcutItem>> = shortcutDao.getShortcutsForPackage(packageName)

        fun getAllShortcuts(packageName: String): Flow<List<ShortcutItem>> = shortcutDao.getAllShortcuts(packageName)

        fun searchShortcuts(
            query: String,
            packageName: String = "global",
        ): Flow<List<ShortcutItem>> =
            if (query.isBlank()) {
                getShortcuts(packageName)
            } else {
                shortcutDao.searchShortcuts(packageName, query.trim())
            }

        fun getAllCategories(): Flow<List<String>> = shortcutDao.getAllCategories()

        suspend fun addShortcut(item: ShortcutItem): Long = shortcutDao.insertShortcut(item.copy(updatedAt = System.currentTimeMillis()))

        suspend fun updateShortcut(item: ShortcutItem) {
            shortcutDao.updateShortcut(item.copy(updatedAt = System.currentTimeMillis()))
        }

        suspend fun deleteShortcut(item: ShortcutItem) {
            shortcutDao.deleteShortcut(item)
        }
    }
