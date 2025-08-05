package com.vci.vectorcamapp.registration.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vci.vectorcamapp.core.domain.cache.CurrentSessionCache
import com.vci.vectorcamapp.core.domain.cache.DeviceCache
import com.vci.vectorcamapp.core.domain.model.Device
import com.vci.vectorcamapp.core.domain.model.Program
import com.vci.vectorcamapp.core.domain.repository.ProgramRepository
import com.vci.vectorcamapp.core.presentation.util.error.ErrorMessageBus
import com.vci.vectorcamapp.core.rules.MainDispatcherRule
import com.vci.vectorcamapp.registration.domain.util.RegistrationError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var deviceCache: DeviceCache
    private lateinit var sessionCache: CurrentSessionCache
    private lateinit var programRepository: ProgramRepository
    private lateinit var viewModel: RegistrationViewModel
    private lateinit var programsFlow: MutableStateFlow<List<Program>>

    private val testPrograms = listOf(
        Program(id = 1, name = "Program 1", country = "Country 1"),
        Program(id = 2, name = "Program 2", country = "Country 2")
    )

    @Before
    fun setUp() {
        ErrorMessageBus.clearLastMessage()
        mockkObject(ErrorMessageBus)
        coEvery { ErrorMessageBus.emit(any(), any()) } returns Unit

        deviceCache = mockk(relaxed = true)
        sessionCache = mockk(relaxed = true)
        programRepository = mockk()

        programsFlow = MutableStateFlow(testPrograms)
        every { programRepository.observeAllPrograms() } returns programsFlow

        viewModel = RegistrationViewModel(deviceCache, sessionCache, programRepository)
    }

    @After
    fun tearDown() {
        unmockkObject(ErrorMessageBus)
    }

    private fun selectProgram(program: Program) = runTest {
        viewModel.state.test {
            awaitItem()
            awaitItem() 
            viewModel.onAction(RegistrationAction.SelectProgram(program))
            advanceUntilIdle()
            val selectedState = awaitItem()
            assertThat(selectedState.selectedProgram).isEqualTo(program)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========================================
    // STATE MANAGEMENT TESTS
    // Tests for initial state and state changes
    // ========================================

    @Test
    fun `S01 initial state has empty programs and no selection`() = runTest {
        advanceUntilIdle()

        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.programs).isEmpty()
            assertThat(initialState.selectedProgram).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `S02 state emits programs from repository after initialization`() = runTest {
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem() 
            val stateWithPrograms = awaitItem()
            assertThat(stateWithPrograms.programs).isEqualTo(testPrograms)
            assertThat(stateWithPrograms.selectedProgram).isNull()
        }
    }

    @Test
    fun `S03 state updates when repository programs change`() = runTest {
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem()
            awaitItem()

            val newPrograms = listOf(Program(id = 99, name = "Updated", country = "Test"))
            programsFlow.value = newPrograms
            advanceUntilIdle()

            val updatedState = awaitItem()
            assertThat(updatedState.programs).isEqualTo(newPrograms)
        }
    }

    // ========================================
    // PROGRAM SELECTION TESTS
    // Tests for program selection functionality
    // ========================================

    @Test
    fun `P01 selecting program updates state correctly`() = runTest {
        val programToSelect = testPrograms[0]
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onAction(RegistrationAction.SelectProgram(programToSelect))
            advanceUntilIdle()

            val updatedState = awaitItem()
            assertThat(updatedState.selectedProgram).isEqualTo(programToSelect)
            assertThat(updatedState.programs).isEqualTo(testPrograms)
        }
    }

    @Test
    fun `P02 program selection persists when repository updates`() = runTest {
        val selectedProgram = testPrograms[0]
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onAction(RegistrationAction.SelectProgram(selectedProgram))
            advanceUntilIdle()
            awaitItem()

            val newPrograms = listOf(Program(id = 3, name = "New", country = "New"))
            programsFlow.value = newPrograms
            advanceUntilIdle()

            val finalState = awaitItem()
            assertThat(finalState.programs).isEqualTo(newPrograms)
            assertThat(finalState.selectedProgram).isEqualTo(selectedProgram)
        }
    }

    // ========================================
    // CONFIRMATION TESTS
    // Tests for registration confirmation logic
    // ========================================

    @Test
    fun `C01 confirm without selection emits error and no events`() = runTest {
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(RegistrationAction.ConfirmRegistration)
            advanceUntilIdle()
            expectNoEvents()
        }

        coVerify(exactly = 0) { deviceCache.saveDevice(any(), any()) }
        coVerify(exactly = 0) { sessionCache.clearSession() }
        coVerify(exactly = 1) { ErrorMessageBus.emit(RegistrationError.PROGRAM_NOT_FOUND, any()) }
    }

    @Test
    fun `C02 repeated confirmations without selection emit multiple errors`() = runTest {
        repeat(3) {
            viewModel.onAction(RegistrationAction.ConfirmRegistration)
            advanceUntilIdle()
        }

        coVerify(exactly = 3) { ErrorMessageBus.emit(RegistrationError.PROGRAM_NOT_FOUND, any()) }
    }

    @Test
    fun `C03 successful confirmation calls operations in order and emits event`() = runTest {
        val selectedProgram = testPrograms[0]
        selectProgram(selectedProgram)

        viewModel.events.test {
            viewModel.onAction(RegistrationAction.ConfirmRegistration)
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isEqualTo(RegistrationEvent.NavigateToLandingScreen)
            expectNoEvents()
        }

        coVerifyOrder {
            deviceCache.saveDevice(match { it.id == -1 }, selectedProgram.id)
            sessionCache.clearSession()
        }
    }

    @Test
    fun `C04 successful confirmation creates device with correct properties`() = runTest {
        val selectedProgram = testPrograms[0]
        selectProgram(selectedProgram)

        viewModel.events.test {
            viewModel.onAction(RegistrationAction.ConfirmRegistration)
            advanceUntilIdle()
            awaitItem()
        }

        coVerify(exactly = 1) {
            deviceCache.saveDevice(
                device = match<Device> { device ->
                    device.id == -1 &&
                            device.registeredAt > 0 &&
                            device.submittedAt == null &&
                            device.model.isNotEmpty()
                },
                programId = selectedProgram.id
            )
        }
    }

    // ========================================
    // EVENT EMISSION TESTS
    // Tests for event handling and multiple emissions
    // ========================================

    @Test
    fun `E01 multiple successful confirmations emit multiple events`() = runTest {
        viewModel.state.test {
            awaitItem() 
            awaitItem() 
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.events.test {
            viewModel.onAction(RegistrationAction.SelectProgram(testPrograms[0]))
            advanceUntilIdle()
            viewModel.onAction(RegistrationAction.ConfirmRegistration)
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(RegistrationEvent.NavigateToLandingScreen)

            viewModel.onAction(RegistrationAction.SelectProgram(testPrograms[1]))
            advanceUntilIdle()
            viewModel.onAction(RegistrationAction.ConfirmRegistration)
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(RegistrationEvent.NavigateToLandingScreen)

            expectNoEvents()
        }
    }

    // ========================================
    // EXCEPTION HANDLING TESTS
    // Tests for error scenarios and exception handling
    // ========================================

    @Test
    fun `X01 saveDevice failure prevents clearSession and emits error`() = runTest {
        val selectedProgram = testPrograms[0]
        selectProgram(selectedProgram)

        coEvery { deviceCache.saveDevice(any(), any()) } throws RuntimeException("Save failed")

        viewModel.events.test {
            viewModel.onAction(RegistrationAction.ConfirmRegistration)
            advanceUntilIdle()
            expectNoEvents()
        }

        coVerify(exactly = 1) { deviceCache.saveDevice(any(), selectedProgram.id) }
        coVerify(exactly = 0) { sessionCache.clearSession() }
        coVerify(exactly = 1) { ErrorMessageBus.emit(RegistrationError.UNKNOWN_ERROR, any()) }
    }

    @Test
    fun `X02 clearSession failure after successful saveDevice emits error`() = runTest {
        val selectedProgram = testPrograms[0]
        selectProgram(selectedProgram)

        coEvery { sessionCache.clearSession() } throws RuntimeException("Clear failed")

        viewModel.events.test {
            viewModel.onAction(RegistrationAction.ConfirmRegistration)
            advanceUntilIdle()
            expectNoEvents()
        }

        coVerify(exactly = 1) { deviceCache.saveDevice(any(), selectedProgram.id) }
        coVerify(exactly = 1) { sessionCache.clearSession() }
        coVerify(exactly = 1) { ErrorMessageBus.emit(RegistrationError.UNKNOWN_ERROR, any()) }
    }

    @Test
    fun `X03 different exception types are handled correctly`() = runTest {
        val selectedProgram = testPrograms[0]
        selectProgram(selectedProgram)

        coEvery {
            deviceCache.saveDevice(
                any(),
                any()
            )
        } throws IllegalStateException("Custom error")

        viewModel.events.test {
            viewModel.onAction(RegistrationAction.ConfirmRegistration)
            advanceUntilIdle()
            expectNoEvents()
        }

        coVerify { ErrorMessageBus.emit(RegistrationError.UNKNOWN_ERROR, any()) }
    }

    // ========================================
    // EDGE CASE TESTS
    // Tests for unusual conditions and boundary cases
    // ========================================

    @Test
    fun `G01 empty repository emits error on confirmation`() = runTest {
        val emptyFlow = MutableStateFlow(emptyList<Program>())
        every { programRepository.observeAllPrograms() } returns emptyFlow
        val emptyRepoViewModel = RegistrationViewModel(deviceCache, sessionCache, programRepository)

        emptyRepoViewModel.onAction(RegistrationAction.ConfirmRegistration)
        advanceUntilIdle()

        coVerify { ErrorMessageBus.emit(RegistrationError.PROGRAM_NOT_FOUND, any()) }

        emptyRepoViewModel.events.test {
            expectNoEvents()
        }
    }

    @Test
    fun `G02 confirmation without event collector still completes operations`() = runTest {
        val selectedProgram = testPrograms[0]
        selectProgram(selectedProgram)

        viewModel.onAction(RegistrationAction.ConfirmRegistration)
        advanceUntilIdle()

        coVerify(exactly = 1) { deviceCache.saveDevice(any(), selectedProgram.id) }
        coVerify(exactly = 1) { sessionCache.clearSession() }
    }
}
