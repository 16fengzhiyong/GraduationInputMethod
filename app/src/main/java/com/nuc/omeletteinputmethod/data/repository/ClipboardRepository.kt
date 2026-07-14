package com.nuc.omeletteinputmethod.data.repository

import android.content.ClipboardManager
import android.content.Context
import com.nuc.omeletteinputmethod.data.local.ClipboardDao
import com.nuc.omeletteinputmethod.data.model.ClipboardItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardRepository
    @Inject
    constructor(
        private val clipboardDao: ClipboardDao,
        @ApplicationContext private val appContext: Context,
    ) {
        companion object {
            private const val DEFAULT_MAX_ITEMS = 500
        }

        private val prefs = appContext.getSharedPreferences("omelette_clipboard_prefs", Context.MODE_PRIVATE)
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private var listenerRegistered = false
        private val clipboardManager: ClipboardManager?
            get() =
                try {
                    appContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                } catch (e: Exception) {
                    null
                }

        fun getAllPinnedFirst(): Flow<List<ClipboardItem>> = clipboardDao.getAllPinnedFirst()

        fun searchContent(query: String): Flow<List<ClipboardItem>> =
            if (query.isBlank()) {
                clipboardDao.getAllPinnedFirst()
            } else {
                clipboardDao.searchContent(query.trim())
            }

        fun getRecent(limit: Int): Flow<List<ClipboardItem>> = clipboardDao.getRecent(limit)

        suspend fun insertItem(item: ClipboardItem) {
            val existing = item.content.trim()
            if (existing.isEmpty()) return
            val latestContent = clipboardDao.getLatestContent()
            if (latestContent != null && latestContent.trim() == existing) return
            clipboardDao.insert(item.copy(content = existing))
            val maxItems = prefs.getInt("clipboard_max_items", DEFAULT_MAX_ITEMS)
            clipboardDao.clearOldest(maxItems)
        }

        suspend fun pinItem(id: Long) {
            clipboardDao.pinItem(id)
        }

        suspend fun unpinItem(id: Long) {
            clipboardDao.unpinItem(id)
        }

        suspend fun deleteItem(item: ClipboardItem) {
            clipboardDao.delete(item)
        }

        suspend fun deleteAll() {
            clipboardDao.deleteAll()
        }

        fun getMaxItems(): Int = prefs.getInt("clipboard_max_items", DEFAULT_MAX_ITEMS)

        fun setMaxItems(limit: Int) {
            prefs.edit().putInt("clipboard_max_items", limit.coerceIn(50, 2000)).apply()
        }

        fun startMonitoring() {
            val cm = clipboardManager ?: return
            if (listenerRegistered) return
            listenerRegistered = true
            try {
                cm.addPrimaryClipChangedListener {
                    val clip = cm.primaryClip ?: return@addPrimaryClipChangedListener
                    if (clip.itemCount == 0) return@addPrimaryClipChangedListener
                    val text = clip.getItemAt(0).coerceToText(appContext).toString()
                    if (text.isBlank()) return@addPrimaryClipChangedListener
                    scope.launch {
                        insertItem(ClipboardItem(content = text))
                    }
                }
            } catch (e: Exception) {
                listenerRegistered = false
            }
        }
    }
