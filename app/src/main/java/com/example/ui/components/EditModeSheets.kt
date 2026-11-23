package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Dock
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.BatteryGlassMode
import com.example.model.CustomAppOverride
import com.example.model.GridLayoutPreset
import com.example.model.HomeFolderModel
import com.example.model.IconGlassStyle
import com.example.model.InstalledApp
import com.example.model.LauncherSettings
import com.example.model.SystemStatusSnapshot
import com.example.model.ThemePack
import com.example.model.WallpaperPreset
import com.example.model.WidgetType
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalGlassPerformance
import com.example.viewmodel.LaunchingAppState

@Composable
fun LiquidGlassVolumeHud(
  visible: Boolean,
  volume: Int,
  maxVolume: Int,
  modifier: Modifier = Modifier,
) {
  val palette = LocalGlassPalette.current
  val fraction = (volume.toFloat() / maxVolume.coerceAtLeast(1)).coerceIn(0f, 1f)
  val animatedFraction by
    animateFloatAsState(
      targetValue = fraction,
      animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f),
      label = "vol_hud_fill",
    )

  AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
    exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it }),
    modifier = modifier,
  ) {
    LiquidGlassSurface(
      shape = RoundedCornerShape(28.dp),
      elevated = true,
      tintOverride = palette.pillScrim,
      modifier = Modifier.width(58.dp).height(210.dp).testTag("liquid_volume_hud"),
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .fillMaxHeight(animatedFraction)
              .align(Alignment.BottomCenter)
              .background(
                Brush.verticalGradient(
                  colors = listOf(Color.White.copy(alpha = 0.9f), palette.accent.copy(alpha = 0.85f))
                )
              )
        )
        Column(
          modifier = Modifier.fillMaxSize().padding(vertical = 14.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "$volume",
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.labelSmall,
            color = if (animatedFraction > 0.84f) Color(0xFF0F172A) else Color.White,
          )
          Icon(
            imageVector =
              if (volume == 0) Icons.AutoMirrored.Rounded.VolumeOff
              else Icons.AutoMirrored.Rounded.VolumeUp,
            contentDescription = "Volume",
            tint = if (animatedFraction > 0.25f) Color(0xFF0F172A) else Color.White,
            modifier = Modifier.size(22.dp),
          )
        }
      }
    }
  }
}

@Composable
fun AppMorphLaunchOverlay(launchingApp: LaunchingAppState?) {
  val palette = LocalGlassPalette.current
  AnimatedVisibility(
    visible = launchingApp != null,
    enter = fadeIn() + scaleIn(initialScale = 0.35f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 340f)),
    exit = fadeOut() + scaleOut(targetScale = 0.35f),
  ) {
    if (launchingApp != null) {
      val bmp = rememberDrawableBitmap(launchingApp.app.packageName, launchingApp.app.icon)
      val scale by
        animateFloatAsState(
          targetValue = if (launchingApp.isClosing) 0.55f else 1.05f,
          animationSpec = spring(dampingRatio = 0.82f, stiffness = 360f),
          label = "morph_scale",
        )
      Box(
        modifier =
          Modifier.fillMaxSize()
            .graphicsLayer {
              scaleX = scale
              scaleY = scale
            }
            .background(palette.pillScrim),
        contentAlignment = Alignment.Center,
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(
            contentAlignment = Alignment.Center,
            modifier =
              Modifier.size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(palette.surfaceElevatedTint)
                .border(1.5.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(28.dp)),
          ) {
            if (bmp != null) {
              Image(
                bitmap = bmp,
                contentDescription = launchingApp.app.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().padding(10.dp).clip(RoundedCornerShape(20.dp)),
              )
            } else {
              Icon(
                imageVector = Icons.Rounded.Apps,
                contentDescription = launchingApp.app.label,
                tint = Color.White,
                modifier = Modifier.size(48.dp),
              )
            }
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = launchingApp.app.label,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
          )
        }
      }
    }
  }
}

@Composable
fun FolderModalOverlay(
  folder: HomeFolderModel?,
  appsMap: Map<String, InstalledApp>,
  overrides: Map<String, CustomAppOverride>,
  iconStyle: IconGlassStyle,
  cornerRadiusDp: Float,
  isEditMode: Boolean,
  onClose: () -> Unit,
  onRenameFolder: (String, String) -> Unit,
  onLaunchApp: (InstalledApp) -> Unit,
  onRemoveFromFolder: (String, String) -> Unit,
) {
  val palette = LocalGlassPalette.current
  AnimatedVisibility(
    visible = folder != null,
    enter = fadeIn() + scaleIn(initialScale = 0.84f),
    exit = fadeOut() + scaleOut(targetScale = 0.84f),
  ) {
    if (folder != null) {
      var titleDraft by remember(folder.id, folder.title) { mutableStateOf(folder.title) }
      val folderApps = folder.packageNames.mapNotNull { appsMap[it] }

      Box(
        modifier =
          Modifier.fillMaxSize()
            .background(Color.Black.copy(alpha = 0.48f))
            .clickable { onClose() }
            .padding(24.dp)
            .testTag("folder_modal_overlay"),
        contentAlignment = Alignment.Center,
      ) {
        LiquidGlassSurface(
          elevated = true,
          shape = RoundedCornerShape(34.dp),
          onClick = {},
          modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              if (isEditMode) {
                OutlinedTextField(
                  value = titleDraft,
                  onValueChange = {
                    titleDraft = it
                    onRenameFolder(folder.id, it)
                  },
                  singleLine = true,
                  label = { Text("Folder Name") },
                  colors =
                    OutlinedTextFieldDefaults.colors(
                      focusedTextColor = palette.onGlass,
                      unfocusedTextColor = palette.onGlass,
                      focusedBorderColor = palette.accent,
                      unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                    ),
                  modifier = Modifier.weight(1f),
                )
              } else {
                Text(
                  text = folder.title,
                  style = MaterialTheme.typography.headlineMedium,
                  color = palette.onGlass,
                )
              }
              IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Close folder", tint = palette.onGlass)
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
              columns = GridCells.Fixed(3),
              modifier = Modifier.fillMaxWidth().height(260.dp),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
              items(folderApps, key = { it.packageName }) { app ->
                LiquidGlassAppIcon(
                  app = app,
                  iconSize = 58.dp,
                  cornerRadiusDp = cornerRadiusDp,
                  glassStyle = iconStyle,
                  override = overrides[app.packageName],
                  isEditMode = isEditMode,
                  onClick = { onLaunchApp(app) },
                  onLongPress = {},
                  onRemoveClick =
                    if (isEditMode) {
                      { onRemoveFromFolder(app.packageName, folder.id) }
                    } else null,
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun WidgetGalleryOverlay(
  visible: Boolean,
  onSelectWidget: (WidgetType) -> Unit,
  onClose: () -> Unit,
) {
  val palette = LocalGlassPalette.current
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + scaleIn(initialScale = 0.92f),
    exit = fadeOut() + scaleOut(targetScale = 0.92f),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(Color.Black.copy(alpha = 0.55f))
          .clickable { onClose() }
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(18.dp)
          .testTag("widget_gallery_overlay"),
      contentAlignment = Alignment.Center,
    ) {
      LiquidGlassSurface(
        elevated = true,
        shape = RoundedCornerShape(32.dp),
        tintOverride = palette.pillScrim,
        onClick = {},
        modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "Interactive Widgets",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
              )
              Text(
                text = "Tap any glass widget to add to this page",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.72f),
              )
            }
            IconButton(onClick = onClose) {
              Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
            }
          }

          WidgetType.entries.forEach { widgetType ->
            LiquidGlassSurface(
              shape = RoundedCornerShape(22.dp),
              onClick = { onSelectWidget(widgetType) },
              modifier = Modifier.fillMaxWidth().testTag("add_widget_${widgetType.name}"),
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = widgetType.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                  )
                  Text(
                    text = widgetType.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.74f),
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                  modifier =
                    Modifier.clip(CircleShape)
                      .background(palette.accent.copy(alpha = 0.25f))
                      .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = palette.accent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (widgetType.isWide) "4×2" else "2×2",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ThemeStudioOverlay(
  visible: Boolean,
  settings: LauncherSettings,
  status: SystemStatusSnapshot,
  onSelectThemePack: (ThemePack) -> Unit,
  onSelectIconStyle: (IconGlassStyle) -> Unit,
  onSelectGridPreset: (GridLayoutPreset) -> Unit,
  onSelectBatteryMode: (BatteryGlassMode) -> Unit,
  onSelectWallpaper: (WallpaperPreset) -> Unit,
  onPickCustomPhoto: () -> Unit,
  onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
  onOpenDefaultLauncher: () -> Unit,
  onLockNow: () -> Unit,
  onClose: () -> Unit,
) {
  val palette = LocalGlassPalette.current
  val perf = LocalGlassPerformance.current

  AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + scaleIn(initialScale = 0.94f),
    exit = fadeOut() + scaleOut(targetScale = 0.94f),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(Color.Black.copy(alpha = 0.58f))
          .clickable { onClose() }
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(16.dp)
          .testTag("theme_studio_overlay"),
      contentAlignment = Alignment.Center,
    ) {
      LiquidGlassSurface(
        elevated = true,
        shape = RoundedCornerShape(32.dp),
        tintOverride = palette.pillScrim,
        onClick = {},
        modifier = Modifier.widthIn(max = 520.dp).fillMaxSize(),
      ) {
        Column(
          modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "Glass 27 Studio",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
              )
              Text(
                text = "Themes, Icons, Grid & Battery Engine",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.72f),
              )
            }
            IconButton(onClick = onClose, modifier = Modifier.testTag("studio_close_btn")) {
              Icon(Icons.Rounded.Close, contentDescription = "Close Studio", tint = Color.White)
            }
          }

          // Theme Packs
          StudioSectionTitle("Liquid Glass Theme Packs")
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            ThemePack.entries.forEach { pack ->
              val selected = settings.themePack == pack
              StudioChipCard(
                title = pack.title,
                subtitle = pack.subtitle,
                selected = selected,
                accent = palette.accent,
                onClick = { onSelectThemePack(pack) },
                modifier = Modifier.width(156.dp).testTag("theme_pack_${pack.name}"),
              )
            }
          }

          // Wallpapers
          StudioSectionTitle("Refractive Wallpapers")
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            listOf(
              WallpaperPreset.PRISM,
              WallpaperPreset.AURORA,
              WallpaperPreset.AMBER,
            ).forEach { preset ->
              StudioPillChip(
                label = preset.title,
                selected = settings.wallpaperPreset == preset,
                accent = palette.accent,
                onClick = { onSelectWallpaper(preset) },
              )
            }
            LiquidGlassSurface(
              shape = CircleShape,
              elevated = settings.wallpaperPreset == WallpaperPreset.CUSTOM_PHOTO,
              onClick = onPickCustomPhoto,
              modifier = Modifier.testTag("pick_photo_wallpaper_btn"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = palette.accent, modifier = Modifier.size(16.dp))
                Text("Photo Picker", style = MaterialTheme.typography.labelLarge, color = Color.White)
              }
            }
          }

          // Icon Style & Grid
          StudioSectionTitle("Liquid Glass Icons & Grid")
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            IconGlassStyle.entries.forEach { style ->
              StudioPillChip(
                label = style.title,
                selected = settings.iconGlassStyle == style,
                accent = palette.accent,
                onClick = { onSelectIconStyle(style) },
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            GridLayoutPreset.entries.forEach { preset ->
              StudioPillChip(
                label = preset.label,
                selected = settings.gridPreset == preset,
                accent = palette.secondaryAccent,
                onClick = { onSelectGridPreset(preset) },
                modifier = Modifier.weight(1f),
              )
            }
          }

          // Corner Radius + Refraction Sliders
          Column {
            Text(
              text = "Icon Corner Radius (${settings.iconCornerRadiusDp.toInt()}dp)",
              style = MaterialTheme.typography.labelLarge,
              color = Color.White,
            )
            Slider(
              value = settings.iconCornerRadiusDp,
              onValueChange = { r -> onUpdateSettings { it.copy(iconCornerRadiusDp = r) } },
              valueRange = 10f..28f,
              colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = palette.accent),
            )
            Text(
              text = "Glass Refraction Intensity (${(settings.glassRefractionStrength * 100).toInt()}%)",
              style = MaterialTheme.typography.labelLarge,
              color = Color.White,
            )
            Slider(
              value = settings.glassRefractionStrength,
              onValueChange = { s -> onUpdateSettings { it.copy(glassRefractionStrength = s) } },
              valueRange = 0.35f..1.0f,
              colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = palette.accent),
            )
          }

          // Battery Optimization Engine
          StudioSectionTitle("Battery & GPU Glass Optimization")
          LiquidGlassSurface(
            shape = RoundedCornerShape(20.dp),
            tintOverride = palette.ecoAccent.copy(alpha = 0.14f),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector =
                    if (status.isCharging) Icons.Rounded.BatteryChargingFull
                    else Icons.Rounded.Eco,
                  contentDescription = null,
                  tint = palette.ecoAccent,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Active Profile: ${perf.effectiveMode.title}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                  )
                  Text(
                    text =
                      "Blur: ${if (perf.enableBlur) "Hardware" else "Eco Off"} • Battery ${status.batteryPercent}% • ${status.batteryTempCelsius}°C",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.78f),
                  )
                }
              }
            }
          }

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BatteryGlassMode.entries.forEach { mode ->
              val selected = settings.batteryMode == mode
              LiquidGlassSurface(
                shape = RoundedCornerShape(18.dp),
                elevated = selected,
                tintOverride = if (selected) palette.ecoAccent.copy(alpha = 0.22f) else null,
                onClick = { onSelectBatteryMode(mode) },
                modifier = Modifier.fillMaxWidth().testTag("battery_mode_${mode.name}"),
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(mode.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(mode.description, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.74f))
                  }
                  if (selected) {
                    Text("ACTIVE", style = MaterialTheme.typography.labelSmall, color = palette.ecoAccent, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }

          // Switches
          StudioToggleRow(
            title = "Haptic Gesture Feedback",
            checked = settings.hapticEnabled,
            onCheckedChange = { v -> onUpdateSettings { it.copy(hapticEnabled = v) } },
          )
          StudioToggleRow(
            title = "Show App Labels on Home",
            checked = settings.showLabels,
            onCheckedChange = { v -> onUpdateSettings { it.copy(showLabels = v) } },
          )
          StudioToggleRow(
            title = "Dark Smoked Glass Mode",
            checked = settings.darkGlassMode,
            onCheckedChange = { v -> onUpdateSettings { it.copy(darkGlassMode = v) } },
          )
          StudioToggleRow(
            title = "Show Lock Screen on Launch",
            checked = settings.lockScreenEnabled,
            onCheckedChange = { v -> onUpdateSettings { it.copy(lockScreenEnabled = v) } },
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Button(
              onClick = onOpenDefaultLauncher,
              colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = Color(0xFF020617)),
              modifier = Modifier.weight(1f),
            ) {
              Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Default Home", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Button(
              onClick = {
                onClose()
                onLockNow()
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.18f), contentColor = Color.White),
              modifier = Modifier.weight(1f),
            ) {
              Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Test Lock", maxLines = 1)
            }
          }
        }
      }
    }
  }
}

@Composable
fun AppCustomizerOverlay(
  app: InstalledApp?,
  override: CustomAppOverride?,
  folders: List<HomeFolderModel>,
  isInDock: Boolean,
  onSaveOverride: (String, String?, Long?, String?) -> Unit,
  onCreateFolder: (String, String) -> Unit,
  onMoveToFolder: (String, String) -> Unit,
  onToggleDock: (String) -> Unit,
  onRemoveFromHome: (String) -> Unit,
  onOpenAppInfo: (String) -> Unit,
  onUninstallApp: (String) -> Unit,
  onClose: () -> Unit,
) {
  val palette = LocalGlassPalette.current
  AnimatedVisibility(
    visible = app != null,
    enter = fadeIn() + scaleIn(initialScale = 0.9f),
    exit = fadeOut() + scaleOut(targetScale = 0.9f),
  ) {
    if (app != null) {
      var labelDraft by remember(app.packageName) { mutableStateOf(override?.customLabel ?: app.label) }
      var selectedTint by remember(app.packageName) { mutableStateOf(override?.tintHex) }
      var selectedGlyph by remember(app.packageName) { mutableStateOf(override?.glyphKey) }
      var newFolderName by remember(app.packageName) { mutableStateOf("") }

      Box(
        modifier =
          Modifier.fillMaxSize()
            .background(Color.Black.copy(alpha = 0.56f))
            .clickable { onClose() }
            .padding(20.dp)
            .testTag("app_customizer_dialog"),
        contentAlignment = Alignment.Center,
      ) {
        LiquidGlassSurface(
          elevated = true,
          shape = RoundedCornerShape(30.dp),
          tintOverride = palette.pillScrim,
          onClick = {},
          modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("Customize App Icon", style = MaterialTheme.typography.headlineMedium, color = Color.White)
              IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
              }
            }

            OutlinedTextField(
              value = labelDraft,
              onValueChange = { labelDraft = it },
              label = { Text("Custom Label") },
              singleLine = true,
              colors =
                OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White,
                  focusedBorderColor = palette.accent,
                  unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                ),
              modifier = Modifier.fillMaxWidth(),
            )

            Text("Glass Tint Override", style = MaterialTheme.typography.labelLarge, color = Color.White)
            Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              CustomTintOptions.forEach { (hex, title) ->
                val isSelected = selectedTint == hex
                Box(
                  modifier =
                    Modifier.clip(CircleShape)
                      .background(if (hex != null) Color(hex).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.14f))
                      .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Color.White else Color.White.copy(alpha = 0.3f), CircleShape)
                      .clickable { selectedTint = hex }
                      .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                  Text(title, style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
              }
            }

            Text("Custom Icon Glyph", style = MaterialTheme.typography.labelLarge, color = Color.White)
            Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              CustomGlyphOptions.forEach { (key, title) ->
                val isSelected = selectedGlyph == key
                Box(
                  modifier =
                    Modifier.clip(RoundedCornerShape(14.dp))
                      .background(if (isSelected) palette.accent.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.12f))
                      .border(1.dp, if (isSelected) palette.accent else Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                      .clickable { selectedGlyph = key }
                      .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(glyphVectorForKey(key), contentDescription = title, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(title, style = MaterialTheme.typography.labelSmall, color = Color.White)
                  }
                }
              }
            }

            Button(
              onClick = {
                val finalLabel = if (labelDraft.trim() == app.label) null else labelDraft.trim()
                onSaveOverride(app.packageName, finalLabel, selectedTint, selectedGlyph)
              },
              colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = Color(0xFF020617)),
              modifier = Modifier.fillMaxWidth().testTag("save_custom_icon_btn"),
            ) {
              Text("Apply Custom Icon")
            }

            // Folder Grouping Section
            Text("Folder Grouping", style = MaterialTheme.typography.labelLarge, color = Color.White)
            if (folders.isNotEmpty()) {
              Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                folders.forEach { folder ->
                  StudioPillChip(
                    label = "Move → ${folder.title}",
                    selected = false,
                    accent = palette.secondaryAccent,
                    onClick = { onMoveToFolder(app.packageName, folder.id) },
                  )
                }
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              OutlinedTextField(
                value = newFolderName,
                onValueChange = { newFolderName = it },
                placeholder = { Text("New folder name") },
                singleLine = true,
                colors =
                  OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = palette.accent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                  ),
                modifier = Modifier.weight(1f),
              )
              Button(
                onClick = { onCreateFolder(app.packageName, newFolderName.ifBlank { "Group" }) },
                modifier = Modifier.testTag("create_folder_btn"),
              ) {
                Icon(Icons.Rounded.CreateNewFolder, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Group")
              }
            }

            // Dock & Removal Actions
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              TextButton(
                onClick = { onToggleDock(app.packageName) },
                modifier = Modifier.weight(1f),
              ) {
                Icon(Icons.Rounded.Dock, contentDescription = null, tint = palette.accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (isInDock) "Unpin Dock" else "Pin Dock", color = Color.White, maxLines = 1)
              }
              TextButton(
                onClick = { onRemoveFromHome(app.packageName) },
                modifier = Modifier.weight(1f).testTag("remove_from_home_btn"),
              ) {
                Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = null, tint = palette.warningAccent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Remove Home", color = palette.warningAccent, maxLines = 1)
              }
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              TextButton(onClick = { onOpenAppInfo(app.packageName) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Rounded.Info, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("App Info", color = Color.White.copy(alpha = 0.85f))
              }
              TextButton(onClick = { onUninstallApp(app.packageName) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = palette.dangerAccent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Uninstall", color = palette.dangerAccent)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StudioSectionTitle(text: String) {
  Text(
    text = text.uppercase(),
    style = MaterialTheme.typography.labelSmall,
    color = Color.White.copy(alpha = 0.68f),
  )
}

@Composable
private fun StudioChipCard(
  title: String,
  subtitle: String,
  selected: Boolean,
  accent: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  LiquidGlassSurface(
    shape = RoundedCornerShape(20.dp),
    elevated = selected,
    tintOverride = if (selected) accent.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.1f),
    onClick = onClick,
    modifier = modifier,
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1)
      Spacer(modifier = Modifier.height(2.dp))
      Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f), maxLines = 2)
    }
  }
}

@Composable
private fun StudioPillChip(
  label: String,
  selected: Boolean,
  accent: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier =
      modifier
        .clip(CircleShape)
        .background(if (selected) accent.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.12f))
        .border(1.dp, if (selected) accent else Color.White.copy(alpha = 0.28f), CircleShape)
        .clickable { onClick() }
        .padding(horizontal = 14.dp, vertical = 10.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelLarge,
      color = Color.White,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun StudioToggleRow(
  title: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}
