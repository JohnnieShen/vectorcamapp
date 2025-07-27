package com.vci.vectorcamapp.landing.presentation

sealed interface LandingAction {
    data object StartNewSurveillanceSession : LandingAction
    data object StartNewDataCollectionSession : LandingAction
    data object ViewIncompleteSessions : LandingAction
    data object ViewCompleteSessions : LandingAction
    data object ResumeSession : LandingAction
    data object DismissResumePrompt : LandingAction
}
