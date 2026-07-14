package com.nuc.omeletteinputmethod.di

import android.content.Context
import androidx.room.Room
import com.nuc.omeletteinputmethod.data.local.AppDatabase
import com.nuc.omeletteinputmethod.data.local.UserDictionaryDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "omelette_db"
        ).build()
    }

    @Provides
    fun provideUserDictionaryDao(database: AppDatabase): UserDictionaryDao {
        return database.userDictionaryDao()
    }

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    fun provideScheduleDao(database: AppDatabase): ScheduleDao {
        return database.scheduleDao()
    }

    @Provides
    fun provideShortcutDao(database: AppDatabase): ShortcutDao {
        return database.shortcutDao()
    }
}
