package com.nuc.omeletteinputmethod.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nuc.omeletteinputmethod.data.model.CategoryDictionary
import com.nuc.omeletteinputmethod.data.model.CategoryDictionaryDao
import com.nuc.omeletteinputmethod.data.model.ClipboardItem
import com.nuc.omeletteinputmethod.data.model.InputStat
import com.nuc.omeletteinputmethod.data.model.InputStatDao
import com.nuc.omeletteinputmethod.data.model.Note
import com.nuc.omeletteinputmethod.data.model.ShortcutItem
import com.nuc.omeletteinputmethod.data.model.UserBigram
import com.nuc.omeletteinputmethod.data.model.UserBigramDao
import com.nuc.omeletteinputmethod.data.model.UserDictionary
import com.nuc.omeletteinputmethod.data.model.UserEntity

    @Database(
    entities = [UserDictionary::class, Note::class, ShortcutItem::class, ClipboardItem::class, InputStat::class, UserBigram::class, CategoryDictionary::class, UserEntity::class],
    version = 9,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDictionaryDao(): UserDictionaryDao

    abstract fun noteDao(): NoteDao

    abstract fun shortcutDao(): ShortcutDao

    abstract fun clipboardDao(): ClipboardDao

    abstract fun inputStatDao(): InputStatDao

    abstract fun userBigramDao(): UserBigramDao

    abstract fun categoryDictionaryDao(): CategoryDictionaryDao

    abstract fun userDao(): UserDao

    companion object {
        val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("DROP TABLE IF EXISTS schedules")
                    database.execSQL("ALTER TABLE notes ADD COLUMN title TEXT NOT NULL DEFAULT ''")
                    database.execSQL("ALTER TABLE notes ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE shortcuts ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE shortcuts ADD COLUMN category TEXT NOT NULL DEFAULT ''")
                    database.execSQL("ALTER TABLE shortcuts ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                }
            }

        val MIGRATION_5_6 =
            object : Migration(5, 6) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
                    CREATE TABLE IF NOT EXISTS clipboard_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        content TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        sourcePkg TEXT,
                        pinned INTEGER NOT NULL DEFAULT 0,
                        category TEXT NOT NULL DEFAULT ''
                    )
                """,
                    )
                }
            }

        val MIGRATION_6_7 =
            object : Migration(6, 7) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
                    CREATE TABLE IF NOT EXISTS input_stats (
                        date TEXT NOT NULL PRIMARY KEY,
                        totalChars INTEGER NOT NULL DEFAULT 0,
                        totalWords INTEGER NOT NULL DEFAULT 0,
                        uniqueChars INTEGER NOT NULL DEFAULT 0,
                        sessionCount INTEGER NOT NULL DEFAULT 0,
                        avgSpeed REAL NOT NULL DEFAULT 0
                    )
                """,
                    )
                    database.execSQL(
                        """
                    CREATE TABLE IF NOT EXISTS user_bigram (
                        prevWord TEXT NOT NULL,
                        nextWord TEXT NOT NULL,
                        frequency INTEGER NOT NULL DEFAULT 1,
                        lastUsed INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY (prevWord, nextWord)
                    )
                """,
                    )
                    database.execSQL(
                        """
                    CREATE TABLE IF NOT EXISTS category_dictionary (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        category TEXT NOT NULL,
                        word TEXT NOT NULL,
                        pinyin TEXT NOT NULL,
                        freq INTEGER NOT NULL DEFAULT 0,
                        source TEXT NOT NULL DEFAULT ''
                    )
                """,
                    )
                }
            }

        val MIGRATION_7_8 =
            object : Migration(7, 8) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE user_dictionary ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE user_dictionary ADD COLUMN createdTime INTEGER NOT NULL DEFAULT 0")
                }
            }

        val MIGRATION_8_9 =
            object : Migration(8, 9) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
                    CREATE TABLE IF NOT EXISTS user_profile (
                        id TEXT NOT NULL PRIMARY KEY,
                        phone TEXT NOT NULL,
                        nickname TEXT,
                        avatarUrl TEXT,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        lastSyncAt INTEGER
                    )
                """,
                    )
                }
            }
    }
}