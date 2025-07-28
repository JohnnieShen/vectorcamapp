package com.vci.vectorcamapp.registration.presentation

import com.vci.vectorcamapp.core.domain.model.Program

data class RegistrationState(
    val programs: List<Program> = emptyList(),
    val selectedProgram: Program? = null,
    val error: String? = null
)