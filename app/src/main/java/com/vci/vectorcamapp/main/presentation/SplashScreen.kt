package com.vci.vectorcamapp.main.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import com.vci.vectorcamapp.BuildConfig
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions
import com.vci.vectorcamapp.ui.theme.screenWidthFraction

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colors.accent,
                        MaterialTheme.colors.appBackground
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.splash_logo),
                contentDescription = "Splash logo",
                modifier = Modifier.width(screenWidthFraction(0.9f))
            )

            Text(
                text = "Democratizing Vector Surveillance",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colors.textPrimary,
                modifier = Modifier.padding(vertical = MaterialTheme.dimensions.paddingSmall),
            )

            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colors.textPrimary,
                modifier = Modifier.padding(vertical = MaterialTheme.dimensions.paddingSmall),
            )
        }
    }
}
