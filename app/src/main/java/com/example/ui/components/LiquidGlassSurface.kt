package com.example.ui.components

import android.net.Uri
import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.WallpaperPreset
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalGlassPerformance

@Composable
fun LiquidGlassSurface(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(26.dp),
  elevated: Boolean = false,
  tintOverride: Color? = null,
  borderWidth: Dp = 1.dp,
  onClick: (() -> Unit)? = null,
  onLongPress: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit,
) {
  val palette = LocalGlassPalette.current
  val perf = LocalGlassPerformance.current
  var isPressed by remember { mutableStateOf(false) }

  val scale by
    animateFloatAsState(
      targetValue = if (isPressed && onClick != null) 0.96f else 1f,
      animationSpec = spring(dampingRatio = 0.72f, stiffness = 460f),
      label = "glass_press_scale",
    )

  val sheenShift =
    if (perf.enableSpecularAnimation) {
      val infinite = rememberInfiniteTransition(label = "glass_sheen")
      infinite
        .animateFloat(
          initialValue = 0f,
          targetValue = 1f,
          animationSpec =
            infiniteRepeatable(
              animation = tween(durationMillis = 5200, easing = FastOutSlowInEasing),
              repeatMode = RepeatMode.Reverse,
            ),
          label = "sheen_progress",
        )
        .value
    } else {
      0.25f
    }

  val fillBase =
    tintOverride ?: if (elevated) palette.surfaceElevatedTint else palette.surfaceTint
  val refraction = perf.glassRefraction.coerceIn(0.25f, 1f)

  val rimBrush =
    Brush.linearGradient(
      colors =
        listOf(
          palette.specularHighlight.copy(alpha = 0.65f * refraction),
          palette.chromaticRimStart.copy(alpha = 0.42f * refraction),
          palette.chromaticRimEnd.copy(alpha = 0.25f * refraction),
          Color.White.copy(alpha = 0.14f * refraction),
        ),
      start = Offset(0f, 0f),
      end = Offset(600f + (sheenShift * 260f), 600f),
    )

  val interactionModifier =
    if (onClick != null || onLongPress != null) {
      Modifier.pointerInput(onClick, onLongPress) {
        detectTapGestures(
          onPress = {
            isPressed = true
            tryAwaitRelease()
            isPressed = false
          },
          onTap = { onClick?.invoke() },
          onLongPress = { onLongPress?.invoke() },
        )
      }
    } else {
      Modifier
    }

  Box(
    modifier =
      modifier
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        }
        .clip(shape)
        .background(fillBase)
        .drawBehind {
          // Subtle inner top specular reflection gradient
          drawRect(
            brush =
              Brush.verticalGradient(
                colors =
                  listOf(
                    Color.White.copy(alpha = 0.18f * refraction),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.08f),
                  )
              )
          )
        }
        .border(width = borderWidth, brush = rimBrush, shape = shape)
        .then(interactionModifier),
    content = content,
  )
}

@Composable
fun WallpaperBackdrop(
  wallpaperPreset: WallpaperPreset,
  customWallpaperUri: String?,
  blurActive: Boolean,
  zoomScale: Float,
  dimAlpha: Float = 0.12f,
  modifier: Modifier = Modifier,
) {
  val perf = LocalGlassPerformance.current
  val animatedZoom by
    animateFloatAsState(
      targetValue = zoomScale,
      animationSpec = spring(dampingRatio = 0.85f, stiffness = 260f),
      label = "wallpaper_zoom",
    )
  val animatedBlur by
    animateFloatAsState(
      targetValue = if (blurActive && perf.enableBlur) 22f else 0f,
      animationSpec = tween(durationMillis = 240),
      label = "wallpaper_blur",
    )

  val blurModifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && animatedBlur > 0.5f) {
      Modifier.blur(animatedBlur.dp)
    } else {
      Modifier
    }

  Box(modifier = modifier.fillMaxSize()) {
    val imageModifier =
      Modifier.fillMaxSize()
        .graphicsLayer {
          scaleX = animatedZoom
          scaleY = animatedZoom
        }
        .then(blurModifier)

    if (wallpaperPreset == WallpaperPreset.CUSTOM_PHOTO && !customWallpaperUri.isNullOrBlank()) {
      AsyncImage(
        model = Uri.parse(customWallpaperUri),
        contentDescription = "Custom wallpaper",
        contentScale = ContentScale.Crop,
        modifier = imageModifier,
      )
    } else {
      val resId =
        when (wallpaperPreset) {
          WallpaperPreset.PRISM -> R.drawable.img_wallpaper_prism
          WallpaperPreset.AURORA -> R.drawable.img_wallpaper_aurora
          WallpaperPreset.AMBER -> R.drawable.img_wallpaper_amber
          WallpaperPreset.CUSTOM_PHOTO -> R.drawable.img_wallpaper_prism
        }
      Image(
        painter = painterResource(id = resId),
        contentDescription = "Liquid glass wallpaper",
        contentScale = ContentScale.Crop,
        modifier = imageModifier,
      )
    }

    // Subtle legibility scrim + extra fallback dim when blur is disabled in Eco mode
    val effectiveDim =
      if (blurActive && (!perf.enableBlur || Build.VERSION.SDK_INT < Build.VERSION_CODES.S)) {
        (dimAlpha + 0.34f).coerceAtMost(0.68f)
      } else {
        dimAlpha
      }
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors =
                listOf(
                  Color.Black.copy(alpha = effectiveDim + 0.08f),
                  Color.Black.copy(alpha = effectiveDim * 0.5f),
                  Color.Black.copy(alpha = effectiveDim + 0.14f),
                )
            )
          )
    )
  }
}
