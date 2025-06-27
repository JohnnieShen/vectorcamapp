package com.vci.vectorcamapp.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vci.vectorcamapp.core.presentation.util.ObserveAsEvents
import com.vci.vectorcamapp.imaging.presentation.util.toString
import com.vci.vectorcamapp.imaging.presentation.ImagingEvent
import com.vci.vectorcamapp.imaging.presentation.ImagingScreen
import com.vci.vectorcamapp.imaging.presentation.ImagingViewModel
import com.vci.vectorcamapp.landing.presentation.LandingEvent
import com.vci.vectorcamapp.landing.presentation.LandingScreen
import com.vci.vectorcamapp.landing.presentation.LandingViewModel
import androidx.navigation.toRoute
import com.vci.vectorcamapp.animation.presentation.LoadingAnimation
import com.vci.vectorcamapp.complete_session.form.presentation.CompleteSessionFormAction
import com.vci.vectorcamapp.complete_session.form.presentation.CompleteSessionFormScreen
import com.vci.vectorcamapp.complete_session.form.presentation.CompleteSessionFormViewModel
import com.vci.vectorcamapp.complete_session.list.presentation.CompleteSessionListEvent
import com.vci.vectorcamapp.complete_session.list.presentation.CompleteSessionListScreen
import com.vci.vectorcamapp.complete_session.list.presentation.CompleteSessionListViewModel
import com.vci.vectorcamapp.complete_session.specimens.presentation.CompleteSessionSpecimensAction
import com.vci.vectorcamapp.complete_session.specimens.presentation.CompleteSessionSpecimensScreen
import com.vci.vectorcamapp.complete_session.specimens.presentation.CompleteSessionSpecimensViewModel
import com.vci.vectorcamapp.incomplete_session.presentation.IncompleteSessionAction
import com.vci.vectorcamapp.incomplete_session.presentation.IncompleteSessionEvent
import com.vci.vectorcamapp.incomplete_session.presentation.IncompleteSessionScreen
import com.vci.vectorcamapp.incomplete_session.presentation.IncompleteSessionViewModel
import com.vci.vectorcamapp.registration.presentation.RegistrationEvent
import com.vci.vectorcamapp.registration.presentation.RegistrationScreen
import com.vci.vectorcamapp.registration.presentation.RegistrationViewModel
import com.vci.vectorcamapp.surveillance_form.presentation.SurveillanceFormEvent
import com.vci.vectorcamapp.surveillance_form.presentation.SurveillanceFormScreen
import com.vci.vectorcamapp.surveillance_form.presentation.SurveillanceFormViewModel
import java.util.UUID

private const val TAB_SESSION_FORM = 0
private const val TAB_SESSION_SPECIMENS = 1

@Composable
fun NavGraph(startDestination: Destination) {
    val navController = rememberNavController()

    NavHost(
        navController = navController, startDestination = startDestination
    ) {
        composable<Destination.Registration> {
            val viewModel = hiltViewModel<RegistrationViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            ObserveAsEvents(events = viewModel.events) { event ->
                when (event) {
                    RegistrationEvent.NavigateToLandingScreen -> {
                        navController.navigate(Destination.Landing) {
                            popUpTo(Destination.Registration) { inclusive = true }
                        }
                    }
                }
            }

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                 RegistrationScreen(
                    state = state,
                    onAction = viewModel::onAction,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        composable<Destination.Landing> {
            val viewModel = hiltViewModel<LandingViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            ObserveAsEvents(events = viewModel.events) { event ->
                when (event) {
                    LandingEvent.NavigateToNewSurveillanceSessionScreen -> navController.navigate(
                        Destination.SurveillanceForm
                    )

                    LandingEvent.NavigateToIncompleteSessionsScreen -> navController.navigate(
                        Destination.IncompleteSession
                    )

                    LandingEvent.NavigateToCompleteSessionsScreen -> navController.navigate(
                        Destination.CompleteSessionList
                    )
                }
            }

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                when (state.isLoading) {
                    true -> LoadingAnimation(
                        text = "Loading...", modifier = Modifier.padding(innerPadding)
                    )

                    false -> LandingScreen(
                        state = state,
                        onAction = viewModel::onAction,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        composable<Destination.SurveillanceForm> {
            val viewModel = hiltViewModel<SurveillanceFormViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            ObserveAsEvents(events = viewModel.events) { event ->
                when (event) {
                    SurveillanceFormEvent.NavigateToImagingScreen -> navController.navigate(
                        Destination.Imaging
                    )

                    SurveillanceFormEvent.NavigateBackToLandingScreen -> {
                        navController.popBackStack(Destination.Landing, false)
                    }

                    SurveillanceFormEvent.NavigateBackToRegistrationScreen -> {
                        navController.popBackStack(Destination.Registration, false)
                    }
                }
            }

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                when (state.isLoading) {
                    true -> LoadingAnimation(
                        text = "Loading...", modifier = Modifier.padding(innerPadding)
                    )

                    false -> SurveillanceFormScreen(
                        state = state,
                        onAction = viewModel::onAction,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        composable<Destination.Imaging> {
            val context = LocalContext.current
            val viewModel = hiltViewModel<ImagingViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            ObserveAsEvents(events = viewModel.events) { event ->
                when (event) {
                    is ImagingEvent.DisplayImagingError -> {
                        Toast.makeText(context, event.error.toString(context), Toast.LENGTH_LONG)
                            .show()
                    }

                    ImagingEvent.NavigateBackToLandingScreen -> {
                        navController.popBackStack(Destination.Landing, false)
                    }
                }
            }

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                ImagingScreen(
                    state = state,
                    onAction = viewModel::onAction,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
        composable<Destination.IncompleteSession> {
            val viewModel = hiltViewModel<IncompleteSessionViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            ObserveAsEvents(events = viewModel.events) { event ->
                when (event) {
                    IncompleteSessionEvent.NavigateToSurveillanceForm ->
                        navController.navigate(Destination.SurveillanceForm)
                }
            }
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                IncompleteSessionScreen(
                    state = state,
                    onAction = viewModel::onAction,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
        composable<Destination.CompleteSessionList> {
            val viewModel = hiltViewModel<CompleteSessionListViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            ObserveAsEvents(events = viewModel.events) { event ->
                when (event) {
                    is CompleteSessionListEvent.NavigateToCompleteSessionDetails -> {
                        navController.navigate(Destination.CompleteSessionDetails(event.sessionId.toString()))
                    }
                }
            }
            CompleteSessionListScreen(
                state = state,
                onAction = viewModel::onAction
            )
        }
        composable<Destination.CompleteSessionDetails> {
            val args = it.toRoute<Destination.CompleteSessionDetails>()
            val selectedTab = rememberSaveable { mutableIntStateOf(0) }

            val formViewModel = hiltViewModel<CompleteSessionFormViewModel>()
            val formState by formViewModel.state.collectAsStateWithLifecycle()

            val uuid = try {
                UUID.fromString(args.sessionId)
            } catch (e: IllegalArgumentException) {
                return@composable
            }

            LaunchedEffect(uuid) {
                formViewModel.onAction(CompleteSessionFormAction.LoadSession(uuid))
            }

            val specimensViewModel = hiltViewModel<CompleteSessionSpecimensViewModel>()
            val specimensState by specimensViewModel.state.collectAsStateWithLifecycle()
            LaunchedEffect(uuid) {
                specimensViewModel.onAction(CompleteSessionSpecimensAction.LoadSession(uuid))
            }

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {
                    TabRow(selectedTabIndex = selectedTab.intValue) {
                        Tab(
                            selected = selectedTab.intValue == TAB_SESSION_FORM,
                            onClick = { selectedTab.intValue = TAB_SESSION_FORM },
                            text = { Text("Details") }
                        )
                        Tab(
                            selected = selectedTab.intValue == TAB_SESSION_SPECIMENS,
                            onClick = { selectedTab.intValue = TAB_SESSION_SPECIMENS },
                            text = { Text("Specimens") }
                        )
                    }

                    when (selectedTab.intValue) {
                        TAB_SESSION_FORM -> CompleteSessionFormScreen(state = formState)
                        TAB_SESSION_SPECIMENS -> CompleteSessionSpecimensScreen(state = specimensState)
                    }
                }
            }
        }
    }
}