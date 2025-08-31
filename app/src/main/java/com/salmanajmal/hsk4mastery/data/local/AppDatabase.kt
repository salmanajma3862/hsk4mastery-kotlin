package com.salmanajmal.hsk4mastery.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.salmanajmal.hsk4mastery.data.local.dao.WordDao
import com.salmanajmal.hsk4mastery.data.local.model.UserWordProgressEntity
import com.salmanajmal.hsk4mastery.data.local.model.WordEntity

@Database(
    entities = [WordEntity::class, UserWordProgressEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
}

