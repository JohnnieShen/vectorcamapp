package com.vci.vectorcamapp.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.vci.vectorcamapp.animation.presentation.LoadingAnimation
import com.vci.vectorcamapp.incomplete_session.presentation.IncompleteSessionScreen
import com.vci.vectorcamapp.incomplete_session.presentation.IncompleteSessionViewModel
import com.vci.vectorcamapp.surveillance_form.presentation.SurveillanceFormEvent
import com.vci.vectorcamapp.surveillance_form.presentation.SurveillanceFormScreen
import com.vci.vectorcamapp.surveillance_form.presentation.SurveillanceFormViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController, startDestination = Destination.Landing
    ) {
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
            // TODO: UPON PRESSING THE BACK BUTTON, SHOULD CLEAR THE CACHE!
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

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                IncompleteSessionScreen(
                    state = state,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
