package com.example.ui.components

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ScreenLockRotation
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.BatteryGlassMode
import com.example.model.LauncherSettings
import com.example.model.SystemStatusSnapshot
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalGlassPerformance
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ControlCenterOverlay(
  visible: Boolean,
  settings: LauncherSettings,
  status: SystemStatusSnapshot,
  isMediaPlaying: Boolean,
  onClose: () -> Unit,
  onOpenWifi: () -> Unit,
  onOpenBluetooth: () -> Unit,
  onOpenAirplane: () -> Unit,
  onOpenDefaultLauncher: () -> Unit,
  onToggleFlashlight: () -> Unit,
  onToggleFocus: () -> Unit,
  onToggleEcoGlass: () -> Unit,
  onToggleDarkGlass: () -> Unit,
  onToggleOrientationLock: () -> Unit,
  onLockScreen: () -> Unit,
  onOpenThemeStudio: () -> Unit,
  onOpenAndroidSettings: () -> Unit,
  onBrightnessChange: (Float) -> Unit,
  onVolumeChange: (Int) -> Unit,
  onPlayPause: () -> Unit,
  onNext: () -> Unit,
  onPrev: () -> Unit,
) {
  val palette = LocalGlassPalette.current
  val perf = LocalGlassPerformance.current
  val isEco = perf.effectiveMode == BatteryGlassMode.ECO_BATTERY_SAVER

  AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 3 }),
    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 3 }),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(Color.Black.copy(alpha = 0.28f))
          .pointerInput(Unit) {
            detectVerticalDragGestures { _, dragAmount ->
              if (dragAmount < -32f) onClose()
            }
          }
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(horizontal = 18.dp, vertical = 12.dp)
          .testTag("control_center_overlay"),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier =
          Modifier.widthIn(max = 520.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Header Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "Control Center",
              style = MaterialTheme.typography.headlineMedium,
              color = palette.onGlass,
            )
            Text(
              text =
                "${status.wifiLabel} • ${status.batteryPercent}% • ${perf.effectiveMode.title}",
              style = MaterialTheme.typography.labelSmall,
              color = palette.onGlassMuted,
            )
          }
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiquidGlassSurface(
              shape = CircleShape,
              onClick = onLockScreen,
              modifier = Modifier.size(44.dp).testTag("cc_lock_button"),
            ) {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Rounded.Lock,
                  contentDescription = "Lock screen",
                  tint = palette.onGlass,
                  modifier = Modifier.size(20.dp),
                )
              }
            }
            LiquidGlassSurface(
              shape = CircleShape,
              onClick = onClose,
              modifier = Modifier.size(44.dp).testTag("cc_close_button"),
            ) {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Rounded.Close,
                  contentDescription = "Close Control Center",
                  tint = palette.onGlass,
                  modifier = Modifier.size(20.dp),
                )
              }
            }
          }
        }

        // Top Bento Row: Connectivity 2x2 + Now Playing
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          LiquidGlassSurface(
            modifier = Modifier.weight(1f).height(166.dp),
            elevated = true,
          ) {
            Column(
              modifier = Modifier.fillMaxSize().padding(14.dp),
              verticalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                ConnectivityOrb(
                  icon = Icons.Rounded.AirplanemodeActive,
                  label = "Airplane",
                  active = status.airplaneMode,
                  activeColor = palette.warningAccent,
                  onClick = onOpenAirplane,
                )
                ConnectivityOrb(
                  icon = Icons.Rounded.CellTower,
                  label = "Home",
                  active = status.isDefaultLauncher,
                  activeColor = palette.ecoAccent,
                  onClick = onOpenDefaultLauncher,
                )
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                ConnectivityOrb(
                  icon = Icons.Rounded.Wifi,
                  label = "Wi-Fi",
                  active = status.wifiConnected,
                  activeColor = palette.accent,
                  onClick = onOpenWifi,
                )
                ConnectivityOrb(
                  icon = Icons.Rounded.Bluetooth,
                  label = "Bluetooth",
                  active = status.bluetoothEnabled,
                  activeColor = palette.secondaryAccent,
                  onClick = onOpenBluetooth,
                )
              }
            }
          }

          LiquidGlassSurface(
            modifier = Modifier.weight(1f).height(166.dp),
            elevated = true,
          ) {
            Column(
              modifier = Modifier.fillMaxSize().padding(14.dp),
              verticalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier =
                    Modifier.size(36.dp)
                      .clip(RoundedCornerShape(10.dp))
                      .background(palette.accent.copy(alpha = 0.25f)),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = palette.onGlass,
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = if (isMediaPlaying) "Playing" else "Media Ready",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.onGlass,
                    maxLines = 1,
                  )
                  Text(
                    text = "System Audio",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.onGlassMuted,
                    maxLines = 1,
                  )
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                IconButton(onClick = onPrev, modifier = Modifier.size(40.dp)) {
                  Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = palette.onGlass)
                }
                IconButton(
                  onClick = onPlayPause,
                  modifier =
                    Modifier.size(48.dp)
                      .clip(CircleShape)
                      .background(Color.White.copy(alpha = 0.18f)),
                ) {
                  Icon(
                    imageVector = if (isMediaPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isMediaPlaying) "Pause" else "Play",
                    tint = palette.onGlass,
                  )
                }
                IconButton(onClick = onNext, modifier = Modifier.size(40.dp)) {
                  Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = palette.onGlass)
                }
              }
            }
          }
        }

        // Middle Row: Vertical Liquid Glass Sliders (Brightness + Volume) + 2x2 Quick Toggles
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          // 2x2 Quick Toggle Cluster
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              GlassQuickTile(
                icon = if (status.flashlightOn) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff,
                label = "Torch",
                active = status.flashlightOn,
                activeColor = palette.warningAccent,
                onClick = onToggleFlashlight,
                modifier = Modifier.weight(1f).height(86.dp).testTag("cc_torch_tile"),
              )
              GlassQuickTile(
                icon = Icons.Rounded.DoNotDisturbOn,
                label = "Focus",
                active = settings.focusModeEnabled,
                activeColor = palette.secondaryAccent,
                onClick = onToggleFocus,
                modifier = Modifier.weight(1f).height(86.dp).testTag("cc_focus_tile"),
              )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              GlassQuickTile(
                icon = Icons.Rounded.Eco,
                label = "Eco Glass",
                active = isEco,
                activeColor = palette.ecoAccent,
                onClick = onToggleEcoGlass,
                modifier = Modifier.weight(1f).height(86.dp).testTag("cc_eco_tile"),
              )
              GlassQuickTile(
                icon = Icons.Rounded.DarkMode,
                label = "Dark Glass",
                active = settings.darkGlassMode,
                activeColor = palette.accent,
                onClick = onToggleDarkGlass,
                modifier = Modifier.weight(1f).height(86.dp).testTag("cc_dark_tile"),
              )
            }
          }

          // Vertical Liquid Glass Brightness & Volume Pills
          Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            LiquidGlassVerticalSlider(
              fraction = status.brightnessLevel,
              icon = Icons.Rounded.BrightnessHigh,
              label = "${(status.brightnessLevel * 100).toInt()}%",
              accent = palette.warningAccent,
              onFractionChange = onBrightnessChange,
              modifier = Modifier.weight(1f).height(184.dp).testTag("cc_brightness_slider"),
            )
            LiquidGlassVerticalSlider(
              fraction = status.mediaVolume.toFloat() / status.maxMediaVolume.coerceAtLeast(1),
              icon = Icons.AutoMirrored.Rounded.VolumeUp,
              label = "${status.mediaVolume}",
              accent = palette.accent,
              onFractionChange = { frac ->
                val target = (frac * status.maxMediaVolume).toInt().coerceIn(0, status.maxMediaVolume)
                onVolumeChange(target)
              },
              modifier = Modifier.weight(1f).height(184.dp).testTag("cc_volume_slider"),
            )
          }
        }

        // Bottom Utility Pills
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          GlassQuickTile(
            icon =
              if (status.orientationLocked) Icons.Rounded.ScreenLockRotation
              else Icons.Rounded.ScreenRotation,
            label = if (status.orientationLocked) "Locked" else "Rotate",
            active = status.orientationLocked,
            activeColor = palette.dangerAccent,
            onClick = onToggleOrientationLock,
            modifier = Modifier.weight(1f).height(76.dp),
          )
          GlassQuickTile(
            icon = Icons.Rounded.Palette,
            label = "Glass Studio",
            active = false,
            activeColor = palette.accent,
            onClick = {
              onClose()
              onOpenThemeStudio()
            },
            modifier = Modifier.weight(1f).height(76.dp).testTag("cc_studio_tile"),
          )
          GlassQuickTile(
            icon = Icons.Rounded.Home,
            label = if (status.isDefaultLauncher) "Default" else "Set Home",
            active = status.isDefaultLauncher,
            activeColor = palette.ecoAccent,
            onClick = onOpenDefaultLauncher,
            modifier = Modifier.weight(1f).height(76.dp),
          )
          GlassQuickTile(
            icon = Icons.Rounded.Settings,
            label = "Android",
            active = false,
            activeColor = palette.accent,
            onClick = onOpenAndroidSettings,
            modifier = Modifier.weight(1f).height(76.dp),
          )
        }

        // Swipe up handle indicator
        Box(
          modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
          contentAlignment = Alignment.Center,
        ) {
          Box(
            modifier =
              Modifier.width(46.dp)
                .height(5.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.55f))
                .clickable { onClose() }
          )
        }
      }
    }
  }
}

@Composable
private fun ConnectivityOrb(
  icon: ImageVector,
  label: String,
  active: Boolean,
  activeColor: Color,
  onClick: () -> Unit,
) {
  val palette = LocalGlassPalette.current
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.width(64.dp).clickable { onClick() },
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier =
        Modifier.size(48.dp)
          .clip(CircleShape)
          .background(if (active) activeColor else Color.White.copy(alpha = 0.16f))
          .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (active) Color(0xFF020617) else palette.onGlass,
        modifier = Modifier.size(22.dp),
      )
    }
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = palette.onGlassMuted,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun GlassQuickTile(
  icon: ImageVector,
  label: String,
  active: Boolean,
  activeColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalGlassPalette.current
  LiquidGlassSurface(
    modifier = modifier,
    elevated = active,
    tintOverride = if (active) activeColor.copy(alpha = 0.32f) else null,
    onClick = onClick,
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(10.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (active) Color.White else palette.onGlass,
        modifier = Modifier.size(24.dp),
      )
      Spacer(modifier = Modifier.height(5.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = palette.onGlass,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun LiquidGlassVerticalSlider(
  fraction: Float,
  icon: ImageVector,
  label: String,
  accent: Color,
  onFractionChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalGlassPalette.current
  var heightPx by remember { mutableIntStateOf(1) }
  val clamped = fraction.coerceIn(0f, 1f)

  LiquidGlassSurface(
    modifier =
      modifier
        .onSizeChanged { heightPx = it.height.coerceAtLeast(1) }
        .pointerInput(Unit) {
          detectVerticalDragGestures { change, _ ->
            change.consume()
            val y = change.position.y.coerceIn(0f, heightPx.toFloat())
            val next = 1f - (y / heightPx.toFloat())
            onFractionChange(next)
          }
        },
    shape = RoundedCornerShape(28.dp),
    elevated = true,
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .fillMaxHeight(clamped)
            .align(Alignment.BottomCenter)
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.86f), accent.copy(alpha = 0.85f))
              )
            )
      )
      Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = label,
          fontFamily = JetBrainsMonoFontFamily,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold,
          color = if (clamped > 0.82f) Color(0xFF0F172A) else palette.onGlass,
        )
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (clamped > 0.25f) Color(0xFF0F172A) else palette.onGlass,
          modifier = Modifier.size(26.dp),
        )
      }
    }
  }
}
