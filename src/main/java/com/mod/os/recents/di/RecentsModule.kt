package com.mod.os.recents.di

import android.content.Context
import androidx.room.Room
import com.mod.os.recents.clipboard.ClipboardMonitor
import com.mod.os.recents.clipboard.ClipboardRepository
import com.mod.os.recents.contract.HostBridge
import com.mod.os.recents.data.RecentsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
            .fallbackToDestructiveMigration() // for dev only; use migrations in prod
            .build()
    }

    @Provides
    @Singleton
    fun provideClipDao(database: RecentsDatabase) = database.clipDao()

    @Provides
    @Singleton
    fun provideClipboardRepository(
        clipDao: ClipDao,
        hostBridge: HostBridge
    ): ClipboardRepository {
        return ClipboardRepository(clipDao, hostBridge)
    }

    @Provides
    @Singleton
    fun provideClipboardMonitor(
        hostBridge: HostBridge,
        repository: ClipboardRepository
    ): ClipboardMonitor {
        return ClipboardMonitor(hostBridge, repository)
    }

    // HostBridge is provided by the host app — module expects it injected from host
    // If host uses Hilt too, consider @EntryPoint or component dependency
}
