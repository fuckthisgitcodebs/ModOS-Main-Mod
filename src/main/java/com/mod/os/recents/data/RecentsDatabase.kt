package com.mod.os.recents.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Database(
    entities = [ClipEntry::class, ClipFts::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(InstantConverter::class, ClipTypeConverter::class)
abstract class RecentsDatabase : RoomDatabase() {

    abstract fun clipDao(): ClipDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Placeholder - add actual migration when bumping version
            }
        }
    }
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
