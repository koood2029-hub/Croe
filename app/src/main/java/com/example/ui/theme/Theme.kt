package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Semantic enterprise status colors bundle
data class EuiStatusColors(
  val success: Color,
  val successContainer: Color,
  val onSuccessContainer: Color,
  val warning: Color,
  val warningContainer: Color,
  val onWarningContainer: Color,
  val error: Color,
  val errorContainer: Color,
  val onErrorContainer: Color,
)

val LocalEuiStatusColors = staticCompositionLocalOf {
  EuiStatusColors(
    success = EuiSuccessLight,
    successContainer = EuiSuccessContainerLight,
    onSuccessContainer = EuiOnSuccessContainerLight,
    warning = EuiWarningLight,
    warningContainer = EuiWarningContainerLight,
    onWarningContainer = EuiOnWarningContainerLight,
    error = EuiErrorLight,
    errorContainer = EuiErrorContainerLight,
    onErrorContainer = EuiOnErrorContainerLight,
  )
}

private val DarkColorScheme =
  darkColorScheme(
    primary = EuiPrimaryDark,
    onPrimary = EuiOnPrimaryDark,
    primaryContainer = EuiPrimaryContainerDark,
    onPrimaryContainer = EuiOnPrimaryContainerDark,
    secondary = EuiSecondaryDark,
    onSecondary = EuiOnSecondaryDark,
    secondaryContainer = EuiSecondaryContainerDark,
    onSecondaryContainer = EuiOnSecondaryContainerDark,
    background = EuiBackgroundDark,
    onBackground = EuiOnBackgroundDark,
    surface = EuiSurfaceDark,
    onSurface = EuiOnSurfaceDark,
    surfaceVariant = EuiSurfaceVariantDark,
    onSurfaceVariant = EuiOnSurfaceVariantDark,
    outline = EuiOutlineDark,
    outlineVariant = EuiOutlineVariantDark,
    error = EuiErrorDark,
    onError = EuiOnPrimaryDark,
    errorContainer = EuiErrorContainerDark,
    onErrorContainer = EuiOnErrorContainerDark,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EuiPrimaryLight,
    onPrimary = EuiOnPrimaryLight,
    primaryContainer = EuiPrimaryContainerLight,
    onPrimaryContainer = EuiOnPrimaryContainerLight,
    secondary = EuiSecondaryLight,
    onSecondary = EuiOnSecondaryLight,
    secondaryContainer = EuiSecondaryContainerLight,
    onSecondaryContainer = EuiOnSecondaryContainerLight,
    background = EuiBackgroundLight,
    onBackground = EuiOnBackgroundLight,
    surface = EuiSurfaceLight,
    onSurface = EuiOnSurfaceLight,
    surfaceVariant = EuiSurfaceVariantLight,
    onSurfaceVariant = EuiOnSurfaceVariantLight,
    outline = EuiOutlineLight,
    outlineVariant = EuiOutlineVariantLight,
    error = EuiErrorLight,
    onError = EuiOnPrimaryLight,
    errorContainer = EuiErrorContainerLight,
    onErrorContainer = EuiOnErrorContainerLight,
  )

val LightEuiStatusColors =
  EuiStatusColors(
    success = EuiSuccessLight,
    successContainer = EuiSuccessContainerLight,
    onSuccessContainer = EuiOnSuccessContainerLight,
    warning = EuiWarningLight,
    warningContainer = EuiWarningContainerLight,
    onWarningContainer = EuiOnWarningContainerLight,
    error = EuiErrorLight,
    errorContainer = EuiErrorContainerLight,
    onErrorContainer = EuiOnErrorContainerLight,
  )

val DarkEuiStatusColors =
  EuiStatusColors(
    success = EuiSuccessDark,
    successContainer = EuiSuccessContainerDark,
    onSuccessContainer = EuiOnSuccessContainerDark,
    warning = EuiWarningDark,
    warningContainer = EuiWarningContainerDark,
    onWarningContainer = EuiOnWarningContainerDark,
    error = EuiErrorDark,
    errorContainer = EuiErrorContainerDark,
    onErrorContainer = EuiOnErrorContainerDark,
  )

@Composable
fun PocketCoreTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val statusColors = if (darkTheme) DarkEuiStatusColors else LightEuiStatusColors

  CompositionLocalProvider(LocalEuiStatusColors provides statusColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content,
    )
  }
}
