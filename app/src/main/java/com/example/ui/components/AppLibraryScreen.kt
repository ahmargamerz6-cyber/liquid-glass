package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppCategory
import com.example.model.CustomAppOverride
import com.example.model.IconGlassStyle
import com.example.model.InstalledApp
import com.example.ui.theme.LocalGlassPalette

@Composable
fun AppLibraryScreen(
  installedApps: List<InstalledApp>,
  overrides: Map<String, CustomAppOverride>,
  searchQuery: String,
  expandedCategoryName: String?,
  iconStyle: IconGlassStyle,
  cornerRadiusDp: Float,
  onSearchChange: (String) -> Unit,
  onToggleCategory: (String?) -> Unit,
  onLaunchApp: (InstalledApp) -> Unit,
  onAddToHome: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalGlassPalette.current
  val filteredApps =
    remember(installedApps, searchQuery) {
      if (searchQuery.isBlank()) installedApps
      else installedApps.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }
  val categorized =
    remember(installedApps) {
      AppCategory.entries
        .map { cat -> cat to installedApps.filter { it.category == cat } }
        .filter { it.second.isNotEmpty() }
    }

  Box(modifier = modifier.fillMaxSize().testTag("app_library_screen")) {
    Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
    ) {
      Text(
        text = "App Library",
        style = MaterialTheme.typography.headlineLarge,
        color = palette.onGlass,
        modifier = Modifier.padding(top = 6.dp, bottom = 10.dp),
      )

      // Liquid Glass Search Bar
      LiquidGlassSurface(
        shape = RoundedCornerShape(22.dp),
        elevated = true,
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("app_library_search"),
      ) {
        Row(
          modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = "Search apps",
            tint = palette.onGlassMuted,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (searchQuery.isEmpty()) {
              Text(
                text = "Search ${installedApps.size} installed apps",
                style = MaterialTheme.typography.bodyLarge,
                color = palette.onGlassMuted,
              )
            }
            BasicTextField(
              value = searchQuery,
              onValueChange = onSearchChange,
              singleLine = true,
              textStyle = MaterialTheme.typography.bodyLarge.copy(color = palette.onGlass),
              cursorBrush = SolidColor(palette.accent),
              modifier = Modifier.fillMaxWidth(),
            )
          }
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(32.dp)) {
              Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Clear search",
                tint = palette.onGlassMuted,
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (searchQuery.isNotBlank()) {
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(bottom = 110.dp),
        ) {
          items(filteredApps, key = { it.packageName }) { app ->
            AppLibrarySearchRow(
              app = app,
              override = overrides[app.packageName],
              onLaunch = { onLaunchApp(app) },
              onAddToHome = { onAddToHome(app.packageName) },
            )
          }
        }
      } else {
        LazyVerticalGrid(
          columns = GridCells.Fixed(2),
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
          contentPadding = PaddingValues(bottom = 110.dp),
        ) {
          items(categorized, key = { it.first.name }) { (category, apps) ->
            CategoryGlassPod(
              category = category,
              apps = apps,
              iconStyle = iconStyle,
              cornerRadiusDp = cornerRadiusDp,
              onLaunchApp = onLaunchApp,
              onExpand = { onToggleCategory(category.name) },
            )
          }
        }
      }
    }

    // Expanded Category Overlay
    val activeCategory =
      expandedCategoryName?.let { name ->
        categorized.firstOrNull { it.first.name == name }
      }
    AnimatedVisibility(
      visible = activeCategory != null,
      enter = fadeIn() + scaleIn(initialScale = 0.9f),
      exit = fadeOut() + scaleOut(targetScale = 0.9f),
    ) {
      if (activeCategory != null) {
        Box(
          modifier =
            Modifier.fillMaxSize()
              .background(Color.Black.copy(alpha = 0.52f))
              .clickable { onToggleCategory(null) }
              .padding(22.dp),
          contentAlignment = Alignment.Center,
        ) {
          LiquidGlassSurface(
            elevated = true,
            shape = RoundedCornerShape(32.dp),
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(420.dp),
          ) {
            Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = activeCategory.first.title,
                  style = MaterialTheme.typography.headlineMedium,
                  color = palette.onGlass,
                )
                IconButton(onClick = { onToggleCategory(null) }) {
                  Icon(Icons.Rounded.Close, contentDescription = "Close category", tint = palette.onGlass)
                }
              }
              Spacer(modifier = Modifier.height(12.dp))
              LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
              ) {
                items(activeCategory.second, key = { it.packageName }) { app ->
                  LiquidGlassAppIcon(
                    app = app,
                    iconSize = 56.dp,
                    cornerRadiusDp = cornerRadiusDp,
                    glassStyle = iconStyle,
                    override = overrides[app.packageName],
                    onClick = { onLaunchApp(app) },
                    onLongPress = { onAddToHome(app.packageName) },
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

@Composable
private fun CategoryGlassPod(
  category: AppCategory,
  apps: List<InstalledApp>,
  iconStyle: IconGlassStyle,
  cornerRadiusDp: Float,
  onLaunchApp: (InstalledApp) -> Unit,
  onExpand: () -> Unit,
) {
  val palette = LocalGlassPalette.current
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    LiquidGlassSurface(
      elevated = true,
      shape = RoundedCornerShape(28.dp),
      onClick = onExpand,
      modifier = Modifier.fillMaxWidth().height(164.dp),
    ) {
      Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          apps.getOrNull(0)?.let { app ->
            MiniLibraryAppTile(app = app, cornerRadiusDp = cornerRadiusDp, onClick = { onLaunchApp(app) }, modifier = Modifier.weight(1f))
          } ?: Spacer(Modifier.weight(1f))
          apps.getOrNull(1)?.let { app ->
            MiniLibraryAppTile(app = app, cornerRadiusDp = cornerRadiusDp, onClick = { onLaunchApp(app) }, modifier = Modifier.weight(1f))
          } ?: Spacer(Modifier.weight(1f))
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          apps.getOrNull(2)?.let { app ->
            MiniLibraryAppTile(app = app, cornerRadiusDp = cornerRadiusDp, onClick = { onLaunchApp(app) }, modifier = Modifier.weight(1f))
          } ?: Spacer(Modifier.weight(1f))

          // 4th quadrant: expand mini cluster
          Box(
            modifier =
              Modifier.weight(1f)
                .height(62.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.14f))
                .clickable { onExpand() }
                .padding(7.dp),
            contentAlignment = Alignment.Center,
          ) {
            val extra = apps.drop(3).take(4)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              for (r in 0..1) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  for (c in 0..1) {
                    val sub = extra.getOrNull(r * 2 + c)
                    val bmp = sub?.let { rememberDrawableBitmap(it.packageName, it.icon) }
                    Box(
                      modifier =
                        Modifier.size(18.dp)
                          .clip(RoundedCornerShape(5.dp))
                          .background(Color.White.copy(alpha = 0.16f)),
                      contentAlignment = Alignment.Center,
                    ) {
                      if (bmp != null) {
                        Image(bitmap = bmp, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
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
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = category.title,
      style = MaterialTheme.typography.labelSmall,
      color = palette.onGlass,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun MiniLibraryAppTile(
  app: InstalledApp,
  cornerRadiusDp: Float,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val bmp = rememberDrawableBitmap(app.packageName, app.icon)
  Box(
    contentAlignment = Alignment.Center,
    modifier =
      modifier
        .height(62.dp)
        .clip(RoundedCornerShape(cornerRadiusDp.coerceAtMost(16f).dp))
        .background(Color.White.copy(alpha = 0.14f))
        .border(0.8.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(cornerRadiusDp.coerceAtMost(16f).dp))
        .clickable { onClick() },
  ) {
    if (bmp != null) {
      Image(
        bitmap = bmp,
        contentDescription = app.label,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize().padding(4.dp).clip(RoundedCornerShape(12.dp)),
      )
    } else {
      Icon(Icons.Rounded.Apps, contentDescription = app.label, tint = Color.White)
    }
  }
}

@Composable
private fun AppLibrarySearchRow(
  app: InstalledApp,
  override: CustomAppOverride?,
  onLaunch: () -> Unit,
  onAddToHome: () -> Unit,
) {
  val palette = LocalGlassPalette.current
  val bmp = rememberDrawableBitmap(app.packageName, app.icon)
  LiquidGlassSurface(
    shape = RoundedCornerShape(20.dp),
    onClick = onLaunch,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier =
          Modifier.size(44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White.copy(alpha = 0.16f)),
      ) {
        if (bmp != null) {
          Image(bitmap = bmp, contentDescription = app.label, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
          Icon(Icons.Rounded.Apps, contentDescription = app.label, tint = palette.onGlass)
        }
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = override?.customLabel ?: app.label,
          style = MaterialTheme.typography.titleMedium,
          color = palette.onGlass,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = app.category.title,
          style = MaterialTheme.typography.labelSmall,
          color = palette.onGlassMuted,
        )
      }
      Box(
        contentAlignment = Alignment.Center,
        modifier =
          Modifier.size(38.dp)
            .clip(CircleShape)
            .background(palette.accent.copy(alpha = 0.25f))
            .clickable { onAddToHome() },
      ) {
        Icon(
          imageVector = Icons.Rounded.Add,
          contentDescription = "Add ${app.label} to Home Screen",
          tint = palette.onGlass,
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}
