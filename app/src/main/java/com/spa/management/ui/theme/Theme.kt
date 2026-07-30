package com.spa.management.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Bang mau chu dao lay cam hung tu spa: hong dao + xanh sage nhe nhang
val SpaPink = Color(0xFFE38A8A)
val SpaPinkDark = Color(0xFFB35E5E)
val SpaSage = Color(0xFF8AA68A)
val SpaCream = Color(0xFFFFF8F3)
val SpaBrown = Color(0xFF4A3B33)

private val LightColors = lightColorScheme(
    primary = SpaPink,
    onPrimary = Color.White,
    secondary = SpaSage,
    background = SpaCream,
    surface = Color.White,
    onBackground = SpaBrown,
    onSurface = SpaBrown,
    error = Color(0xFFB3261E)
)

private val DarkColors = darkColorScheme(
    primary = SpaPink,
    secondary = SpaSage
)

val SpaTypography = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyMedium = TextStyle(fontSize = 14.sp)
)

@Composable
fun SpaManagementTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = SpaTypography, content = content)
}
