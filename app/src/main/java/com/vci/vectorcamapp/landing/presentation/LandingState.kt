package com.vci.vectorcamapp.landing.presentation

import com.vci.vectorcamapp.core.domain.model.Program

data class LandingState(
    val isLoading: Boolean = false,
    val enrolledProgram: Program = Program(
        id = -1,
        name = "",
        country = ""
    ),
    val showResumeDialog: Boolean = false,
    val incompleteSessionsCount: Int = 0
)
