package com.vci.vectorcamapp.surveillance_form.location.di

import com.vci.vectorcamapp.surveillance_form.location.data.repository.LocationRepositoryImplementation
import com.vci.vectorcamapp.surveillance_form.location.domain.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class LocationModule {

    @Binds
    @ViewModelScoped
    abstract fun bindLocationRepository(
        locationRepositoryImplementation: LocationRepositoryImplementation
    ): LocationRepository
}
