package com.nuc.omeletteinputmethod.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
            class DictDeployer
                @Inject
                constructor(
                    @ApplicationContext private val context: Context,
                ) {
                    companion object {
                        private const val ASSET_DICT_DB = "dict.db"
                        private const val ASSET_BIGRAM_DB = "bigram.db"
                        private const val DICT_DB_FILENAME = "dict.db"
                        private const val BIGRAM_DB_FILENAME = "bigram.db"
                        private const val CELLDICT_ASSET_DIR = "celldict"
                        private const val CELLDICT_FILES_DIR = "celldict"
                    }

                    val dictDbFile: File get() = File(context.filesDir, DICT_DB_FILENAME)
                    val bigramDbFile: File get() = File(context.filesDir, BIGRAM_DB_FILENAME)
                    val celldictDir: File get() = File(context.filesDir, CELLDICT_FILES_DIR)
                    val filesDirPath: String get() = context.filesDir.absolutePath

                    fun isDeployed(): Boolean =
                        dictDbFile.exists() && dictDbFile.length() > 0 &&
                            bigramDbFile.exists() && bigramDbFile.length() > 0

                    fun deployIfNeeded(): Boolean {
                        if (dictDbFile.exists() && dictDbFile.length() > 0) {
                            return true
                        }

                        var success = true

                        if (!copyAsset(ASSET_DICT_DB, dictDbFile)) success = false
                        if (!copyAsset(ASSET_BIGRAM_DB, bigramDbFile)) success = false

                        deployCategoryDicts()

                        return success
                    }

                    fun deployCategoryDicts() {
                        celldictDir.mkdirs()
                        val knownDbs = listOf("medical.db", "it.db")
                        for (dbName in knownDbs) {
                            val assetPath = "$CELLDICT_ASSET_DIR/$dbName"
                            val destFile = File(celldictDir, dbName)
                            if (!destFile.exists() || destFile.length() == 0L) {
                                try {
                                    copyAsset(assetPath, destFile)
                                } catch (_: Exception) {
                                    android.util.Log.w("DictDeployer", "No celldict asset: $assetPath")
                                }
                            }
                        }
                    }

                    private fun copyAsset(
                        assetName: String,
                        destFile: File,
                    ): Boolean {
                        if (destFile.exists() && destFile.length() > 0) return true

                        return try {
                            val inputStream = context.assets.open(assetName)
                            val outputStream = FileOutputStream(destFile)
                            val buffer = ByteArray(32768)
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                            }
                            outputStream.close()
                            inputStream.close()
                            true
                        } catch (e: Exception) {
                            android.util.Log.e("DictDeployer", "Failed to deploy $assetName", e)
                            false
                        }
                    }
                }
