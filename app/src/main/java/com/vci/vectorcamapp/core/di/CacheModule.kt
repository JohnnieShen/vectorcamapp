package com.vci.vectorcamapp.core.di

import com.vci.vectorcamapp.core.data.cache.CurrentSessionCacheImplementation
import com.vci.vectorcamapp.core.data.cache.DeviceCacheImplementation
import com.vci.vectorcamapp.core.domain.cache.CurrentSessionCache
import com.vci.vectorcamapp.core.domain.cache.DeviceCache
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CacheModule {

    @Binds
    @Singleton
    abstract fun bindCurrentSessionCache(
        currentSessionCacheImplementation: CurrentSessionCacheImplementation
    ): CurrentSessionCache

    @Binds
    @Singleton
    abstract fun bindDeviceCache(
        deviceCacheImplementation: DeviceCacheImplementation
    ): DeviceCache
}
