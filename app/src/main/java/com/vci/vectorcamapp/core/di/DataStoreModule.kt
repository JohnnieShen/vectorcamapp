package com.vci.vectorcamapp.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.vci.vectorcamapp.core.data.cache.serializers.DeviceDtoSerializer
import com.vci.vectorcamapp.core.data.cache.serializers.SessionDtoSerializer
import com.vci.vectorcamapp.core.data.dto.DeviceDto
import com.vci.vectorcamapp.core.data.dto.SessionDto
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

private const val CURRENT_SESSION_DATA_STORE_FILE_NAME = "current_session.pb"
private const val DEVICE_DATA_STORE_FILE_NAME = "device.pb"

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideCurrentSessionDataStore(@ApplicationContext context: Context) : DataStore<SessionDto> {
        return DataStoreFactory.create(
            serializer = SessionDtoSerializer,
            produceFile = { context.dataStoreFile(CURRENT_SESSION_DATA_STORE_FILE_NAME)},
            corruptionHandler = ReplaceFileCorruptionHandler { SessionDto() },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        )
    }

    @Provides
    @Singleton
    fun provideDeviceDataStore(@ApplicationContext context: Context) : DataStore<DeviceDto> {
        return DataStoreFactory.create(
            serializer = DeviceDtoSerializer,
            produceFile = { context.dataStoreFile(DEVICE_DATA_STORE_FILE_NAME)},
            corruptionHandler = ReplaceFileCorruptionHandler { DeviceDto() },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        )
    }
}
