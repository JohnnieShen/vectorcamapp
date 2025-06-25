package com.vci.vectorcamapp.registration.presentation

import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vci.vectorcamapp.core.domain.cache.CurrentSessionCache
import com.vci.vectorcamapp.core.domain.cache.DeviceCache
import com.vci.vectorcamapp.core.domain.model.Device
import com.vci.vectorcamapp.core.domain.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val deviceCache: DeviceCache,
    private val currentSessionCache: CurrentSessionCache
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = _state.onStart {
        loadAllPrograms()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RegistrationState())

    private val _events = Channel<RegistrationEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: RegistrationAction) {
        viewModelScope.launch {
            when (action) {
                is RegistrationAction.SelectProgram -> {
                    _state.update { it.copy(selectedProgramName = action.option.label) }
                }

                RegistrationAction.ConfirmRegistration -> {
                    val selectedProgram = _state.value.programs.find { it.name == _state.value.selectedProgramName }
                    if (selectedProgram == null) {
                        Log.e(
                            "REGISTRATION_ERROR",
                            "Program not found for name ${_state.value.selectedProgramName}"
                        )
                        return@launch
                    }

                    viewModelScope.launch {
                        val device = Device(
                            id = -1,
                            model = "${Build.MANUFACTURER} ${Build.MODEL}",
                            registeredAt = System.currentTimeMillis(),
                            submittedAt = null,
                        )
                        deviceCache.saveDevice(device, selectedProgram.id)
                        currentSessionCache.clearSession()
                        _events.send(RegistrationEvent.NavigateToLandingScreen)
                    }
                }
            }
        }
    }

    private fun loadAllPrograms() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true
                )
            }
            val programs = programRepository.getAllPrograms()
            _state.update {
                it.copy(
                    programs = programs,
                    isLoading = false
                )
            }
        }
    }
}