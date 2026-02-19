package com.dsquares.library.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Red,
    onPrimary = White,
    secondaryContainer = LightGray,
    onSecondaryContainer = White,
    surface = White,
    onSurface = DarkGray,
    surfaceVariant = OffWhite,
    onSurfaceVariant = Gray,
    errorContainer = Red,
    onErrorContainer = White,
    outline = BluishGray,
    onBackground = CardTitleColor,
    inverseSurface = CardSubtitleColor,
    scrim = Black,
    inverseOnSurface = White
)

@Composable
fun DsquareTaskTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}