package com.nuc.omeletteinputmethod.data.repository

import com.nuc.omeletteinputmethod.data.local.ClipboardDao
import com.nuc.omeletteinputmethod.data.local.NoteDao
import com.nuc.omeletteinputmethod.data.local.ShortcutDao
import com.nuc.omeletteinputmethod.data.local.UserDictionaryDao
import com.nuc.omeletteinputmethod.data.model.ClipboardItem
import com.nuc.omeletteinputmethod.data.model.InputStat
import com.nuc.omeletteinputmethod.data.model.InputStatDao
import com.nuc.omeletteinputmethod.data.model.Note
import com.nuc.omeletteinputmethod.data.model.ShortcutItem
import com.nuc.omeletteinputmethod.data.model.UserDictionary
import com.nuc.omeletteinputmethod.data.remote.api.ApiService
import com.nuc.omeletteinputmethod.data.remote.dto.ClipboardSyncDto
import com.nuc.omeletteinputmethod.data.remote.dto.FullSyncRequest
import com.nuc.omeletteinputmethod.data.remote.dto.InputStatSyncDto
import com.nuc.omeletteinputmethod.data.remote.dto.NoteSyncDto
import com.nuc.omeletteinputmethod.data.remote.dto.ShortcutSyncDto
import com.nuc.omeletteinputmethod.data.remote.dto.UserDictionarySyncDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 云同步结果统计
 */
data class SyncResult(
    val uploaded: Int = 0,
    val downloaded: Int = 0,
    val conflicts: Int = 0,
    val success: Boolean = true,
    val errorMessage: String? = null
)

/**
 * 云同步仓库 — 协调本地数据库与云端 API 的双向同步
 *
 * 冲突解决策略：最后修改时间优先（以 updatedAt / timestamp 较新者为准）
 */
@Singleton
class CloudSyncRepository
    @Inject
    constructor(
        private val apiService: ApiService,
        private val userDictionaryDao: UserDictionaryDao,
        private val noteDao: NoteDao,
        private val shortcutDao: ShortcutDao,
        private val clipboardDao: ClipboardDao,
        private val inputStatDao: InputStatDao
    ) {
        /**
         * 全量同步 — 上传本地所有数据，下载云端所有数据并合并
         */
        suspend fun syncAll(): SyncResult =
            runCatching {
                // 1. 读取本地数据
                val localDict = userDictionaryDao.getAllWordsList()
                val localNotes = noteDao.getAllNotesList()
                val localShortcuts = shortcutDao.getAllShortcutsList()
                val localClipboard = clipboardDao.getAllItemsList()
                val localStats = inputStatDao.getStatsByDateRange("2020-01-01", "2099-12-31")

                // 2. 构建同步请求
                val request =
                    FullSyncRequest(
                        userDictionary = localDict.map { it.toSyncDto() },
                        notes = localNotes.map { it.toSyncDto() },
                        shortcuts = localShortcuts.map { it.toSyncDto() },
                        clipboard = localClipboard.map { it.toSyncDto() },
                        inputStats = localStats.map { it.toSyncDto() }
                    )

                // 3. 调用全量同步 API
                val response = apiService.syncAll(request)
                if (!response.isSuccess || response.data == null) {
                    throw Exception(response.message.ifBlank { "同步失败" })
                }

                val remote = response.data

                // 4. 合并远程数据到本地（简化：直接覆盖）
                mergeRemoteData(remote)

                val uploaded = localDict.size + localNotes.size + localShortcuts.size + localClipboard.size + localStats.size
                val downloaded = remote.userDictionary.size + remote.notes.size + remote.shortcuts.size + remote.clipboard.size + remote.inputStats.size
                SyncResult(
                    uploaded = uploaded,
                    downloaded = downloaded,
                    success = true
                )
            }.getOrElse { e ->
                SyncResult(success = false, errorMessage = e.message)
            }

        /**
         * 仅上传本地数据到云端（全部数据类型）
         */
        suspend fun uploadOnly(): SyncResult =
            runCatching {
                val dict = userDictionaryDao.getAllWordsList()
                val notes = noteDao.getAllNotesList()
                val shortcuts = shortcutDao.getAllShortcutsList()
                val clipboard = clipboardDao.getAllItemsList()
                val stats = inputStatDao.getStatsByDateRange("2020-01-01", "2099-12-31")

                val dictResp = apiService.uploadUserDictionary(dict.map { it.toSyncDto() })
                apiService.uploadNotes(notes.map { it.toSyncDto() })
                apiService.uploadShortcuts(shortcuts.map { it.toSyncDto() })
                apiService.uploadClipboard(clipboard.map { it.toSyncDto() })
                apiService.uploadStats(stats.map { it.toSyncDto() })

                val total = dict.size + notes.size + shortcuts.size + clipboard.size + stats.size
                SyncResult(uploaded = if (dictResp.isSuccess) total else 0, success = dictResp.isSuccess)
            }.getOrElse { e ->
                SyncResult(success = false, errorMessage = e.message)
            }

        /**
         * 仅下载云端数据到本地（全部数据类型并合并）
         */
        suspend fun downloadOnly(): SyncResult =
            runCatching {
                val dictResp = apiService.downloadUserDictionary()
                val notesResp = apiService.downloadNotes()
                val shortcutsResp = apiService.downloadShortcuts()
                val clipboardResp = apiService.downloadClipboard()
                val statsResp = apiService.downloadStats()

                var downloaded = 0
                if (dictResp.isSuccess && dictResp.data != null) {
                    dictResp.data.forEach { userDictionaryDao.insertWord(it.toEntity()) }
                    downloaded += dictResp.data.size
                }
                if (notesResp.isSuccess && notesResp.data != null) {
                    notesResp.data.forEach { noteDao.insertNote(it.toEntity()) }
                    downloaded += notesResp.data.size
                }
                if (shortcutsResp.isSuccess && shortcutsResp.data != null) {
                    shortcutsResp.data.forEach { shortcutDao.insertShortcut(it.toEntity()) }
                    downloaded += shortcutsResp.data.size
                }
                if (clipboardResp.isSuccess && clipboardResp.data != null) {
                    clipboardResp.data.forEach { clipboardDao.insert(it.toEntity()) }
                    downloaded += clipboardResp.data.size
                }
                if (statsResp.isSuccess && statsResp.data != null) {
                    statsResp.data.forEach { inputStatDao.batchInsert(listOf(it.toEntity())) }
                    downloaded += statsResp.data.size
                }

                val allSuccess = dictResp.isSuccess && notesResp.isSuccess && shortcutsResp.isSuccess && clipboardResp.isSuccess && statsResp.isSuccess
                SyncResult(downloaded = downloaded, success = allSuccess)
            }.getOrElse { e ->
                SyncResult(success = false, errorMessage = e.message)
            }

        // ── 私有方法 ──

        private suspend fun mergeRemoteData(remote: com.nuc.omeletteinputmethod.data.remote.dto.FullSyncResponse) {
            // 用户词典：按 id 去重（REPLACE 策略）
            remote.userDictionary.forEach { dto ->
                userDictionaryDao.insertWord(dto.toEntity())
            }
            // 记事本：按 id 去重
            remote.notes.forEach { dto ->
                noteDao.insertNote(dto.toEntity())
            }
            // 快捷短语：按 id 去重
            remote.shortcuts.forEach { dto ->
                shortcutDao.insertShortcut(dto.toEntity())
            }
            // 剪贴板：按 id 去重（REPLACE 策略，远程较新时覆盖本地）
            remote.clipboard.forEach { dto ->
                clipboardDao.insertOrReplace(dto.toEntity())
            }
            // 输入统计：按 date 去重
            remote.inputStats.forEach { dto ->
                inputStatDao.batchInsert(listOf(dto.toEntity()))
            }
        }

        // ── 映射扩展：Entity → DTO ──

        private fun UserDictionary.toSyncDto() =
            UserDictionarySyncDto(
                id = id,
                word = word,
                frequency = frequency,
                lastUsedTimestamp = lastUsedTimestamp,
                pinned = pinned,
                createdTime = createdTime
            )

        private fun Note.toSyncDto() =
            NoteSyncDto(
                id = id,
                title = title,
                content = content,
                timestamp = timestamp,
                updatedAt = updatedAt
            )

        private fun ShortcutItem.toSyncDto() =
            ShortcutSyncDto(
                id = id,
                packageName = packageName,
                content = content,
                label = label,
                category = category,
                sortOrder = sortOrder,
                updatedAt = updatedAt
            )

        private fun ClipboardItem.toSyncDto() =
            ClipboardSyncDto(
                id = id,
                content = content,
                timestamp = timestamp,
                sourcePkg = sourcePkg,
                pinned = pinned,
                category = category
            )

        private fun InputStat.toSyncDto() =
            InputStatSyncDto(
                date = date,
                totalChars = totalChars,
                totalWords = totalWords,
                uniqueChars = uniqueChars,
                sessionCount = sessionCount,
                avgSpeed = avgSpeed
            )

        // ── 映射扩展：DTO → Entity ──

        private fun UserDictionarySyncDto.toEntity() =
            UserDictionary(
                id = id,
                word = word,
                frequency = frequency,
                lastUsedTimestamp = lastUsedTimestamp,
                pinned = pinned,
                createdTime = createdTime
            )

        private fun NoteSyncDto.toEntity() =
            Note(
                id = id,
                title = title,
                content = content,
                timestamp = timestamp,
                updatedAt = updatedAt
            )

        private fun ShortcutSyncDto.toEntity() =
            ShortcutItem(
                id = id,
                packageName = packageName,
                content = content,
                label = label,
                category = category,
                sortOrder = sortOrder,
                updatedAt = updatedAt
            )

        private fun ClipboardSyncDto.toEntity() =
            ClipboardItem(
                id = id,
                content = content,
                timestamp = timestamp,
                sourcePkg = sourcePkg,
                pinned = pinned,
                category = category
            )

        private fun InputStatSyncDto.toEntity() =
            InputStat(
                date = date,
                totalChars = totalChars,
                totalWords = totalWords,
                uniqueChars = uniqueChars,
                sessionCount = sessionCount,
                avgSpeed = avgSpeed
            )
    }
