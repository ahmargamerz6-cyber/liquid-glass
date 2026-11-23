package com.example.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.R
import com.example.model.CustomAppOverride
import com.example.model.HomeFolderModel
import com.example.model.IconGlassStyle
import com.example.model.InstalledApp
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalGlassPerformance
import kotlin.math.abs

@Composable
fun rememberDrawableBitmap(packageName: String, drawable: Drawable?): ImageBitmap? {
  return remember(packageName, drawable) {
    drawable?.let {
      try {
        it.toBitmap(width = 144, height = 144).asImageBitmap()
      } catch (_: Exception) {
        null
      }
    }
  }
}

fun glyphVectorForKey(key: String?): ImageVector =
  when (key) {
    "sparkles" -> Icons.Rounded.AutoAwesome
    "bolt" -> Icons.Rounded.Bolt
    "chat" -> Icons.Rounded.ChatBubble
    "music" -> Icons.Rounded.MusicNote
    "camera" -> Icons.Rounded.CameraAlt
    "phone" -> Icons.Rounded.Phone
    "explore" -> Icons.Rounded.Explore
    "star" -> Icons.Rounded.Star
    "heart" -> Icons.Rounded.Favorite
    "settings" -> Icons.Rounded.Settings
    "folder" -> Icons.Rounded.Folder
    else -> Icons.Rounded.Apps
  }

val CustomGlyphOptions =
  listOf(
    null to "Default App",
    "sparkles" to "Prism",
    "bolt" to "Flash",
    "chat" to "Bubble",
    "music" to "Audio",
    "camera" to "Lens",
    "phone" to "Call",
    "explore" to "Compass",
    "star" to "Star",
    "heart" to "Heart",
    "settings" to "Gear",
  )

val CustomTintOptions: List<Pair<Long?, String>> =
  listOf(
    null to "Original",
    0xFF38BDF8L to "Sky",
    0xFF818CF8L to "Indigo",
    0xFF2DD4BFL to "Mint",
    0xFFFBBF24L to "Amber",
    0xFFFB7185L to "Coral",
    0xFFA78BFAL to "Violet",
  )

@Composable
fun LiquidGlassAppIcon(
  app: InstalledApp,
  iconSize: Dp = 60.dp,
  cornerRadiusDp: Float = 18f,
  glassStyle: IconGlassStyle = IconGlassStyle.STANDARD_GLASS,
  override: CustomAppOverride? = null,
  showLabel: Boolean = true,
  isEditMode: Boolean = false,
  showNotificationDot: Boolean = false,
  onClick: () -> Unit,
  onLongPress: () -> Unit,
  onRemoveClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  val palette = LocalGlassPalette.current
  val perf = LocalGlassPerformance.current
  var isPressed by remember { mutableStateOf(false) }

  val pressScale by
    animateFloatAsState(
      targetValue = if (isPressed) 0.9f else 1f,
      animationSpec = spring(dampingRatio = 0.68f, stiffness = 480f),
      label = "icon_press",
    )

  val jiggleDegrees =
    if (isEditMode && perf.enableJiggleMotion) {
      val phaseSeed = remember(app.packageName) { (abs(app.packageName.hashCode()) % 100) / 100f }
      val infinite = rememberInfiniteTransition(label = "jiggle")
      infinite
        .animateFloat(
          initialValue = -2.2f,
          targetValue = 2.2f,
          animationSpec =
            infiniteRepeatable(
              animation = tween(durationMillis = 150 + (phaseSeed * 45).toInt(), easing = LinearEasing),
              repeatMode = RepeatMode.Reverse,
            ),
          label = "jiggle_angle",
        )
        .value
    } else {
      0f
    }

  val displayLabel = override?.customLabel?.takeIf { it.isNotBlank() } ?: app.label
  val shape = RoundedCornerShape(cornerRadiusDp.dp)
  val bitmap = rememberDrawableBitmap(app.packageName, app.icon)

  val customTintColor = override?.tintHex?.let { Color(it) }
  val glassContainerTint =
    when {
      customTintColor != null -> customTintColor.copy(alpha = 0.36f)
      glassStyle == IconGlassStyle.DARK_SMOKED -> Color(0xAA0B1120)
      glassStyle == IconGlassStyle.FROSTED_CLEAR -> Color(0x36FFFFFF)
      glassStyle == IconGlassStyle.TINTED_CRYSTAL -> palette.accent.copy(alpha = 0.32f)
      else -> palette.surfaceTint
    }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier =
      modifier
        .graphicsLayer {
          scaleX = pressScale
          scaleY = pressScale
          rotationZ = jiggleDegrees
        }
        .pointerInput(app.packageName, isEditMode) {
          detectTapGestures(
            onPress = {
              isPressed = true
              tryAwaitRelease()
              isPressed = false
            },
            onTap = { onClick() },
            onLongPress = { onLongPress() },
          )
        }
        .testTag("app_icon_${app.packageName}"),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Box(
        contentAlignment = Alignment.Center,
        modifier =
          Modifier.size(iconSize)
            .clip(shape)
            .background(glassContainerTint)
            .border(
              width = 1.dp,
              brush =
                Brush.linearGradient(
                  colors =
                    listOf(
                      Color.White.copy(alpha = 0.72f * perf.glassRefraction),
                      palette.chromaticRimStart.copy(alpha = 0.4f),
                      Color.White.copy(alpha = 0.15f),
                    )
                ),
              shape = shape,
            ),
      ) {
        if (!override?.glyphKey.isNullOrBlank()) {
          Icon(
            imageVector = glyphVectorForKey(override?.glyphKey),
            contentDescription = stringResource(R.string.cd_app_icon, displayLabel),
            tint = customTintColor ?: palette.onGlass,
            modifier = Modifier.size(iconSize * 0.54f),
          )
        } else if (bitmap != null) {
          val iconPadding =
            when (glassStyle) {
              IconGlassStyle.FROSTED_CLEAR,
              IconGlassStyle.TINTED_CRYSTAL -> 8.dp
              IconGlassStyle.DARK_SMOKED -> 6.dp
              IconGlassStyle.STANDARD_GLASS -> 5.dp
            }
          Image(
            bitmap = bitmap,
            contentDescription = stringResource(R.string.cd_app_icon, displayLabel),
            contentScale = ContentScale.Crop,
            modifier =
              Modifier.fillMaxSize()
                .padding(iconPadding)
                .clip(RoundedCornerShape((cornerRadiusDp - 4f).coerceAtLeast(8f).dp)),
            alpha = if (glassStyle == IconGlassStyle.FROSTED_CLEAR) 0.9f else 1f,
          )
        } else {
          Icon(
            imageVector = Icons.Rounded.Apps,
            contentDescription = stringResource(R.string.cd_app_icon, displayLabel),
            tint = palette.onGlass,
            modifier = Modifier.size(iconSize * 0.52f),
          )
        }

        // Specular top gloss sheen over icon
        Box(
          modifier =
            Modifier.fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors =
                    listOf(
                      Color.White.copy(alpha = 0.22f * perf.glassRefraction),
                      Color.Transparent,
                      Color.Black.copy(alpha = 0.12f),
                    )
                )
              )
        )
      }

      if (isEditMode && onRemoveClick != null) {
        Box(
          contentAlignment = Alignment.Center,
          modifier =
            Modifier.align(Alignment.TopStart)
              .offset(x = (-6).dp, y = (-6).dp)
              .size(24.dp)
              .clip(CircleShape)
              .background(Color(0xE6E2E8F0))
              .border(1.dp, Color.White, CircleShape)
              .clickable { onRemoveClick() }
              .testTag("remove_badge_${app.packageName}"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Remove,
            contentDescription = "Remove $displayLabel",
            tint = Color(0xFF0F172A),
            modifier = Modifier.size(15.dp),
          )
        }
        Box(
          contentAlignment = Alignment.Center,
          modifier =
            Modifier.align(Alignment.TopEnd)
              .offset(x = 6.dp, y = (-6).dp)
              .size(24.dp)
              .clip(CircleShape)
              .background(palette.accent)
              .border(1.dp, Color.White, CircleShape)
              .clickable { onClick() },
        ) {
          Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Customize $displayLabel",
            tint = Color(0xFF020617),
            modifier = Modifier.size(13.dp),
          )
        }
      } else if (showNotificationDot) {
        Box(
          modifier =
            Modifier.align(Alignment.TopEnd)
              .offset(x = 3.dp, y = (-3).dp)
              .size(11.dp)
              .clip(CircleShape)
              .background(palette.dangerAccent)
              .border(1.5.dp, Color.White, CircleShape)
        )
      }
    }

    if (showLabel) {
      Spacer(modifier = Modifier.height(5.dp))
      Text(
        text = displayLabel,
        style = MaterialTheme.typography.labelSmall,
        color = palette.onGlass,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(iconSize + 10.dp),
      )
    }
  }
}

@Composable
fun LiquidGlassFolderIcon(
  folder: HomeFolderModel,
  appsMap: Map<String, InstalledApp>,
  iconSize: Dp = 60.dp,
  cornerRadiusDp: Float = 18f,
  showLabel: Boolean = true,
  isEditMode: Boolean = false,
  onClick: () -> Unit,
  onLongPress: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalGlassPalette.current
  val perf = LocalGlassPerformance.current
  val shape = RoundedCornerShape(cornerRadiusDp.dp)
  val previewApps = remember(folder.packageNames, appsMap) { folder.packageNames.mapNotNull { appsMap[it] }.take(4) }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier =
      modifier
        .pointerInput(folder.id, isEditMode) {
          detectTapGestures(
            onTap = { onClick() },
            onLongPress = { onLongPress() },
          )
        }
        .testTag("folder_icon_${folder.id}"),
  ) {
    Box(
      modifier =
        Modifier.size(iconSize)
          .clip(shape)
          .background(palette.surfaceElevatedTint)
          .border(
            width = 1.dp,
            brush =
              Brush.linearGradient(
                colors =
                  listOf(
                    Color.White.copy(alpha = 0.7f * perf.glassRefraction),
                    palette.chromaticRimStart.copy(alpha = 0.45f),
                    Color.White.copy(alpha = 0.15f),
                  )
              ),
            shape = shape,
          )
          .padding(7.dp),
      contentAlignment = Alignment.Center,
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (row in 0..1) {
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (col in 0..1) {
              val item = previewApps.getOrNull(row * 2 + col)
              val bmp = item?.let { rememberDrawableBitmap(it.packageName, it.icon) }
              Box(
                modifier =
                  Modifier.size(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
              ) {
                if (bmp != null) {
                  Image(
                    bitmap = bmp,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                  )
                }
              }
            }
          }
        }
      }
    }

    if (showLabel) {
      Spacer(modifier = Modifier.height(5.dp))
      Text(
        text = folder.title,
        style = MaterialTheme.typography.labelSmall,
        color = palette.onGlass,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(iconSize + 10.dp),
      )
    }
  }
}
