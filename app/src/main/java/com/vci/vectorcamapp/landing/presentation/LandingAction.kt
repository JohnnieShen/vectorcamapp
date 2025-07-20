package com.vci.vectorcamapp.landing.presentation

sealed interface LandingAction {
    data object StartNewSession : LandingAction
    data object ViewIncompleteSessions : LandingAction
    data object ViewCompleteSessions : LandingAction
    data object ResumeSession : LandingAction
    data object DismissResumePrompt : LandingAction
}
