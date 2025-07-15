package com.vci.vectorcamapp.surveillance_form.di

import com.vci.vectorcamapp.surveillance_form.data.repository.LocationRepositoryImplementation
import com.vci.vectorcamapp.surveillance_form.domain.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class SessionFormRepositoryModule {

    @Binds
    @ViewModelScoped
    abstract fun bindLocationRepository(
        locationRepositoryImplementation: LocationRepositoryImplementation
    ): LocationRepository
}
