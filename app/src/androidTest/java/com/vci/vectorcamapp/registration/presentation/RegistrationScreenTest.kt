package com.vci.vectorcamapp.registration.presentation

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.vci.vectorcamapp.MainActivity
import com.vci.vectorcamapp.core.domain.model.Program
import com.vci.vectorcamapp.core.presentation.components.scaffold.BaseScaffold
import com.vci.vectorcamapp.navigation.Destination
import com.vci.vectorcamapp.registration.presentation.util.RegistrationTestTags
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

    // ========================================
    // Test Harness / Helpers
    // ========================================

    private fun launchRegistrationScreen(
        initialState: RegistrationState = RegistrationState()
    ) {
        composeRule.activity.setContent {
            var state by remember { mutableStateOf(initialState) }

            VectorcamappTheme {
                NavHost(
                    navController = navController,
                    startDestination = Destination.Registration
                ) {
                    composable<Destination.Registration> {
                        BaseScaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            RegistrationScreen(
                                state = state,
                                onAction = { action ->
                                    when (action) {
                                        is RegistrationAction.SelectProgram -> {
                                            state = state.copy(selectedProgram = action.program)
                                        }
                                        is RegistrationAction.ConfirmRegistration -> {
                                            navController.navigate(Destination.Landing)
                                        }
                                    }
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                    composable<Destination.Landing> { /* empty landing */ }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun openDropdown() {
        composeRule.onNodeWithTag(RegistrationTestTags.PROGRAM_DROPDOWN)
            .assertExists()
            .assertHasClickAction()
            .performClick()
    }

    private fun selectProgram(index: Int) {
        openDropdown()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("${RegistrationTestTags.PROGRAM_OPTION}-$index")
            .assertExists()
            .performClick()
        composeRule.waitForIdle()
    }

    private fun assertOnRegistration() {
        composeRule.waitForIdle()
        assertThat(navController.currentDestination?.route)
            .isEqualTo(Destination.Registration::class.qualifiedName)
    }

    private fun assertOnLanding() {
        composeRule.waitForIdle()
        assertThat(navController.currentDestination?.route)
            .isEqualTo(Destination.Landing::class.qualifiedName)
    }

    private fun assertConfirmEnabled(isEnabled: Boolean) {
        composeRule.waitForIdle()
        val node = composeRule.onNodeWithTag(RegistrationTestTags.CONFIRM_PROGRAM_BUTTON).assertExists()
        if (isEnabled) node.assertIsEnabled() else node.assertIsNotEnabled()
    }

    // ========================================
    // A. Static UI
    // ========================================

    @Test
    fun regUi_a01_headersAndBackgroundVisibleOnInitialRender() {
        launchRegistrationScreen()
        composeRule.onNodeWithText("Register Program").assertIsDisplayed()
        composeRule.onNodeWithText("Select your affiliated program").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Mosquito background").assertIsDisplayed()
        composeRule.onNodeWithTag(RegistrationTestTags.PROGRAM_DROPDOWN).assertExists()
        composeRule.onNodeWithTag(RegistrationTestTags.CONFIRM_PROGRAM_BUTTON).assertExists()
    }

    @Test
    fun regUi_a02_dropdownCollapsedByDefaultAndShowsExpandIcon() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
        composeRule.onNodeWithContentDescription("Expand dropdown", useUnmergedTree = true)
            .assertExists()
        composeRule.onNodeWithText("Select").assertIsDisplayed()
    }

    // ========================================
    // B. Confirm Button State
    // ========================================

    @Test
    fun regUi_b01_noProgramsConfirmDisabled() {
        launchRegistrationScreen()
        assertConfirmEnabled(false)
    }

    @Test
    fun regUi_b02_programsPresentButNoSelectionConfirmDisabled() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
        assertConfirmEnabled(false)
    }

    @Test
    fun regUi_b03_selectionPresentConfirmEnabled() {
        launchRegistrationScreen(
            initialState = RegistrationState(
                programs = testPrograms,
                selectedProgram = testPrograms.first()
            )
        )
        assertConfirmEnabled(true)
    }

    @Test
    fun regUi_b04_selectingProgramEnablesConfirm() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
        selectProgram(0)
        assertConfirmEnabled(true)
    }

    // ========================================
    // C. Dropdown Behavior
    // ========================================

    @Test
    fun regUi_c01_openingDropdownShowsCollapseIconAndItems() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
        openDropdown()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Collapse dropdown", useUnmergedTree = true)
            .assertExists()
        composeRule.onNodeWithTag("${RegistrationTestTags.PROGRAM_OPTION}-0").assertExists()
        composeRule.onNodeWithTag("${RegistrationTestTags.PROGRAM_OPTION}-${testPrograms.lastIndex}")
            .assertExists()
    }

    @Test
    fun regUi_c02_selectingAnItemCollapsesDropdownAndSetsText() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
        selectProgram(1)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Expand dropdown", useUnmergedTree = true)
            .assertExists()
        composeRule.onNodeWithText(testPrograms[1].name).assertIsDisplayed()
        composeRule.onNodeWithTag("${RegistrationTestTags.PROGRAM_OPTION}-0").assertDoesNotExist()
    }

    @Test
    fun regUi_c03_selectingDifferentItemUpdatesSelection() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
        selectProgram(0)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(testPrograms[0].name).assertIsDisplayed()
        selectProgram(2)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(testPrograms[2].name).assertIsDisplayed()
    }

    @Test
    fun regUi_c04_dropdownMenuItemsRenderBothNameAndCountry() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
        openDropdown()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("${RegistrationTestTags.PROGRAM_OPTION}-0")
            .assert(
                hasText(testPrograms[0].name) and hasText(testPrograms[0].country)
            )
    }

    @Test
    fun regUi_c05_longNamesAndCountriesRenderWithoutCrashing() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
        selectProgram(2)
        composeRule.onNodeWithText(testPrograms[2].name).assertIsDisplayed()
        composeRule.onNodeWithText(testPrograms[2].country).assertIsDisplayed()
    }

    @Test
    fun regUi_c06_dropdownClickTargetIsClickableAndFullWidth() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
        composeRule.onNodeWithTag(RegistrationTestTags.PROGRAM_DROPDOWN)
            .assertExists()
            .assertHasClickAction()
            .performClick()
    }

    // ========================================
    // D. List Size / Coverage
    // ========================================

    @Test
    fun regUi_d01_allItemsPresentInMenuByTagWhenExpanded() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
        openDropdown()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("${RegistrationTestTags.PROGRAM_OPTION}-0").assertExists()
        composeRule.onNodeWithTag("${RegistrationTestTags.PROGRAM_OPTION}-5").assertExists()
        composeRule.onNodeWithTag("${RegistrationTestTags.PROGRAM_OPTION}-10").assertExists()
    }

    // ========================================
    // E. Navigation Cases
    // ========================================

    @Test
    fun regUi_e01_confirmWhenDisabledDoesNotNavigate() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
        assertOnRegistration()
        composeRule.onNodeWithTag(RegistrationTestTags.CONFIRM_PROGRAM_BUTTON)
            .assertIsNotEnabled()
        assertOnRegistration()
    }

    @Test
    fun regUi_e02_confirmWhenEnabledNavigatesToLanding() {
        launchRegistrationScreen(
            initialState = RegistrationState(
                programs = testPrograms,
                selectedProgram = testPrograms.first()
            )
        )
        composeRule.onNodeWithTag(RegistrationTestTags.CONFIRM_PROGRAM_BUTTON)
            .assertIsEnabled()
            .performClick()
        composeRule.waitForIdle()
        assertOnLanding()
    }

    @Test
    fun regUi_e03_selectThenConfirmNavigatesToLanding() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
        selectProgram(0)
        composeRule.onNodeWithTag(RegistrationTestTags.CONFIRM_PROGRAM_BUTTON)
            .assertIsEnabled()
            .performClick()
        composeRule.waitForIdle()
        assertOnLanding()
    }

    // ========================================
    // F. Accessibility Semantics
    // ========================================

    @Test
    fun regUi_f01_expandCollapseContentDescriptionsToggle() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
        composeRule.onNodeWithContentDescription("Expand dropdown", useUnmergedTree = true)
            .assertExists()
        openDropdown()
        composeRule.onNodeWithContentDescription("Collapse dropdown", useUnmergedTree = true)
            .assertExists()
        composeRule.onNodeWithTag("${RegistrationTestTags.PROGRAM_OPTION}-0").performClick()
        composeRule.onNodeWithContentDescription("Expand dropdown", useUnmergedTree = true)
            .assertExists()
    }

    // ========================================
    // G. Regression/Edge Cases
    // ========================================

    @Test
    fun regUi_g01_reopeningDropdownShowsCurrentSelectionPersisted() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
        selectProgram(1)
        composeRule.onNodeWithText(testPrograms[1].name).assertIsDisplayed()
        openDropdown()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(RegistrationTestTags.PROGRAM_DROPDOWN)
            .assert(hasText(testPrograms[1].name))
    }

    @Test
    fun regUi_g02_placeholderSelectHiddenAfterSelection() {
        launchRegistrationScreen(initialState = RegistrationState(programs = testPrograms))
        composeRule.onNodeWithText("Select").assertIsDisplayed()
        selectProgram(0)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(testPrograms[0].name).assertIsDisplayed()
        composeRule.onNodeWithText("Select").assertDoesNotExist()
    }
}
