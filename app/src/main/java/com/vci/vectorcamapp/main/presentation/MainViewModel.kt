package com.vci.vectorcamapp.main.presentation

import androidx.lifecycle.viewModelScope
import com.vci.vectorcamapp.core.domain.cache.DeviceCache
import com.vci.vectorcamapp.core.presentation.CoreViewModel
import com.vci.vectorcamapp.main.domain.util.MainError
import com.vci.vectorcamapp.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val deviceCache: DeviceCache
) : CoreViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.onStart {
        determineStartDestination()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainState())
    
    private val _events = Channel<MainEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: MainAction) {
        viewModelScope.launch {
            when (action) {
                MainAction.RequestPermissions -> {
                    _state.update { it.copy(isLoading = true) }
                    _events.send(MainEvent.LaunchPermissionRequest)
                }

                MainAction.OpenAppSettings -> {
                    _events.send(MainEvent.NavigateToAppSettings)
                }

                MainAction.OpenLocationSettings -> {
                    _events.send(MainEvent.NavigateToLocationSettings)
                }

                is MainAction.UpdatePermissionStatus -> {
                    _state.update {
                        it.copy(
                            allGranted = action.allGranted, isLoading = false
                        )
                    }
                }

                is MainAction.UpdateGpsStatus -> {
                    _state.update {
                        it.copy(
                            isGpsEnabled = action.isGpsEnabled
                        )
                    }
                }
            }
        }
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            deviceCache.observeProgramId()
                .catch {
                    emitError(MainError.DEVICE_FETCH_FAILED)
                    _state.update { it.copy(startDestination = Destination.Registration) }
                }
                .onEach { programId ->
                    if (programId != -1 && _state.value.startDestination != Destination.Landing) {
                        _state.update { it.copy(startDestination = Destination.Landing) }
                    } else if (programId == -1 && _state.value.startDestination != Destination.Registration) {
                        _state.update { it.copy(startDestination = Destination.Registration) }
                    }
                }
                .collect { }
        }
    }
}
