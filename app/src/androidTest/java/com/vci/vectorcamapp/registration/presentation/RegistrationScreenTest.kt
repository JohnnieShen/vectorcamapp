package com.vci.vectorcamapp.registration.presentation

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.vci.vectorcamapp.MainActivity
import com.vci.vectorcamapp.core.domain.model.Program
import com.vci.vectorcamapp.core.presentation.components.scaffold.BaseScaffold
import com.vci.vectorcamapp.navigation.Destination
import com.vci.vectorcamapp.ui.theme.VectorcamappTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class RegistrationScreenTest {

    private lateinit var navController: TestNavHostController

    private val testPrograms = listOf(
        Program(id = 1, name = "Test Program 1", country = "Country 1"),
        Program(id = 2, name = "Test Program 2", country = "Country 2"),
        Program(
            id = 3,
            name = "Very Very Very Long Name For Test Program 3",
            country = "Very Very Very Long Name For Country 3"
        ),
        Program(id = 4, name = "Test Program 4", country = "Country 4"),
        Program(id = 5, name = "Test Program 5", country = "Country 5"),
        Program(id = 6, name = "Test Program 6", country = "Country 6"),
        Program(id = 7, name = "Test Program 7", country = "Country 7"),
        Program(id = 8, name = "Test Program 8", country = "Country 8"),
        Program(id = 9, name = "Test Program 9", country = "Country 9"),
        Program(id = 10, name = "Test Program 10", country = "Country 10"),
        Program(id = 11, name = "Test Program 11", country = "Country 11"),
    )

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        navController = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
        }
    }

    private fun launchRegistrationScreen(
        initialState: RegistrationState = RegistrationState(),
    ) {
        composeRule.activity.setContent {
            var state by remember { mutableStateOf(initialState) }

            VectorcamappTheme {
                NavHost(
                    navController = navController, startDestination = Destination.Registration
                ) {
                    composable<Destination.Registration> {
                        BaseScaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            RegistrationScreen(
                                state = state, onAction = { action ->
                                    // Handle state updates for UI testing
                                    when (action) {
                                        is RegistrationAction.SelectProgram -> {
                                            state =
                                                state.copy(selectedProgram = action.program)
                                        }

                                        is RegistrationAction.ConfirmRegistration -> {
                                            navController.navigate(Destination.Landing)
                                        }
                                    }
                                }, modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                    composable<Destination.Landing> { }
                }
            }
        }
    }

    @Test
    fun whenProgramListIsEmpty_dropdownHasNoOptions_andConfirmButtonIsDisabled() {
        launchRegistrationScreen()
    }

    @Test
    fun whenProgramListIsNotEmpty_dropdownHasOptions_butConfirmButtonIsDisabled() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
    }

    @Test
    fun whenSelectedProgramIsNotNull_confirmButtonIsEnabled() {
        launchRegistrationScreen(
            initialState = RegistrationState(
                programs = testPrograms, selectedProgram = testPrograms[0]
            )
        )
    }

    @Test
    fun whenProgramIsSelected_dropdownCollapsesAndProgramDetailsAreDisplayed() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
    }

    @Test
    fun whenProgramIsSelected_confirmButtonChangesFromDisabledToEnabled() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
    }

    @Test
    fun selectingAnotherProgram_updatesSelection() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
    }

    @Test
    fun selectedProgram_displaysFullDetailsForLongNamesCorrectly() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
    }

    @Test
    fun whenConfirmButtonIsClicked_navigatesToLandingScreen() {
        launchRegistrationScreen(
            initialState = RegistrationState(
                programs = testPrograms, selectedProgram = testPrograms[0]
            )
        )
    }
}
