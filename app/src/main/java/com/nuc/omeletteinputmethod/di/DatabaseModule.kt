package com.nuc.omeletteinputmethod.di

import android.content.Context
import androidx.room.Room
import com.nuc.omeletteinputmethod.data.local.AppDatabase
import com.nuc.omeletteinputmethod.data.local.ClipboardDao
import com.nuc.omeletteinputmethod.data.local.NoteDao
import com.nuc.omeletteinputmethod.data.local.ShortcutDao
import com.nuc.omeletteinputmethod.data.local.UserDao
import com.nuc.omeletteinputmethod.data.local.UserDictionaryDao
import com.nuc.omeletteinputmethod.data.model.CategoryDictionaryDao
import com.nuc.omeletteinputmethod.data.model.InputStatDao
import com.nuc.omeletteinputmethod.data.model.UserBigramDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room
            .databaseBuilder(
                context,
                AppDatabase::class.java,
                "omelette_db",
            ).addMigrations(
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9
            )
            .build()
    }

    @Provides
    fun provideUserDictionaryDao(database: AppDatabase): UserDictionaryDao = database.userDictionaryDao()

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    @Provides
    fun provideShortcutDao(database: AppDatabase): ShortcutDao = database.shortcutDao()

    @Provides
    fun provideClipboardDao(database: AppDatabase): ClipboardDao = database.clipboardDao()

    @Provides
    fun provideInputStatDao(database: AppDatabase): InputStatDao = database.inputStatDao()

    @Provides
    fun provideUserBigramDao(database: AppDatabase): UserBigramDao = database.userBigramDao()

    @Provides
    fun provideCategoryDictionaryDao(database: AppDatabase): CategoryDictionaryDao = database.categoryDictionaryDao()

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()
}