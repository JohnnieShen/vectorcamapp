package com.vci.vectorcamapp.core.di

import com.vci.vectorcamapp.core.data.repository.InferenceResultRepositoryImplementation
import com.vci.vectorcamapp.core.data.repository.ProgramRepositoryImplementation
import com.vci.vectorcamapp.core.data.repository.SessionRepositoryImplementation
import com.vci.vectorcamapp.core.data.repository.SiteRepositoryImplementation
import com.vci.vectorcamapp.core.data.repository.SpecimenRepositoryImplementation
import com.vci.vectorcamapp.core.data.repository.SurveillanceFormRepositoryImplementation
import com.vci.vectorcamapp.core.domain.repository.InferenceResultRepository
import com.vci.vectorcamapp.core.domain.repository.ProgramRepository
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.domain.repository.SiteRepository
import com.vci.vectorcamapp.core.domain.repository.SpecimenRepository
import com.vci.vectorcamapp.core.domain.repository.SurveillanceFormRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        sessionRepositoryImplementation: SessionRepositoryImplementation
    ): SessionRepository

    @Binds
    @Singleton
    abstract fun bindSpecimenRepository(
        specimenRepositoryImplementation: SpecimenRepositoryImplementation
    ): SpecimenRepository

    @Binds
    @Singleton
    abstract fun bindInferenceResultRepository(
        inferenceResultRepositoryImplementation: InferenceResultRepositoryImplementation
    ): InferenceResultRepository

    @Binds
    @Singleton
    abstract fun bindSurveillanceFormRepository(
        surveillanceFormRepositoryImplementation: SurveillanceFormRepositoryImplementation
    ): SurveillanceFormRepository

    @Binds
    @Singleton
    abstract fun bindSiteRepository(
        siteRepositoryImplementation: SiteRepositoryImplementation
    ): SiteRepository

    @Binds
    @Singleton
    abstract fun bindProgramRepository(
        programRepositoryImplementation: ProgramRepositoryImplementation
    ): ProgramRepository
}
