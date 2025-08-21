package com.vci.vectorcamapp.core.di

import com.vci.vectorcamapp.core.data.network.api.RemoteDeviceDataSource
import com.vci.vectorcamapp.core.data.network.api.RemoteSessionDataSource
import com.vci.vectorcamapp.core.data.network.api.RemoteSpecimenDataSource
import com.vci.vectorcamapp.core.data.network.api.RemoteSpecimenImageDataSource
import com.vci.vectorcamapp.core.data.network.api.RemoteSurveillanceFormDataSource
import com.vci.vectorcamapp.core.domain.network.api.DeviceDataSource
import com.vci.vectorcamapp.core.domain.network.api.SessionDataSource
import com.vci.vectorcamapp.core.domain.network.api.SpecimenDataSource
import com.vci.vectorcamapp.core.domain.network.api.SpecimenImageDataSource
import com.vci.vectorcamapp.core.domain.network.api.SurveillanceFormDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindDeviceDataSource(
        remoteDeviceDataSource: RemoteDeviceDataSource
    ): DeviceDataSource

    @Binds
    @Singleton
    abstract fun bindSessionDataSource(
        remoteSessionDataSource: RemoteSessionDataSource
    ): SessionDataSource

    @Binds
    @Singleton
    abstract fun bindSurveillanceFormDataSource(
        remoteSurveillanceFormDataSource: RemoteSurveillanceFormDataSource
    ): SurveillanceFormDataSource

    @Binds
    @Singleton
    abstract fun bindSpecimenDataSource(
        remoteSpecimenDataSource: RemoteSpecimenDataSource
    ): SpecimenDataSource

    @Binds
    @Singleton
    abstract fun bindSpecimenImageDataSource(
        remoteSpecimenImageDataSource: RemoteSpecimenImageDataSource
    ): SpecimenImageDataSource
}
