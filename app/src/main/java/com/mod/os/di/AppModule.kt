package com.mod.os.di

import com.mod.os.HostBridgeImpl
import com.mod.os.recents.contract.HostBridge
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindHostBridge(impl: HostBridgeImpl): HostBridge
}
