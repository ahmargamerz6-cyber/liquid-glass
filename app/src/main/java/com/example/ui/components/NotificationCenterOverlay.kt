package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Tune
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.BatteryGlassMode
import com.example.model.GlassNotification
import com.example.model.SystemStatusSnapshot
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalGlassPerformance
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DynamicGlassStatusPill(
  timeFormatted: String,
  status: SystemStatusSnapshot,
  focusEnabled: Boolean,
  notificationCount: Int,
  onOpenNotifications: () -> Unit,
  onOpenControlCenter: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalGlassPalette.current
  val perf = LocalGlassPerformance.current
  val isEco = perf.effectiveMode == BatteryGlassMode.ECO_BATTERY_SAVER

  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Left Time + Notifications trigger
    LiquidGlassSurface(
      shape = CircleShape,
      onClick = onOpenNotifications,
      modifier = Modifier.height(38.dp).testTag("top_notification_pill"),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = timeFormatted,
          fontFamily = JetBrainsMonoFontFamily,
          style = MaterialTheme.typography.labelLarge,
          color = palette.onGlass,
        )
        if (notificationCount > 0 && !focusEnabled) {
          Box(
            modifier =
              Modifier.clip(CircleShape)
                .background(palette.accent.copy(alpha = 0.28f))
                .padding(horizontal = 7.dp, vertical = 2.dp)
          ) {
            Text(
              text = "$notificationCount",
              style = MaterialTheme.typography.labelSmall,
              color = palette.onGlass,
            )
          }
        }
        if (focusEnabled) {
          Icon(
            imageVector = Icons.Rounded.DoNotDisturbOn,
            contentDescription = "Focus active",
            tint = palette.secondaryAccent,
            modifier = Modifier.size(15.dp),
          )
        }
      }
    }

    // Center Dynamic Pill indicator
    LiquidGlassSurface(
      shape = CircleShape,
      elevated = true,
      tintOverride = palette.pillScrim,
      onClick = onOpenNotifications,
      modifier = Modifier.height(34.dp).testTag("dynamic_glass_island"),
    ) {
      AnimatedContent(targetState = isEco, label = "island_mode") { eco ->
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = if (eco) Icons.Rounded.Eco else Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = if (eco) palette.ecoAccent else palette.accent,
            modifier = Modifier.size(14.dp),
          )
          Text(
            text = if (eco) "Eco Glass" else "Glass 27",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
          )
        }
      }
    }

    // Right Control Center trigger pill
    LiquidGlassSurface(
      shape = CircleShape,
      onClick = onOpenControlCenter,
      modifier = Modifier.height(38.dp).testTag("top_control_center_pill"),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.Tune,
          contentDescription = "Open Control Center",
          tint = palette.onGlass,
          modifier = Modifier.size(16.dp),
        )
        if (status.isCharging) {
          Icon(
            imageVector = Icons.Rounded.BatteryChargingFull,
            contentDescription = null,
            tint = palette.ecoAccent,
            modifier = Modifier.size(15.dp),
          )
        }
        Text(
          text = "${status.batteryPercent}%",
          fontFamily = JetBrainsMonoFontFamily,
          style = MaterialTheme.typography.labelSmall,
          color = palette.onGlass,
        )
      }
    }
  }
}

@Composable
fun LiveNotificationBanner(
  notification: GlassNotification?,
  onClick: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalGlassPalette.current
  AnimatedVisibility(
    visible = notification != null,
    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
    modifier = modifier,
  ) {
    if (notification != null) {
      LiquidGlassSurface(
        shape = RoundedCornerShape(28.dp),
        elevated = true,
        tintOverride = palette.pillScrim,
        onClick = onClick,
        modifier =
          Modifier.widthIn(max = 460.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .pointerInput(notification.id) {
              detectVerticalDragGestures { _, drag ->
                if (drag < -20f) onDismiss()
              }
            }
            .testTag("live_notification_banner"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier =
              Modifier.size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(notification.accentHex).copy(alpha = 0.28f))
                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
          ) {
            Icon(
              imageVector = glyphVectorForKey(notification.glyphKey),
              contentDescription = null,
              tint = Color(notification.accentHex),
              modifier = Modifier.size(22.dp),
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = "${notification.appName} • ${notification.title}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
              )
              Text(
                text = notification.timeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = notification.message,
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White.copy(alpha = 0.85f),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}

@Composable
fun NotificationCenterOverlay(
  visible: Boolean,
  timeFormatted: String,
  dateFormatted: String,
  notifications: List<GlassNotification>,
  onDismissNotification: (String) -> Unit,
  onClearAll: () -> Unit,
  onSimulateNotification: () -> Unit,
  onClose: () -> Unit,
) {
  val palette = LocalGlassPalette.current
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(Color.Black.copy(alpha = 0.34f))
          .pointerInput(Unit) {
            detectVerticalDragGestures { _, drag ->
              if (drag < -32f) onClose()
            }
          }
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(horizontal = 18.dp, vertical = 12.dp)
          .testTag("notification_center_overlay"),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(modifier = Modifier.widthIn(max = 520.dp).fillMaxSize()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = timeFormatted,
              style = MaterialTheme.typography.displayMedium,
              color = palette.onGlass,
            )
            Text(
              text = dateFormatted,
              style = MaterialTheme.typography.bodyLarge,
              color = palette.onGlassMuted,
            )
          }
          LiquidGlassSurface(
            shape = CircleShape,
            onClick = onClose,
            modifier = Modifier.size(44.dp).testTag("nc_close_button"),
          ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Icon(Icons.Rounded.Close, contentDescription = "Close", tint = palette.onGlass)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          LiquidGlassSurface(
            shape = CircleShape,
            onClick = onSimulateNotification,
            modifier = Modifier.testTag("simulate_notification_btn"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.NotificationsActive,
                contentDescription = null,
                tint = palette.accent,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "Test Live Banner",
                style = MaterialTheme.typography.labelLarge,
                color = palette.onGlass,
              )
            }
          }

          if (notifications.isNotEmpty()) {
            LiquidGlassSurface(
              shape = CircleShape,
              onClick = onClearAll,
              modifier = Modifier.testTag("clear_notifications_btn"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.DeleteSweep,
                  contentDescription = null,
                  tint = palette.onGlassMuted,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = "Clear All",
                  style = MaterialTheme.typography.labelLarge,
                  color = palette.onGlass,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (notifications.isEmpty()) {
          LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            elevated = true,
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(28.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Icon(
                imageVector = Icons.Rounded.NotificationsActive,
                contentDescription = null,
                tint = palette.onGlassMuted,
                modifier = Modifier.size(34.dp),
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "No Pending Notifications",
                style = MaterialTheme.typography.titleMedium,
                color = palette.onGlass,
              )
              Text(
                text = "Tap 'Test Live Banner' above to preview liquid notification drop-ins.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.onGlassMuted,
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
          ) {
            items(notifications, key = { it.id }) { notif ->
              SwipeableNotificationCard(
                notification = notif,
                onDismiss = { onDismissNotification(notif.id) },
              )
            }
          }
        }

        Box(
          modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
          contentAlignment = Alignment.Center,
        ) {
          Box(
            modifier =
              Modifier.width(46.dp)
                .height(5.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.5f))
                .clickable { onClose() }
          )
        }
      }
    }
  }
}

@Composable
fun SwipeableNotificationCard(
  notification: GlassNotification,
  onDismiss: () -> Unit,
) {
  val palette = LocalGlassPalette.current
  val offsetX = remember(notification.id) { Animatable(0f) }
  val scope = rememberCoroutineScope()

  Box(
    modifier =
      Modifier.fillMaxWidth()
        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
        .pointerInput(notification.id) {
          detectHorizontalDragGestures(
            onHorizontalDrag = { _, dragAmount ->
              scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
            },
            onDragEnd = {
              scope.launch {
                if (abs(offsetX.value) > 180f) {
                  onDismiss()
                } else {
                  offsetX.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = 420f))
                }
              }
            },
          )
        }
  ) {
    LiquidGlassSurface(
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
            Modifier.size(42.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(Color(notification.accentHex).copy(alpha = 0.24f)),
        ) {
          Icon(
            imageVector = glyphVectorForKey(notification.glyphKey),
            contentDescription = null,
            tint = Color(notification.accentHex),
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = notification.title,
              style = MaterialTheme.typography.titleMedium,
              color = palette.onGlass,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f),
            )
            Text(
              text = notification.timeLabel,
              style = MaterialTheme.typography.labelSmall,
              color = palette.onGlassMuted,
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = notification.message,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.onGlassMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}
