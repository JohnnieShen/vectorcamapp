package com.vci.vectorcamapp.registration.presentation

import com.vci.vectorcamapp.core.domain.model.Program

data class RegistrationState(
    val isLoading: Boolean = false,
    val programs: List<Program> = emptyList(),
    val selectedProgramName: String = "",
    val error: String? = null
)