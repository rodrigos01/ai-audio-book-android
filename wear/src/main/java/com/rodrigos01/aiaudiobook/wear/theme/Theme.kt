package com.rodrigos01.aiaudiobook.wear.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import com.rodrigos01.aiaudiobook.theme.backgroundDark
import com.rodrigos01.aiaudiobook.theme.backgroundLight
import com.rodrigos01.aiaudiobook.theme.errorContainerDark
import com.rodrigos01.aiaudiobook.theme.errorContainerLight
import com.rodrigos01.aiaudiobook.theme.errorDark
import com.rodrigos01.aiaudiobook.theme.errorLight
import com.rodrigos01.aiaudiobook.theme.onBackgroundDark
import com.rodrigos01.aiaudiobook.theme.onBackgroundLight
import com.rodrigos01.aiaudiobook.theme.onErrorContainerDark
import com.rodrigos01.aiaudiobook.theme.onErrorContainerLight
import com.rodrigos01.aiaudiobook.theme.onErrorDark
import com.rodrigos01.aiaudiobook.theme.onErrorLight
import com.rodrigos01.aiaudiobook.theme.onPrimaryContainerDark
import com.rodrigos01.aiaudiobook.theme.onPrimaryContainerLight
import com.rodrigos01.aiaudiobook.theme.onPrimaryDark
import com.rodrigos01.aiaudiobook.theme.onPrimaryLight
import com.rodrigos01.aiaudiobook.theme.onSecondaryContainerDark
import com.rodrigos01.aiaudiobook.theme.onSecondaryContainerLight
import com.rodrigos01.aiaudiobook.theme.onSecondaryDark
import com.rodrigos01.aiaudiobook.theme.onSecondaryLight
import com.rodrigos01.aiaudiobook.theme.onSurfaceDark
import com.rodrigos01.aiaudiobook.theme.onSurfaceLight
import com.rodrigos01.aiaudiobook.theme.onSurfaceVariantDark
import com.rodrigos01.aiaudiobook.theme.onSurfaceVariantLight
import com.rodrigos01.aiaudiobook.theme.onTertiaryContainerDark
import com.rodrigos01.aiaudiobook.theme.onTertiaryContainerLight
import com.rodrigos01.aiaudiobook.theme.onTertiaryDark
import com.rodrigos01.aiaudiobook.theme.onTertiaryLight
import com.rodrigos01.aiaudiobook.theme.outlineDark
import com.rodrigos01.aiaudiobook.theme.outlineLight
import com.rodrigos01.aiaudiobook.theme.outlineVariantDark
import com.rodrigos01.aiaudiobook.theme.outlineVariantLight
import com.rodrigos01.aiaudiobook.theme.primaryContainerDark
import com.rodrigos01.aiaudiobook.theme.primaryContainerLight
import com.rodrigos01.aiaudiobook.theme.primaryDark
import com.rodrigos01.aiaudiobook.theme.primaryLight
import com.rodrigos01.aiaudiobook.theme.secondaryContainerDark
import com.rodrigos01.aiaudiobook.theme.secondaryContainerLight
import com.rodrigos01.aiaudiobook.theme.secondaryDark
import com.rodrigos01.aiaudiobook.theme.secondaryLight
import com.rodrigos01.aiaudiobook.theme.surfaceContainerDark
import com.rodrigos01.aiaudiobook.theme.surfaceContainerHighDark
import com.rodrigos01.aiaudiobook.theme.surfaceContainerHighLight
import com.rodrigos01.aiaudiobook.theme.surfaceContainerLight
import com.rodrigos01.aiaudiobook.theme.surfaceContainerLowDark
import com.rodrigos01.aiaudiobook.theme.surfaceContainerLowLight
import com.rodrigos01.aiaudiobook.theme.tertiaryContainerDark
import com.rodrigos01.aiaudiobook.theme.tertiaryContainerLight
import com.rodrigos01.aiaudiobook.theme.tertiaryDark
import com.rodrigos01.aiaudiobook.theme.tertiaryLight

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