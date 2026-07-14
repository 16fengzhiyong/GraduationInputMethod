package com.nuc.omeletteinputmethod.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nuc.omeletteinputmethod.data.model.Note
import com.nuc.omeletteinputmethod.data.model.ScheduleItem
import com.nuc.omeletteinputmethod.data.model.ShortcutItem
import com.nuc.omeletteinputmethod.data.model.UserDictionary

@Database(entities = [UserDictionary::class, Note::class, ScheduleItem::class, ShortcutItem::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDictionaryDao(): UserDictionaryDao
    abstract fun noteDao(): NoteDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun shortcutDao(): ShortcutDao
}
