package com.example.model

import android.graphics.drawable.Drawable
import com.squareup.moshi.JsonClass

enum class ThemePack(val title: String, val subtitle: String) {
  CRYSTAL_CLEAR("Crystal Clear", "iOS 27 pure refractive glass"),
  MIDNIGHT_OBSIDIAN("Midnight Obsidian", "Deep smoked OLED glass"),
  PRISM_AURORA("Prism Aurora", "Iridescent teal & violet refraction"),
  WARM_AMBER("Champagne Amber", "Warm sunlit crystal tint"),
  ADAPTIVE_SYSTEM("Adaptive You", "Matches Android Material You"),
}

enum class IconGlassStyle(val title: String) {
  STANDARD_GLASS("Liquid Rim"),
  FROSTED_CLEAR("Clear Glass"),
  TINTED_CRYSTAL("Tinted Crystal"),
  DARK_SMOKED("Dark Smoked"),
}

enum class GridLayoutPreset(val columns: Int, val rows: Int, val label: String) {
  GRID_4X5(4, 5, "4 × 5 Classic"),
  GRID_4X6(4, 6, "4 × 6 Spacious"),
  GRID_5X6(5, 6, "5 × 6 Dense"),
}

enum class BatteryGlassMode(val title: String, val description: String) {
  AUTO_ADAPTIVE("Auto Adaptive", "Switches to Eco Glass below 20% or in Power Saver"),
  ULTRA_LIQUID("Ultra Liquid", "Full backdrop blur, live specular sheen & spring physics"),
  BALANCED_GLASS("Balanced Glass", "Hardware-cached layers with touch-reactive shimmer"),
  ECO_BATTERY_SAVER("Eco Glass", "Zero blur passes & static rims for maximum battery life"),
}

enum class WallpaperPreset(val title: String) {
  PRISM("Liquid Prism"),
  AURORA("Emerald Aurora"),
  AMBER("Champagne Gold"),
  CUSTOM_PHOTO("Custom Photo"),
}

enum class AppCategory(val title: String) {
  COMMUNICATION("Social & Messages"),
  PRODUCTIVITY("Productivity"),
  MEDIA("Media & Audio"),
  CREATIVITY("Photos & Creativity"),
  UTILITIES("Utilities & System"),
  GAMES("Games & Fun"),
}

data class InstalledApp(
  val packageName: String,
  val activityName: String,
  val label: String,
  val category: AppCategory,
  val icon: Drawable?,
  val isSystemShortcut: Boolean = false,
)

enum class WidgetType(val title: String, val subtitle: String, val isWide: Boolean) {
  CLOCK_WORLD("Liquid Clock", "Live time & world clock capsule", false),
  WEATHER_GLASS("Atmosphere", "Refractive forecast & UV telemetry", true),
  BATTERY_ECO("Power & Glass", "Real battery health & GPU glass mode", false),
  CALENDAR_AGENDA("Today Agenda", "Current date & next milestone", false),
  FOCUS_CHECKLIST("Focus Notes", "Interactive glass checklist", true),
  MEDIA_VOLUME("Audio & Volume", "Quick media playback & volume bar", true),
}

@JsonClass(generateAdapter = true)
data class HomeWidgetModel(
  val id: String,
  val type: WidgetType,
  val pageIndex: Int,
)

@JsonClass(generateAdapter = true)
data class HomeFolderModel(
  val id: String,
  val title: String,
  val packageNames: List<String>,
  val pageIndex: Int,
)

@JsonClass(generateAdapter = true)
data class HomeAppSlot(
  val id: String,
  val packageName: String,
  val pageIndex: Int,
)

@JsonClass(generateAdapter = true)
data class CustomAppOverride(
  val packageName: String,
  val customLabel: String? = null,
  val tintHex: Long? = null,
  val glyphKey: String? = null,
)

@JsonClass(generateAdapter = true)
data class FocusNoteItem(
  val id: String,
  val text: String,
  val completed: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class LauncherSettings(
  val themePack: ThemePack = ThemePack.CRYSTAL_CLEAR,
  val iconGlassStyle: IconGlassStyle = IconGlassStyle.STANDARD_GLASS,
  val gridPreset: GridLayoutPreset = GridLayoutPreset.GRID_4X5,
  val batteryMode: BatteryGlassMode = BatteryGlassMode.AUTO_ADAPTIVE,
  val wallpaperPreset: WallpaperPreset = WallpaperPreset.PRISM,
  val customWallpaperUri: String? = null,
  val iconCornerRadiusDp: Float = 18f,
  val glassRefractionStrength: Float = 0.85f,
  val showLabels: Boolean = true,
  val hapticEnabled: Boolean = true,
  val darkGlassMode: Boolean = false,
  val focusModeEnabled: Boolean = false,
  val lockScreenEnabled: Boolean = false,
  val autoEcoThresholdPercent: Int = 20,
)

@JsonClass(generateAdapter = true)
data class PersistedWorkspace(
  val pageCount: Int = 2,
  val dockPackages: List<String> = emptyList(),
  val appSlots: List<HomeAppSlot> = emptyList(),
  val folders: List<HomeFolderModel> = emptyList(),
  val widgets: List<HomeWidgetModel> = emptyList(),
  val appOverrides: List<CustomAppOverride> = emptyList(),
  val focusNotes: List<FocusNoteItem> = emptyList(),
)

data class GlassNotification(
  val id: String,
  val appName: String,
  val title: String,
  val message: String,
  val timeLabel: String,
  val accentHex: Long,
  val glyphKey: String = "bell",
  val isUrgent: Boolean = false,
)

data class SystemStatusSnapshot(
  val batteryPercent: Int = 85,
  val isCharging: Boolean = false,
  val batteryTempCelsius: Float = 31.2f,
  val isAndroidPowerSave: Boolean = false,
  val wifiConnected: Boolean = true,
  val wifiLabel: String = "Wi-Fi",
  val bluetoothEnabled: Boolean = false,
  val airplaneMode: Boolean = false,
  val flashlightOn: Boolean = false,
  val mediaVolume: Int = 7,
  val maxMediaVolume: Int = 15,
  val brightnessLevel: Float = 0.72f,
  val orientationLocked: Boolean = false,
  val isDefaultLauncher: Boolean = false,
)
