package com.rodrigos01.aiaudiobook.wear.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import com.rodrigos01.aiaudiobook.theme.backgroundDark
import com.rodrigos01.aiaudiobook.theme.errorContainerDark
import com.rodrigos01.aiaudiobook.theme.errorDark
import com.rodrigos01.aiaudiobook.theme.onBackgroundDark
import com.rodrigos01.aiaudiobook.theme.onErrorContainerDark
import com.rodrigos01.aiaudiobook.theme.onErrorDark
import com.rodrigos01.aiaudiobook.theme.onPrimaryContainerDark
import com.rodrigos01.aiaudiobook.theme.onPrimaryDark
import com.rodrigos01.aiaudiobook.theme.onSecondaryContainerDark
import com.rodrigos01.aiaudiobook.theme.onSecondaryDark
import com.rodrigos01.aiaudiobook.theme.onSurfaceDark
import com.rodrigos01.aiaudiobook.theme.onSurfaceVariantDark
import com.rodrigos01.aiaudiobook.theme.onTertiaryContainerDark
import com.rodrigos01.aiaudiobook.theme.onTertiaryDark
import com.rodrigos01.aiaudiobook.theme.outlineDark
import com.rodrigos01.aiaudiobook.theme.outlineVariantDark
import com.rodrigos01.aiaudiobook.theme.primaryContainerDark
import com.rodrigos01.aiaudiobook.theme.primaryDark
import com.rodrigos01.aiaudiobook.theme.secondaryContainerDark
import com.rodrigos01.aiaudiobook.theme.secondaryDark
import com.rodrigos01.aiaudiobook.theme.surfaceContainerDark
import com.rodrigos01.aiaudiobook.theme.surfaceContainerHighDark
import com.rodrigos01.aiaudiobook.theme.surfaceContainerLowDark
import com.rodrigos01.aiaudiobook.theme.tertiaryContainerDark
import com.rodrigos01.aiaudiobook.theme.tertiaryDark

private val wearScheme = ColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    onSurface = onSurfaceDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

@Composable
fun AIAudioBookWearTheme(
    content: @Composable() () -> Unit
) {

    MaterialTheme(
        colorScheme = wearScheme,
        typography = WearTypography,
        content = content
    )
}