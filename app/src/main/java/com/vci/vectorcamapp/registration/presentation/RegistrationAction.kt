package com.vci.vectorcamapp.registration.presentation

import com.vci.vectorcamapp.registration.domain.enums.ProgramOption

sealed interface RegistrationAction {
    data class SelectProgram(val option: ProgramOption) : RegistrationAction
    data object ConfirmRegistration : RegistrationAction
}