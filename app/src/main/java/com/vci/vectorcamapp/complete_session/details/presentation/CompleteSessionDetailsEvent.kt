package com.vci.vectorcamapp.complete_session.details.presentation

sealed interface CompleteSessionDetailsEvent {
    data object NavigateBackToCompleteSessionListScreen: CompleteSessionDetailsEvent
}
