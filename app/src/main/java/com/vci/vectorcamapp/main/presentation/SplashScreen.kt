package com.vci.vectorcamapp.main.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

            Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingLarge))

            Text(
                text = "Democratizing Vector Surveillance",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colors.textPrimary,
                modifier = Modifier.width(screenWidthFraction(0.8f)),
                textAlign = TextAlign.Center
            )
        }
    }
}
