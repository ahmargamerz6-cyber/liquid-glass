package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.BatteryGlassMode
import com.example.model.GlassNotification
import com.example.model.SystemStatusSnapshot
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalGlassPerformance
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun LockScreenOverlay(
  visible: Boolean,
  timeFormatted: String,
  dateFormatted: String,
  status: SystemStatusSnapshot,
  notifications: List<GlassNotification>,
  onUnlock: () -> Unit,
  onToggleFlashlight: () -> Unit,
  onOpenCamera: () -> Unit,
) {
  val palette = LocalGlassPalette.current
  val perf = LocalGlassPerformance.current
  val isEco = perf.effectiveMode == BatteryGlassMode.ECO_BATTERY_SAVER
  val dragOffsetY = remember(visible) { Animatable(0f) }
  val scope = rememberCoroutineScope()

  AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 4 }),
    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .offset { IntOffset(0, dragOffsetY.value.roundToInt().coerceAtMost(0)) }
          .pointerInput(Unit) {
            detectVerticalDragGestures(
              onVerticalDrag = { _, drag ->
                scope.launch {
                  dragOffsetY.snapTo((dragOffsetY.value + drag).coerceAtMost(0f))
                }
              },
              onDragEnd = {
                scope.launch {
                  if (dragOffsetY.value < -150f) {
                    onUnlock()
                  } else {
                    dragOffsetY.animateTo(0f, spring(dampingRatio = 0.72f, stiffness = 360f))
                  }
                }
              },
            )
          }
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(horizontal = 24.dp, vertical = 18.dp)
          .testTag("lock_screen_overlay"),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier = Modifier.widthIn(max = 480.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        // Top Clock + Glass Depth Telemetry
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(top = 24.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.LockOpen,
              contentDescription = null,
              tint = palette.onGlassMuted,
              modifier = Modifier.size(16.dp),
            )
            Text(
              text = dateFormatted.uppercase(),
              style = MaterialTheme.typography.labelLarge,
              color = palette.onGlassMuted,
            )
          }

          Text(
            text = timeFormatted,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Light,
            color = palette.onGlass.copy(alpha = 0.94f),
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Compact Liquid Glass Status Capsule
          LiquidGlassSurface(shape = CircleShape, elevated = true) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                imageVector =
                  if (status.isCharging) Icons.Rounded.BatteryChargingFull
                  else Icons.Rounded.BatteryStd,
                contentDescription = null,
                tint = if (status.isCharging) palette.ecoAccent else palette.onGlass,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "${status.batteryPercent}%",
                fontFamily = JetBrainsMonoFontFamily,
                style = MaterialTheme.typography.labelLarge,
                color = palette.onGlass,
              )
              if (isEco) {
                Icon(
                  imageVector = Icons.Rounded.Eco,
                  contentDescription = null,
                  tint = palette.ecoAccent,
                  modifier = Modifier.size(15.dp),
                )
              }
              Text(
                text = status.wifiLabel,
                style = MaterialTheme.typography.labelSmall,
                color = palette.onGlassMuted,
              )
            }
          }
        }

        // Stacked Lock Screen Notifications
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          notifications.take(2).forEach { notif ->
            LiquidGlassSurface(
              shape = RoundedCornerShape(24.dp),
              elevated = true,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier =
                    Modifier.size(36.dp)
                      .clip(RoundedCornerShape(11.dp))
                      .background(Color(notif.accentHex).copy(alpha = 0.28f)),
                ) {
                  Icon(
                    imageVector = glyphVectorForKey(notif.glyphKey),
                    contentDescription = null,
                    tint = Color(notif.accentHex),
                    modifier = Modifier.size(19.dp),
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = notif.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.onGlass,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = notif.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.onGlassMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
            }
          }
        }

        // Bottom Controls: Torch, Unlock Pill, Camera
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            LiquidGlassSurface(
              shape = CircleShape,
              elevated = status.flashlightOn,
              tintOverride = if (status.flashlightOn) palette.warningAccent.copy(alpha = 0.35f) else null,
              onClick = onToggleFlashlight,
              modifier = Modifier.size(54.dp),
            ) {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                  imageVector =
                    if (status.flashlightOn) Icons.Rounded.FlashlightOn
                    else Icons.Rounded.FlashlightOff,
                  contentDescription = stringResource(R.string.cd_flashlight),
                  tint = palette.onGlass,
                )
              }
            }

            LiquidGlassSurface(
              shape = CircleShape,
              elevated = true,
              onClick = onUnlock,
              modifier = Modifier.height(54.dp).testTag("lock_unlock_pill"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Fingerprint,
                  contentDescription = stringResource(R.string.cd_unlock),
                  tint = palette.accent,
                  modifier = Modifier.size(22.dp),
                )
                Text(
                  text = "Tap or Swipe to Unlock",
                  style = MaterialTheme.typography.labelLarge,
                  color = palette.onGlass,
                )
              }
            }

            LiquidGlassSurface(
              shape = CircleShape,
              onClick = onOpenCamera,
              modifier = Modifier.size(54.dp),
            ) {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Rounded.CameraAlt,
                  contentDescription = stringResource(R.string.cd_camera),
                  tint = palette.onGlass,
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          Box(
            modifier =
              Modifier.width(120.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.6f))
          )
        }
      }
    }
  }
}
