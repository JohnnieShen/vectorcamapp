package com.vci.vectorcamapp.intake.di

import com.vci.vectorcamapp.intake.data.repository.LocationRepositoryImplementation
import com.vci.vectorcamapp.intake.domain.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class IntakeRepositoryModule {

    @Binds
    @ViewModelScoped
    abstract fun bindLocationRepository(
        locationRepositoryImplementation: LocationRepositoryImplementation
    ): LocationRepository
}
