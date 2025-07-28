package com.vci.vectorcamapp.registration.presentation

import android.os.Build
import androidx.lifecycle.viewModelScope
import com.vci.vectorcamapp.core.domain.cache.CurrentSessionCache
import com.vci.vectorcamapp.core.domain.cache.DeviceCache
import com.vci.vectorcamapp.core.domain.model.Device
import com.vci.vectorcamapp.core.domain.model.Program
import com.vci.vectorcamapp.core.domain.repository.ProgramRepository
import com.vci.vectorcamapp.core.presentation.CoreViewModel
import com.vci.vectorcamapp.registration.domain.util.RegistrationError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val deviceCache: DeviceCache,
    private val currentSessionCache: CurrentSessionCache,
    programRepository: ProgramRepository
) : CoreViewModel() {

    private val _allPrograms = programRepository.observeAllPrograms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = combine(
        _allPrograms,
        _state
    ) { allPrograms, state ->
        state.copy(
            programs = allPrograms
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RegistrationState())

    private val _events = Channel<RegistrationEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: RegistrationAction) {
        viewModelScope.launch {
            when (action) {
                is RegistrationAction.SelectProgram -> {
                    _state.update { it.copy(selectedProgram = action.program) }
                }

                RegistrationAction.ConfirmRegistration -> {
                    val selectedProgram = state.value.selectedProgram
                    if (selectedProgram == null) {
                        emitError(RegistrationError.PROGRAM_NOT_FOUND)
                        return@launch
                    }

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