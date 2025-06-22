package com.vci.vectorcamapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.vci.vectorcamapp.animation.presentation.LoadingAnimation
import com.vci.vectorcamapp.navigation.NavGraph
import com.vci.vectorcamapp.main.presentation.MainAction
import com.vci.vectorcamapp.main.presentation.MainEvent
import com.vci.vectorcamapp.main.presentation.components.PermissionAndGpsPrompt
import com.vci.vectorcamapp.main.presentation.MainViewModel
import com.vci.vectorcamapp.ui.theme.VectorcamappTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val permissionsRequired = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    private val permissionLauncher = registerForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        viewModel.onAction(MainAction.UpdatePermissionStatus(allGranted))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VectorcamappTheme {
                LaunchedEffect(viewModel) {
                    viewModel.events.collect { event ->
                        when (event) {
                            MainEvent.LaunchPermissionRequest -> {
                                permissionLauncher.launch(permissionsRequired)
                            }

                            MainEvent.NavigateToAppSettings -> openAppSettings()
                            MainEvent.NavigateToLocationSettings -> openLocationSettings()
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    checkAndUpdatePermissionStatus()
                    checkAndUpdateGpsStatus()
                    viewModel.onAction(MainAction.RequestPermissions)
                }

                val state by viewModel.state.collectAsState()

                when (state.allGranted && state.isGpsEnabled) {
                    true -> {
                        when (val startDestination = state.startDestination) {
                            null -> LoadingAnimation(text = "Initializing…")
                            else -> NavGraph(startDestination = startDestination)
                        }
                    }
                    false -> Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        PermissionAndGpsPrompt(
                            state = state,
                            onAction = viewModel::onAction,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndUpdatePermissionStatus()
        checkAndUpdateGpsStatus()
    }

    private fun checkAndUpdatePermissionStatus() {
        val allGranted = permissionsRequired.all { permission ->
            checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }

        viewModel.onAction(MainAction.UpdatePermissionStatus(allGranted))
    }

    private fun checkAndUpdateGpsStatus() {
        val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        viewModel.onAction(MainAction.UpdateGpsStatus(isGpsEnabled))
    }

    private fun openAppSettings() {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ).also(::startActivity)
    }

    private fun openLocationSettings() {
        Intent(
            Settings.ACTION_LOCATION_SOURCE_SETTINGS
        ).also(::startActivity)
    }
}
