package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.model.BatteryGlassMode
import com.example.model.ThemePack

val LocalGlassPalette = staticCompositionLocalOf { GlassThemePalettes.CrystalClear }
val LocalGlassPerformance = staticCompositionLocalOf { GlassPerformanceConfig() }

data class GlassPerformanceConfig(
  val effectiveMode: BatteryGlassMode = BatteryGlassMode.BALANCED_GLASS,
  val enableBlur: Boolean = true,
  val enableSpecularAnimation: Boolean = true,
  val enableJiggleMotion: Boolean = true,
  val glassRefraction: Float = 0.8f,
)

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF38BDF8),
    secondary = Color(0xFF818CF8),
    tertiary = Color(0xFF2DD4BF),
    background = Color(0xFF040814),
    surface = Color(0xFF0F172A),
    onPrimary = Color(0xFF020617),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF0284C7),
    secondary = Color(0xFF6366F1),
    tertiary = Color(0xFF0D9488),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
  )

@Composable
fun Glass27Theme(
  themePack: ThemePack = ThemePack.CRYSTAL_CLEAR,
  darkGlass: Boolean = isSystemInDarkTheme(),
  performanceConfig: GlassPerformanceConfig = GlassPerformanceConfig(),
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val colorScheme =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      if (darkGlass) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
      if (darkGlass) DarkColorScheme else LightColorScheme
    }

  val palette =
    GlassThemePalettes.forPack(
      pack = themePack,
      dynamicAccent = colorScheme.primary,
      darkGlass = darkGlass,
    )

  CompositionLocalProvider(
    LocalGlassPalette provides palette,
    LocalGlassPerformance provides performanceConfig,
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content,
    )
  }
}
