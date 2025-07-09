package com.vci.vectorcamapp.complete_session.details.presentation

import com.vci.vectorcamapp.complete_session.details.presentation.enums.CompleteSessionDetailsTab

sealed interface CompleteSessionDetailsAction {
    data class ChangeSelectedTab(val selectedTab: CompleteSessionDetailsTab) : CompleteSessionDetailsAction
}
