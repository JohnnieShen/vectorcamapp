package com.vci.vectorcamapp.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vci.vectorcamapp.core.presentation.util.error.ErrorMessageBus
import com.vci.vectorcamapp.core.presentation.util.error.LocalErrorDataFlow
import com.vci.vectorcamapp.ui.extensions.dimensions

@Composable
fun BaseScaffold(
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val errorFlow = ErrorMessageBus.errors
    CompositionLocalProvider(LocalErrorDataFlow provides errorFlow) {
        Scaffold(modifier = modifier) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                content(innerPadding)

                ErrorSnackbarHost(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = MaterialTheme.dimensions.paddingMedium)
                )
            }
        }
    }
}
