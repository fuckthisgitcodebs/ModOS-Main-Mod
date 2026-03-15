package com.mod.os.recents.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.Instant

// FIX: Removed dead MIGRATION_1_2 companion object. It was never passed to
// addMigrations() in RecentsModule, was never triggered, and its presence
// implied a schema change that doesn't exist. If a real migration is needed,
// bump version here AND wire it into the builder in RecentsModule at that time.
@Database(
    entities = [ClipEntry::class, ClipFts::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(InstantConverter::class, ClipTypeConverter::class)
abstract class RecentsDatabase : RoomDatabase() {
    abstract fun clipDao(): ClipDao
}

class InstantConverter {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }
}

class ClipTypeConverter {
    @TypeConverter
    fun fromClipType(type: ClipType): String = type.name

    @TypeConverter
    fun toClipType(name: String): ClipType = ClipType.valueOf(name)
}
