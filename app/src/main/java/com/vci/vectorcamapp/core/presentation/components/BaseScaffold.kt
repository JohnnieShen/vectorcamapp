package com.vci.vectorcamapp.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vci.vectorcamapp.core.presentation.util.error.ErrorData
import com.vci.vectorcamapp.core.presentation.util.error.ErrorMessageBus
import com.vci.vectorcamapp.core.presentation.util.error.ErrorSnackbarHost
import com.vci.vectorcamapp.core.presentation.util.error.LocalErrorDataFlow
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun BaseScaffold(
    content: @Composable (PaddingValues) -> Unit,
    modifier: Modifier
) {
    val errorFlow = ErrorMessageBus.errors
    CompositionLocalProvider(LocalErrorDataFlow provides errorFlow) {
        Scaffold(modifier = modifier) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                content(innerPadding)

                ErrorSnackbarHost(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )
            }
        }
    }
}
