package com.primecut.theprimecut.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VividBlue,
    secondary = SlateGray,
    tertiary = OffWhite,
    background = MidnightBlue,
    surface = DarkCharcoal,
    onPrimary = OffWhite,
    onSecondary = OffWhite,
    onTertiary = MidnightBlue,
    onBackground = OffWhite,
    onSurface = OffWhite
)

private val LightColorScheme = lightColorScheme(
    primary = VividBlue,
    secondary = SlateGray,
    tertiary = MidnightBlue,
    background = OffWhite,
    surface = OffWhite,
    onPrimary = OffWhite,
    onSecondary = OffWhite,
    onTertiary = OffWhite,
    onBackground = MidnightBlue,
    onSurface = MidnightBlue
)

@Composable
fun ThePrimeCutTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
