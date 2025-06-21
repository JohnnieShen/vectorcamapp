package com.vci.vectorcamapp.core.di

import android.os.Build
import android.util.Log
import com.vci.vectorcamapp.BuildConfig
import com.vci.vectorcamapp.core.data.cache.CurrentSessionCacheImplementation
import com.vci.vectorcamapp.core.data.cache.DeviceCacheImplementation
import com.vci.vectorcamapp.core.domain.cache.CurrentSessionCache
import com.vci.vectorcamapp.core.domain.cache.DeviceCache
import com.vci.vectorcamapp.core.domain.model.Device
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

//@Module
//@InstallIn(SingletonComponent::class)
//abstract class CacheModule {
//
//    @Binds
//    @Singleton
//    abstract fun bindCurrentSessionCache(
//        currentSessionCacheImplementation: CurrentSessionCacheImplementation
//    ) : CurrentSessionCache
//
//    @Binds
//    @Singleton
//    abstract fun bindDeviceCache(
//        deviceCacheImplementation: DeviceCacheImplementation
//    ) : DeviceCache
//}

// TODO: ⚠️ For development only: wipe database on every launch
@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    @Provides
    @Singleton
    fun provideCurrentSessionCache(
        impl: CurrentSessionCacheImplementation
    ): CurrentSessionCache {
        if (BuildConfig.DEBUG) {
            CoroutineScope(Dispatchers.IO).launch {
                Log.w("CacheModule", "Clearing current session cache (DEBUG only)")
//                impl.clearSession()
            }
        }
        return impl
    }

    @Provides
    @Singleton
    fun provideDeviceCache(
        impl: DeviceCacheImplementation
    ): DeviceCache {
        if (BuildConfig.DEBUG) {
            CoroutineScope(Dispatchers.IO).launch {
                impl.saveDevice(
                    Device(
                        id = 1,
                        model = Build.MANUFACTURER + " " + Build.MODEL,
                        registeredAt = System.currentTimeMillis(),
                    ), programId = 2
                )
            }
        }
        return impl
    }
}
