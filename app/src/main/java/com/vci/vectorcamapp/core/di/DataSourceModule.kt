package com.vci.vectorcamapp.core.di

import com.vci.vectorcamapp.core.data.network.api.RemoteSessionDataSource
import com.vci.vectorcamapp.core.domain.network.api.SessionDataSource
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
    abstract fun bindSessionDataSource(
        remoteSessionDataSource: RemoteSessionDataSource
    ): SessionDataSource
}
