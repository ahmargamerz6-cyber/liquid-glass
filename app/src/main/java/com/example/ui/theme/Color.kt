package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.model.ThemePack

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)

@Immutable
data class GlassPalette(
  val pack: ThemePack,
  val displayName: String,
  val surfaceTint: Color,
  val surfaceElevatedTint: Color,
  val specularHighlight: Color,
  val chromaticRimStart: Color,
  val chromaticRimEnd: Color,
  val accent: Color,
  val secondaryAccent: Color,
  val onGlass: Color,
  val onGlassMuted: Color,
  val pillScrim: Color,
  val ecoAccent: Color = Color(0xFF34D399),
  val warningAccent: Color = Color(0xFFFBBF24),
  val dangerAccent: Color = Color(0xFFFB7185),
)

object GlassThemePalettes {
  val CrystalClear =
    GlassPalette(
      pack = ThemePack.CRYSTAL_CLEAR,
      displayName = "Crystal Clear",
      surfaceTint = Color(0x38FFFFFF),
      surfaceElevatedTint = Color(0x52FFFFFF),
      specularHighlight = Color(0xCCFFFFFF),
      chromaticRimStart = Color(0xB3E0F2FE),
      chromaticRimEnd = Color(0x4D93C5FD),
      accent = Color(0xFF38BDF8),
      secondaryAccent = Color(0xFF818CF8),
      onGlass = Color(0xFFFFFFFF),
      onGlassMuted = Color(0xCCFFFFFF),
      pillScrim = Color(0x990B132B),
    )

  val MidnightObsidian =
    GlassPalette(
      pack = ThemePack.MIDNIGHT_OBSIDIAN,
      displayName = "Midnight Obsidian",
      surfaceTint = Color(0x730F172A),
      surfaceElevatedTint = Color(0x941E293B),
      specularHighlight = Color(0x80E2E8F0),
      chromaticRimStart = Color(0x8064748B),
      chromaticRimEnd = Color(0x3338BDF8),
      accent = Color(0xFF60A5FA),
      secondaryAccent = Color(0xFFA78BFA),
      onGlass = Color(0xFFF8FAFC),
      onGlassMuted = Color(0xB3CBD5E1),
      pillScrim = Color(0xD9020617),
    )

  val PrismAurora =
    GlassPalette(
      pack = ThemePack.PRISM_AURORA,
      displayName = "Prism Aurora",
      surfaceTint = Color(0x420D9488),
      surfaceElevatedTint = Color(0x5E134E4A),
      specularHighlight = Color(0xCCF0FDFA),
      chromaticRimStart = Color(0xB35EEAD4),
      chromaticRimEnd = Color(0x73A78BFA),
      accent = Color(0xFF2DD4BF),
      secondaryAccent = Color(0xFFC084FC),
      onGlass = Color(0xFFFFFFFF),
      onGlassMuted = Color(0xCCF0FDFA),
      pillScrim = Color(0xB3042F2E),
    )

  val WarmAmber =
    GlassPalette(
      pack = ThemePack.WARM_AMBER,
      displayName = "Champagne Amber",
      surfaceTint = Color(0x42F59E0B),
      surfaceElevatedTint = Color(0x5C78350F),
      specularHighlight = Color(0xCCFEF3C7),
      chromaticRimStart = Color(0xB3FDE68A),
      chromaticRimEnd = Color(0x66FB7185),
      accent = Color(0xFFFBBF24),
      secondaryAccent = Color(0xFFFB7185),
      onGlass = Color(0xFFFFFFFF),
      onGlassMuted = Color(0xCCFEF3C7),
      pillScrim = Color(0xB3291708),
    )

  fun forPack(pack: ThemePack, dynamicAccent: Color? = null, darkGlass: Boolean = false): GlassPalette {
    val base =
      when (pack) {
        ThemePack.CRYSTAL_CLEAR -> CrystalClear
        ThemePack.MIDNIGHT_OBSIDIAN -> MidnightObsidian
        ThemePack.PRISM_AURORA -> PrismAurora
        ThemePack.WARM_AMBER -> WarmAmber
        ThemePack.ADAPTIVE_SYSTEM -> {
          val accent = dynamicAccent ?: Color(0xFF38BDF8)
          CrystalClear.copy(
            pack = ThemePack.ADAPTIVE_SYSTEM,
            displayName = "Adaptive System",
            surfaceTint = if (darkGlass) Color(0x700F172A) else accent.copy(alpha = 0.22f),
            surfaceElevatedTint = if (darkGlass) Color(0x921E293B) else accent.copy(alpha = 0.34f),
            chromaticRimStart = accent.copy(alpha = 0.72f),
            chromaticRimEnd = Color(0x66FFFFFF),
            accent = accent,
          )
        }
      }
    return if (darkGlass && pack != ThemePack.MIDNIGHT_OBSIDIAN) {
      base.copy(
        surfaceTint = Color(0x78090E1A),
        surfaceElevatedTint = Color(0x9E111827),
        pillScrim = Color(0xD6050814),
      )
    } else {
      base
    }
  }
}
