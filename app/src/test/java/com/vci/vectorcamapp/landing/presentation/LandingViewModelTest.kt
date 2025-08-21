package com.vci.vectorcamapp.landing.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vci.vectorcamapp.core.domain.cache.CurrentSessionCache
import com.vci.vectorcamapp.core.domain.cache.DeviceCache
import com.vci.vectorcamapp.core.domain.model.Program
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.enums.SessionType
import com.vci.vectorcamapp.core.domain.repository.ProgramRepository
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.presentation.util.error.ErrorMessageBus
import com.vci.vectorcamapp.core.rules.MainDispatcherRule
import com.vci.vectorcamapp.landing.domain.util.LandingError
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
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class LandingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var deviceCache: DeviceCache
    private lateinit var sessionCache: CurrentSessionCache
    private lateinit var programRepository: ProgramRepository
    private lateinit var sessionRepository: SessionRepository
    private lateinit var viewModel: LandingViewModel

    // Observed by VM to derive incompleteSessionsCount
    private lateinit var incompleteFlow: MutableStateFlow<List<Session>>

    @Before
    fun setUp() {
        // Error bus
        mockkObject(ErrorMessageBus)
        coEvery { ErrorMessageBus.emit(any(), any()) } returns Unit

        // Mocks
        deviceCache = mockk(relaxed = true)
        sessionCache = mockk(relaxed = true)
        programRepository = mockk()
        sessionRepository = mockk()

        // Default flow
        incompleteFlow = MutableStateFlow(emptyList())
        every { sessionRepository.observeIncompleteSessions() } returns incompleteFlow
    }

    @After
    fun tearDown() {
        unmockkObject(ErrorMessageBus)
    }

    // ========================================
    // Test Harness / Helpers
    // ========================================

    private fun makeSession(type: SessionType) = Session(
        localId = UUID.randomUUID(),
        remoteId = null,
        houseNumber = "1",
        collectorTitle = "Dr.",
        collectorName = "Alice",
        collectionDate = 1_632_000_000L,
        collectionMethod = "Net",
        specimenCondition = "Good",
        createdAt = 1_632_000_100L,
        completedAt = null,
        submittedAt = null,
        notes = "",
        latitude = null,
        longitude = null,
        type = type
    )

    private fun initViewModel(
        programId: Int? = 1,
        program: Program? = Program(1, "Program A", "AA"),
        currentSession: Session? = null
    ) {
        coEvery { deviceCache.getProgramId() } returns programId
        if (programId != null) {
            coEvery { programRepository.getProgramById(programId) } returns program
        }
        coEvery { sessionCache.getSession() } returns currentSession
        viewModel = LandingViewModel(
            deviceCache = deviceCache,
            currentSessionCache = sessionCache,
            programRepository = programRepository,
            sessionRepository = sessionRepository
        )
    }

    // ========================================
    // A. Initialization & State
    // ========================================

    @Test
    fun landVm_a01_noProgramId_navigatesBackAndStopsLoading() = runTest {
        initViewModel(programId = null)

        viewModel.state.test {
            awaitItem() // triggers onStart -> loadLandingDetails

            // Now that loading started, collect events
            viewModel.events.test {
                assertThat(awaitItem()).isEqualTo(LandingEvent.NavigateBackToRegistrationScreen)
                expectNoEvents()
            }

            val afterLoad = awaitItem()
            assertThat(afterLoad.isLoading).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun landVm_a02_programMissing_navigatesBackAndStopsLoading() = runTest {
        initViewModel(programId = 7, program = null)

        viewModel.state.test {
            awaitItem()

            viewModel.events.test {
                assertThat(awaitItem()).isEqualTo(LandingEvent.NavigateBackToRegistrationScreen)
                expectNoEvents()
            }

            val after = awaitItem()
            assertThat(after.isLoading).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun landVm_a03_validProgram_setsProgram_andNoResumeDialogWhenNoSession() = runTest {
        val program = Program(5, "Valid", "US")
        initViewModel(programId = 5, program = program, currentSession = null)
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertThat(loaded.isLoading).isFalse()
            assertThat(loaded.enrolledProgram).isEqualTo(program)
            assertThat(loaded.showResumeDialog).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========================================
    // B. Resume / Dismiss Resume
    // ========================================

    @Test
    fun landVm_b01_existingSession_showsResumeDialog() = runTest {
        val session = makeSession(SessionType.DATA_COLLECTION)
        initViewModel(programId = 1, program = Program(1, "P", "C"), currentSession = session)
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertThat(loaded.showResumeDialog).isTrue()
            assertThat(loaded.enrolledProgram.name).isEqualTo("P")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun landVm_b02_resume_emitsNavigateWithCorrectType_andHidesDialog() = runTest {
        val session = makeSession(SessionType.SURVEILLANCE)
        initViewModel(programId = 1, program = Program(1, "P", "C"), currentSession = session)

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertThat(loaded.showResumeDialog).isTrue()

            viewModel.events.test {
                viewModel.onAction(LandingAction.ResumeSession)
                assertThat(awaitItem()).isEqualTo(
                    LandingEvent.NavigateToIntakeScreen(SessionType.SURVEILLANCE)
                )
                expectNoEvents()
            }

            val after = awaitItem()
            assertThat(after.showResumeDialog).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun landVm_b03_resume_whenNoSession_emitsError_andNoNavigation() = runTest {
        initViewModel(programId = 1, program = Program(1, "P", "C"), currentSession = null)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(LandingAction.ResumeSession)
            advanceUntilIdle()
            expectNoEvents()
        }

        coVerify(exactly = 1) { ErrorMessageBus.emit(LandingError.SESSION_NOT_FOUND, any()) }
    }

    @Test
    fun landVm_b04_dismissResume_clearsSession_andHidesDialog() = runTest {
        val session = makeSession(SessionType.DATA_COLLECTION)
        initViewModel(programId = 1, program = Program(1, "P", "C"), currentSession = session)

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertThat(loaded.showResumeDialog).isTrue()

            viewModel.onAction(LandingAction.DismissResumePrompt)

            val after = awaitItem()
            assertThat(after.showResumeDialog).isFalse()

            coVerify(exactly = 1) { sessionCache.clearSession() }

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========================================
    // C. Action Tiles -> Events
    // ========================================

    @Test
    fun landVm_c01_actionTiles_emitCorrectEvents() = runTest {
        initViewModel(programId = 1, program = Program(1, "P", "C"), currentSession = null)
        incompleteFlow.value = listOf(
            makeSession(SessionType.SURVEILLANCE),
            makeSession(SessionType.DATA_COLLECTION)
        )
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(LandingAction.StartNewSurveillanceSession)
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(
                LandingEvent.NavigateToIntakeScreen(SessionType.SURVEILLANCE)
            )

            viewModel.onAction(LandingAction.StartNewDataCollectionSession)
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(
                LandingEvent.NavigateToIntakeScreen(SessionType.DATA_COLLECTION)
            )

            viewModel.onAction(LandingAction.ViewIncompleteSessions)
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(LandingEvent.NavigateToIncompleteSessionsScreen)

            viewModel.onAction(LandingAction.ViewCompleteSessions)
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(LandingEvent.NavigateToCompleteSessionsScreen)

            expectNoEvents()
        }
    }

    // ========================================
    // D. Incomplete Count Updates
    // ========================================

    @Test
    fun landVm_d01_incompleteSessionsCount_updatesWithFlow() = runTest {
        initViewModel(programId = 5, program = Program(5, "Prog", "XX"), currentSession = null)

        viewModel.state.test {
            awaitItem()
            val s1 = awaitItem()
            assertThat(s1.incompleteSessionsCount).isEqualTo(0)

            incompleteFlow.value = listOf(makeSession(SessionType.SURVEILLANCE))
            advanceUntilIdle()
            val s2 = awaitItem()
            assertThat(s2.incompleteSessionsCount).isEqualTo(1)

            incompleteFlow.value = List(4) { makeSession(SessionType.DATA_COLLECTION) }
            advanceUntilIdle()
            val s3 = awaitItem()
            assertThat(s3.incompleteSessionsCount).isEqualTo(4)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========================================
    // E. Ordering / Side-effects
    // ========================================

    @Test
    fun landVm_e01_dismissThenStartNew_ordersAreRespected_noExtraErrors() = runTest {
        val session = makeSession(SessionType.SURVEILLANCE)
        initViewModel(programId = 2, program = Program(2, "Q", "YY"), currentSession = session)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(LandingAction.DismissResumePrompt)
            advanceUntilIdle()

            viewModel.onAction(LandingAction.StartNewDataCollectionSession)
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(
                LandingEvent.NavigateToIntakeScreen(SessionType.DATA_COLLECTION)
            )
            expectNoEvents()
        }

        coVerifyOrder {
            sessionCache.clearSession()
        }
        coVerify(exactly = 0) { ErrorMessageBus.emit(LandingError.SESSION_NOT_FOUND, any()) }
    }
}
