package com.mod.os.recents.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mod.os.recents.clipboard.ClipboardMonitor
import com.mod.os.recents.clipboard.ClipboardRepository
import com.mod.os.recents.contract.HostBridge
import com.mod.os.recents.data.RecentsDatabase
import com.mod.os.recents.util.SensitivityDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecentsModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): RecentsDatabase {
        return Room.databaseBuilder(
            context,
            RecentsDatabase::class.java,
            "recents_clipboard_db"
        )
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .fallbackToDestructiveMigrationOnDowngrade()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.execSQL("PRAGMA synchronous = NORMAL;")
                    db.execSQL("PRAGMA cache_size = -20000;")
                    db.execSQL("PRAGMA mmap_size = 268435456;")
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideClipDao(database: RecentsDatabase) = database.clipDao()

    @Provides
    @Singleton
    fun provideClipboardRepository(
        clipDao: com.mod.os.recents.data.ClipDao,
        hostBridge: HostBridge,
        sensitivityDetector: SensitivityDetector
    ): ClipboardRepository {
        return ClipboardRepository(clipDao, hostBridge, sensitivityDetector)
    }

    @Provides
    @Singleton
    fun provideClipboardMonitor(
        hostBridge: HostBridge,
        repository: ClipboardRepository
    ): ClipboardMonitor {
        return ClipboardMonitor(hostBridge, repository)
    }

    @Provides
    @Singleton
    fun provideSensitivityDetector(
        hostBridge: HostBridge
    ): SensitivityDetector {
        return SensitivityDetector(hostBridge)
    }
}
