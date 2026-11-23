package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryGlassMode
import com.example.model.FocusNoteItem
import com.example.model.HomeWidgetModel
import com.example.model.SystemStatusSnapshot
import com.example.model.WidgetType
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalGlassPerformance
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InteractiveWidgetCard(
  widget: HomeWidgetModel,
  currentTime: String,
  currentDate: String,
  systemStatus: SystemStatusSnapshot,
  weatherIndex: Int,
  focusNotes: List<FocusNoteItem>,
  isMediaPlaying: Boolean,
  isEditMode: Boolean,
  onCycleWeather: () -> Unit,
  onToggleNote: (String) -> Unit,
  onPlayPause: () -> Unit,
  onNextTrack: () -> Unit,
  onPrevTrack: () -> Unit,
  onVolumeChange: (Int) -> Unit,
  onOpenBatteryStudio: () -> Unit,
  onRemoveWidget: () -> Unit,
  onLongPress: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    when (widget.type) {
      WidgetType.CLOCK_WORLD ->
        ClockWorldWidget(
          currentTime = currentTime,
          currentDate = currentDate,
          onLongPress = onLongPress,
          modifier = Modifier.fillMaxWidth().height(152.dp),
        )
      WidgetType.BATTERY_ECO ->
        BatteryEcoWidget(
          status = systemStatus,
          onClick = onOpenBatteryStudio,
          onLongPress = onLongPress,
          modifier = Modifier.fillMaxWidth().height(152.dp),
        )
      WidgetType.WEATHER_GLASS ->
        WeatherGlassWidget(
          conditionIndex = weatherIndex,
          onClick = onCycleWeather,
          onLongPress = onLongPress,
          modifier = Modifier.fillMaxWidth().height(152.dp),
        )
      WidgetType.CALENDAR_AGENDA ->
        CalendarAgendaWidget(
          currentDate = currentDate,
          onLongPress = onLongPress,
          modifier = Modifier.fillMaxWidth().height(152.dp),
        )
      WidgetType.FOCUS_CHECKLIST ->
        FocusChecklistWidget(
          notes = focusNotes,
          onToggleNote = onToggleNote,
          onLongPress = onLongPress,
          modifier = Modifier.fillMaxWidth().height(152.dp),
        )
      WidgetType.MEDIA_VOLUME ->
        MediaVolumeWidget(
          isPlaying = isMediaPlaying,
          volume = systemStatus.mediaVolume,
          maxVolume = systemStatus.maxMediaVolume,
          onPlayPause = onPlayPause,
          onNext = onNextTrack,
          onPrev = onPrevTrack,
          onVolumeChange = onVolumeChange,
          onLongPress = onLongPress,
          modifier = Modifier.fillMaxWidth().height(152.dp),
        )
    }

    if (isEditMode) {
      Box(
        contentAlignment = Alignment.Center,
        modifier =
          Modifier.align(Alignment.TopStart)
            .offset(x = (-6).dp, y = (-6).dp)
            .size(28.dp)
            .clip(CircleShape)
            .background(Color(0xFFE2E8F0))
            .border(1.5.dp, Color.White, CircleShape)
            .clickable { onRemoveWidget() }
            .testTag("remove_widget_${widget.id}"),
      ) {
        Icon(
          imageVector = Icons.Rounded.Close,
          contentDescription = "Remove ${widget.type.title} widget",
          tint = Color(0xFF0F172A),
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}

@Composable
private fun ClockWorldWidget(
  currentTime: String,
  currentDate: String,
  onLongPress: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalGlassPalette.current
  val cal = Calendar.getInstance()
  val hour = cal.get(Calendar.HOUR)
  val minute = cal.get(Calendar.MINUTE)

  LiquidGlassSurface(
    modifier = modifier.testTag("widget_clock"),
    elevated = true,
    onLongPress = onLongPress,
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Canvas(modifier = Modifier.size(44.dp)) {
          val radius = size.minDimension / 2f
          val center = Offset(radius, radius)
          drawCircle(
            color = Color.White.copy(alpha = 0.14f),
            radius = radius,
            center = center,
          )
          val minAngle = Math.toRadians((minute * 6 - 90).toDouble())
          val hourAngle = Math.toRadians(((hour + minute / 60f) * 30 - 90).toDouble())
          drawLine(
            color = palette.onGlass,
            start = center,
            end =
              Offset(
                center.x + (radius * 0.48f * cos(hourAngle)).toFloat(),
                center.y + (radius * 0.48f * sin(hourAngle)).toFloat(),
              ),
            strokeWidth = 4f,
            cap = StrokeCap.Round,
          )
          drawLine(
            color = palette.accent,
            start = center,
            end =
              Offset(
                center.x + (radius * 0.72f * cos(minAngle)).toFloat(),
                center.y + (radius * 0.72f * sin(minAngle)).toFloat(),
              ),
            strokeWidth = 3f,
            cap = StrokeCap.Round,
          )
          drawCircle(color = Color.White, radius = 3.5f, center = center)
        }
        Icon(
          imageVector = Icons.Rounded.Schedule,
          contentDescription = null,
          tint = palette.accent,
          modifier = Modifier.size(18.dp),
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = currentTime,
        fontFamily = JetBrainsMonoFontFamily,
        fontSize = 30.sp,
        fontWeight = FontWeight.SemiBold,
        color = palette.onGlass,
      )
      Text(
        text = currentDate.substringBefore(","),
        style = MaterialTheme.typography.labelSmall,
        color = palette.onGlassMuted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun BatteryEcoWidget(
  status: SystemStatusSnapshot,
  onClick: () -> Unit,
  onLongPress: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalGlassPalette.current
  val perf = LocalGlassPerformance.current
  val isEco = perf.effectiveMode == BatteryGlassMode.ECO_BATTERY_SAVER
  val badgeColor = if (isEco || status.isCharging) palette.ecoAccent else palette.accent

  LiquidGlassSurface(
    modifier = modifier.testTag("widget_battery_eco"),
    elevated = true,
    onClick = onClick,
    onLongPress = onLongPress,
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector =
            if (status.isCharging) Icons.Rounded.BatteryChargingFull
            else Icons.Rounded.BatteryStd,
          contentDescription = null,
          tint = badgeColor,
          modifier = Modifier.size(22.dp),
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier =
            Modifier.clip(RoundedCornerShape(10.dp))
              .background(badgeColor.copy(alpha = 0.2f))
              .padding(horizontal = 7.dp, vertical = 3.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Eco,
            contentDescription = null,
            tint = badgeColor,
            modifier = Modifier.size(12.dp),
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = if (isEco) "Eco" else "Glass",
            style = MaterialTheme.typography.labelSmall,
            color = palette.onGlass,
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "${status.batteryPercent}%",
        fontFamily = JetBrainsMonoFontFamily,
        fontSize = 30.sp,
        fontWeight = FontWeight.SemiBold,
        color = palette.onGlass,
      )
      LinearProgressIndicator(
        progress = { (status.batteryPercent / 100f).coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
        color = badgeColor,
        trackColor = Color.White.copy(alpha = 0.16f),
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "${perf.effectiveMode.title} • ${status.batteryTempCelsius}°C",
        style = MaterialTheme.typography.labelSmall,
        color = palette.onGlassMuted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun WeatherGlassWidget(
  conditionIndex: Int,
  onClick: () -> Unit,
  onLongPress: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalGlassPalette.current
  val conditions =
    listOf(
      Triple("72°", "Clear Prism Sky", "H:76°  L:59° • UV 4 Moderate"),
      Triple("64°", "Mist & Refraction", "H:68°  L:55° • Humidity 68%"),
      Triple("58°", "Twilight Breeze", "H:62°  L:51° • AQI 18 Good"),
    )
  val (temp, title, detail) = conditions[conditionIndex % conditions.size]
  val icon =
    when (conditionIndex % 3) {
      0 -> Icons.Rounded.WbSunny
      1 -> Icons.Rounded.WaterDrop
      else -> Icons.Rounded.NightsStay
    }

  LiquidGlassSurface(
    modifier = modifier.testTag("widget_weather"),
    elevated = true,
    onClick = onClick,
    onLongPress = onLongPress,
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "CUPERTINO • TAP TO CYCLE",
          style = MaterialTheme.typography.labelSmall,
          color = palette.onGlassMuted,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = temp,
          fontFamily = JetBrainsMonoFontFamily,
          fontSize = 38.sp,
          fontWeight = FontWeight.Light,
          color = palette.onGlass,
        )
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = palette.onGlass,
        )
        Text(
          text = detail,
          style = MaterialTheme.typography.labelSmall,
          color = palette.onGlassMuted,
        )
      }
      Box(
        contentAlignment = Alignment.Center,
        modifier =
          Modifier.size(64.dp)
            .clip(CircleShape)
            .background(palette.accent.copy(alpha = 0.22f))
            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = if (conditionIndex % 3 == 0) palette.warningAccent else palette.accent,
          modifier = Modifier.size(34.dp),
        )
      }
    }
  }
}

@Composable
private fun CalendarAgendaWidget(
  currentDate: String,
  onLongPress: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalGlassPalette.current
  val dayName = currentDate.substringBefore(",").uppercase()
  val dayNum = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()

  LiquidGlassSurface(
    modifier = modifier.testTag("widget_calendar"),
    elevated = true,
    onLongPress = onLongPress,
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = dayName,
          style = MaterialTheme.typography.labelLarge,
          color = palette.dangerAccent,
        )
        Icon(
          imageVector = Icons.Rounded.CalendarMonth,
          contentDescription = null,
          tint = palette.onGlassMuted,
          modifier = Modifier.size(18.dp),
        )
      }
      Text(
        text = dayNum,
        fontFamily = JetBrainsMonoFontFamily,
        fontSize = 34.sp,
        fontWeight = FontWeight.SemiBold,
        color = palette.onGlass,
      )
      Column {
        Text(
          text = "Glass UI Polish",
          style = MaterialTheme.typography.labelLarge,
          color = palette.onGlass,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = "10:30 AM • Studio",
          style = MaterialTheme.typography.labelSmall,
          color = palette.onGlassMuted,
          maxLines = 1,
        )
      }
    }
  }
}

@Composable
private fun FocusChecklistWidget(
  notes: List<FocusNoteItem>,
  onToggleNote: (String) -> Unit,
  onLongPress: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalGlassPalette.current
  LiquidGlassSurface(
    modifier = modifier.testTag("widget_focus_checklist"),
    elevated = true,
    onLongPress = onLongPress,
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "FOCUS CHECKLIST",
          style = MaterialTheme.typography.labelSmall,
          color = palette.accent,
        )
        Text(
          text = "${notes.count { it.completed }}/${notes.size} done",
          style = MaterialTheme.typography.labelSmall,
          color = palette.onGlassMuted,
        )
      }
      notes.take(3).forEach { item ->
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .clickable { onToggleNote(item.id) }
              .padding(vertical = 3.dp, horizontal = 4.dp),
        ) {
          Icon(
            imageVector =
              if (item.completed) Icons.Rounded.CheckCircle
              else Icons.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (item.completed) palette.ecoAccent else palette.onGlassMuted,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = item.text,
            style =
              MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (item.completed) TextDecoration.LineThrough else null
              ),
            color = if (item.completed) palette.onGlassMuted else palette.onGlass,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

@Composable
private fun MediaVolumeWidget(
  isPlaying: Boolean,
  volume: Int,
  maxVolume: Int,
  onPlayPause: () -> Unit,
  onNext: () -> Unit,
  onPrev: () -> Unit,
  onVolumeChange: (Int) -> Unit,
  onLongPress: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalGlassPalette.current
  LiquidGlassSurface(
    modifier = modifier.testTag("widget_media_volume"),
    elevated = true,
    onLongPress = onLongPress,
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            contentAlignment = Alignment.Center,
            modifier =
              Modifier.size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(palette.accent.copy(alpha = 0.25f)),
          ) {
            Icon(
              imageVector = Icons.Rounded.MusicNote,
              contentDescription = null,
              tint = palette.onGlass,
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = if (isPlaying) "Spatial Audio Active" else "System Media",
              style = MaterialTheme.typography.titleMedium,
              color = palette.onGlass,
            )
            Text(
              text = "Tap controls to dispatch media key",
              style = MaterialTheme.typography.labelSmall,
              color = palette.onGlassMuted,
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onPrev, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = palette.onGlass)
          }
          IconButton(onClick = onPlayPause, modifier = Modifier.size(40.dp)) {
            Icon(
              imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
              contentDescription = if (isPlaying) "Pause" else "Play",
              tint = palette.accent,
            )
          }
          IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = palette.onGlass)
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
          contentDescription = null,
          tint = palette.onGlassMuted,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Slider(
          value = volume.toFloat(),
          onValueChange = { onVolumeChange(it.toInt()) },
          valueRange = 0f..maxVolume.coerceAtLeast(1).toFloat(),
          colors =
            SliderDefaults.colors(
              thumbColor = Color.White,
              activeTrackColor = palette.accent,
              inactiveTrackColor = Color.White.copy(alpha = 0.2f),
            ),
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}
