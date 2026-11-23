package com.example.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.CustomAppOverride
import com.example.model.HomeFolderModel
import com.example.model.HomeWidgetModel
import com.example.model.InstalledApp
import com.example.model.WallpaperPreset
import com.example.ui.components.AppCustomizerOverlay
import com.example.ui.components.AppLibraryScreen
import com.example.ui.components.AppMorphLaunchOverlay
import com.example.ui.components.ControlCenterOverlay
import com.example.ui.components.DynamicGlassStatusPill
import com.example.ui.components.FolderModalOverlay
import com.example.ui.components.InteractiveWidgetCard
import com.example.ui.components.LiquidGlassAppIcon
import com.example.ui.components.LiquidGlassFolderIcon
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.components.LiquidGlassVolumeHud
import com.example.ui.components.LiveNotificationBanner
import com.example.ui.components.LockScreenOverlay
import com.example.ui.components.NotificationCenterOverlay
import com.example.ui.components.ThemeStudioOverlay
import com.example.ui.components.WallpaperBackdrop
import com.example.ui.components.WidgetGalleryOverlay
import com.example.ui.theme.LocalGlassPalette
import com.example.viewmodel.LauncherUiState
import com.example.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch

@Composable
fun LauncherHomeScreen(
  uiState: LauncherUiState,
  viewModel: LauncherViewModel,
) {
  val context = LocalContext.current
  val activity = context as? Activity
  val palette = LocalGlassPalette.current
  val scope = rememberCoroutineScope()

  val photoPickerLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.PickVisualMedia(),
      onResult = { uri: Uri? ->
        if (uri != null) {
          try {
            context.contentResolver.takePersistableUriPermission(
              uri,
              Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
          } catch (_: Exception) {}
          viewModel.setWallpaperPreset(WallpaperPreset.CUSTOM_PHOTO, uri.toString())
        }
      },
    )

  val appsMap = remember(uiState.installedApps) { uiState.installedApps.associateBy { it.packageName } }
  val overridesMap =
    remember(uiState.workspace.appOverrides) {
      uiState.workspace.appOverrides.associateBy { it.packageName }
    }

  val pagerState =
    rememberPagerState(
      initialPage = uiState.currentPage.coerceIn(0, (uiState.totalPagesWithLibrary - 1).coerceAtLeast(0)),
      pageCount = { uiState.totalPagesWithLibrary.coerceAtLeast(1) },
    )

  LaunchedEffect(pagerState.currentPage) {
    if (pagerState.currentPage != uiState.currentPage) {
      viewModel.setCurrentPage(pagerState.currentPage)
    }
  }

  LaunchedEffect(uiState.currentPage) {
    if (pagerState.currentPage != uiState.currentPage && uiState.currentPage < pagerState.pageCount) {
      pagerState.animateScrollToPage(uiState.currentPage)
    }
  }

  // Back gesture handlers for active overlays/modes
  BackHandler(
    enabled =
      uiState.isControlCenterOpen ||
        uiState.isNotificationCenterOpen ||
        uiState.openFolderId != null ||
        uiState.editingAppPackage != null ||
        uiState.isWidgetPickerOpen ||
        uiState.isThemeStudioOpen ||
        uiState.isEditMode ||
        uiState.currentPage == uiState.appLibraryPageIndex
  ) {
    when {
      uiState.editingAppPackage != null -> viewModel.openAppEditor(null)
      uiState.isWidgetPickerOpen -> viewModel.setWidgetPickerOpen(false)
      uiState.isThemeStudioOpen -> viewModel.setThemeStudioOpen(false)
      uiState.openFolderId != null -> viewModel.openFolder(null)
      uiState.isControlCenterOpen -> viewModel.closeControlCenter()
      uiState.isNotificationCenterOpen -> viewModel.closeNotificationCenter()
      uiState.isEditMode -> viewModel.toggleEditMode(false)
      uiState.currentPage == uiState.appLibraryPageIndex -> {
        scope.launch { pagerState.animateScrollToPage(0) }
      }
    }
  }

  val overlayOpen =
    uiState.isControlCenterOpen ||
      uiState.isNotificationCenterOpen ||
      uiState.openFolderId != null ||
      uiState.isWidgetPickerOpen ||
      uiState.isThemeStudioOpen ||
      uiState.editingAppPackage != null ||
      uiState.isLockScreenVisible

  val unlockScale by
    animateFloatAsState(
      targetValue = if (uiState.isLockScreenVisible) 0.88f else 1f,
      animationSpec = spring(dampingRatio = 0.78f, stiffness = 320f),
      label = "home_unlock_scale",
    )

  Box(modifier = Modifier.fillMaxSize()) {
    WallpaperBackdrop(
      wallpaperPreset = uiState.settings.wallpaperPreset,
      customWallpaperUri = uiState.settings.customWallpaperUri,
      blurActive = overlayOpen || uiState.currentPage == uiState.appLibraryPageIndex,
      zoomScale =
        when {
          uiState.isControlCenterOpen || uiState.isNotificationCenterOpen -> 1.08f
          uiState.isEditMode -> 0.96f
          else -> 1.0f
        },
    )

    if (uiState.isLoading) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = palette.accent)
      }
    } else {
      // Main Home & App Library Pager
      Column(
        modifier =
          Modifier.fillMaxSize()
            .graphicsLayer {
              scaleX = unlockScale
              scaleY = unlockScale
              alpha = if (uiState.isLockScreenVisible) 0f else 1f
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
              var totalDrag = 0f
              var startX = 0f
              detectVerticalDragGestures(
                onDragStart = { offset ->
                  totalDrag = 0f
                  startX = offset.x
                },
                onVerticalDrag = { _, dragAmount ->
                  totalDrag += dragAmount
                },
                onDragEnd = {
                  if (totalDrag > 95f) {
                    if (startX > size.width * 0.52f) {
                      viewModel.openControlCenter()
                    } else {
                      viewModel.openNotificationCenter()
                    }
                  } else if (totalDrag < -110f) {
                    scope.launch {
                      pagerState.animateScrollToPage(uiState.appLibraryPageIndex)
                    }
                  }
                },
              )
            },
      ) {
        DynamicGlassStatusPill(
          timeFormatted = uiState.currentTimeFormatted,
          status = uiState.systemStatus,
          focusEnabled = uiState.settings.focusModeEnabled,
          notificationCount = uiState.notifications.size,
          onOpenNotifications = { viewModel.openNotificationCenter() },
          onOpenControlCenter = { viewModel.openControlCenter() },
        )

        // Edit Mode Action Bar
        AnimatedVisibility(
          visible = uiState.isEditMode && uiState.currentPage < uiState.appLibraryPageIndex,
          enter = fadeIn() + slideInVertically(),
          exit = fadeOut() + slideOutVertically(),
        ) {
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("edit_mode_bar"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              LiquidGlassSurface(
                shape = CircleShape,
                elevated = true,
                onClick = { viewModel.setWidgetPickerOpen(true) },
                modifier = Modifier.testTag("edit_add_widget_btn"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Icon(Icons.Rounded.Add, contentDescription = null, tint = palette.accent, modifier = Modifier.size(16.dp))
                  Text("Widget", style = MaterialTheme.typography.labelLarge, color = palette.onGlass)
                }
              }

              LiquidGlassSurface(
                shape = CircleShape,
                elevated = true,
                onClick = { viewModel.setThemeStudioOpen(true) },
                modifier = Modifier.testTag("edit_studio_btn"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Icon(Icons.Rounded.Palette, contentDescription = null, tint = palette.accent, modifier = Modifier.size(16.dp))
                  Text("Studio", style = MaterialTheme.typography.labelLarge, color = palette.onGlass)
                }
              }
            }

            LiquidGlassSurface(
              shape = CircleShape,
              elevated = true,
              tintOverride = palette.accent.copy(alpha = 0.38f),
              onClick = { viewModel.toggleEditMode(false) },
              modifier = Modifier.testTag("edit_done_btn"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text("Done", style = MaterialTheme.typography.labelLarge, color = Color.White)
              }
            }
          }
        }

        // Main Horizontal Pager (Home Pages + App Library)
        HorizontalPager(
          state = pagerState,
          modifier = Modifier.weight(1f).fillMaxWidth().testTag("home_pager"),
        ) { pageIdx ->
          if (pageIdx == uiState.appLibraryPageIndex) {
            AppLibraryScreen(
              installedApps = uiState.installedApps,
              overrides = overridesMap,
              searchQuery = uiState.appLibrarySearchQuery,
              expandedCategoryName = uiState.expandedLibraryCategory,
              iconStyle = uiState.settings.iconGlassStyle,
              cornerRadiusDp = uiState.settings.iconCornerRadiusDp,
              onSearchChange = { viewModel.updateAppLibrarySearch(it) },
              onToggleCategory = { viewModel.toggleLibraryCategory(it) },
              onLaunchApp = { viewModel.launchAppWithMorph(it) },
              onAddToHome = { viewModel.addAppToHomeScreen(it) },
            )
          } else {
            val pageWidgets =
              remember(uiState.workspace.widgets, pageIdx) {
                uiState.workspace.widgets.filter { it.pageIndex == pageIdx }
              }
            val pageFolders =
              remember(uiState.workspace.folders, pageIdx) {
                uiState.workspace.folders.filter { it.pageIndex == pageIdx }
              }
            val pageApps =
              remember(uiState.workspace.appSlots, appsMap, pageIdx) {
                uiState.workspace.appSlots
                  .filter { it.pageIndex == pageIdx }
                  .mapNotNull { slot -> appsMap[slot.packageName] }
              }

            HomeWorkspacePage(
              pageIndex = pageIdx,
              widgets = pageWidgets,
              folders = pageFolders,
              apps = pageApps,
              appsMap = appsMap,
              overridesMap = overridesMap,
              uiState = uiState,
              viewModel = viewModel,
              onOpenAppLibrary = {
                scope.launch { pagerState.animateScrollToPage(uiState.appLibraryPageIndex) }
              },
            )
          }
        }

        // Bottom Page Indicators & Floating Liquid Glass Dock (hidden on App Library)
        AnimatedVisibility(
          visible = uiState.currentPage < uiState.appLibraryPageIndex,
          enter = fadeIn(),
          exit = fadeOut(),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            // Page Indicator Dots + Quick Search / Studio Pill
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(bottom = 10.dp),
            ) {
              LiquidGlassSurface(
                shape = CircleShape,
                onClick = { viewModel.setThemeStudioOpen(true) },
                modifier = Modifier.testTag("quick_studio_pill"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                  Icon(Icons.Rounded.Tune, contentDescription = "Themes & Settings", tint = palette.onGlass, modifier = Modifier.size(14.dp))
                  Text("Themes", style = MaterialTheme.typography.labelSmall, color = palette.onGlass)
                }
              }

              LiquidGlassSurface(shape = CircleShape) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  for (i in 0 until uiState.totalPagesWithLibrary) {
                    val active = i == uiState.currentPage
                    val isLibraryDot = i == uiState.appLibraryPageIndex
                    if (isLibraryDot) {
                      Icon(
                        imageVector = Icons.Rounded.Apps,
                        contentDescription = "App Library",
                        tint = if (active) palette.accent else palette.onGlassMuted,
                        modifier =
                          Modifier.size(13.dp).clickable {
                            scope.launch { pagerState.animateScrollToPage(i) }
                          },
                      )
                    } else {
                      Box(
                        modifier =
                          Modifier.size(if (active) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (active) Color.White else Color.White.copy(alpha = 0.42f))
                            .clickable {
                              scope.launch { pagerState.animateScrollToPage(i) }
                            }
                      )
                    }
                  }
                }
              }

              LiquidGlassSurface(
                shape = CircleShape,
                onClick = { scope.launch { pagerState.animateScrollToPage(uiState.appLibraryPageIndex) } },
                modifier = Modifier.testTag("quick_search_pill"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                  Icon(Icons.Rounded.Search, contentDescription = "Search Apps", tint = palette.onGlass, modifier = Modifier.size(14.dp))
                  Text("Library", style = MaterialTheme.typography.labelSmall, color = palette.onGlass)
                }
              }
            }

            // Floating Liquid Glass Dock
            val dockApps =
              remember(uiState.workspace.dockPackages, appsMap) {
                uiState.workspace.dockPackages.mapNotNull { appsMap[it] }
              }
            LiquidGlassSurface(
              shape = RoundedCornerShape(32.dp),
              elevated = true,
              modifier =
                Modifier.widthIn(max = 460.dp)
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp)
                  .testTag("liquid_glass_dock"),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                dockApps.forEach { app ->
                  LiquidGlassAppIcon(
                    app = app,
                    iconSize = 56.dp,
                    cornerRadiusDp = uiState.settings.iconCornerRadiusDp,
                    glassStyle = uiState.settings.iconGlassStyle,
                    override = overridesMap[app.packageName],
                    showLabel = false,
                    isEditMode = uiState.isEditMode,
                    onClick = { viewModel.launchAppWithMorph(app) },
                    onLongPress = { viewModel.toggleEditMode(true) },
                    onRemoveClick =
                      if (uiState.isEditMode) {
                        { viewModel.removeAppFromHomeScreen(app.packageName) }
                      } else null,
                  )
                }
              }
            }
          }
        }
      }
    }

    // Live Drop-In Notification Banner
    LiveNotificationBanner(
      notification = uiState.activeBannerNotification,
      onClick = { viewModel.openNotificationCenter() },
      onDismiss = { viewModel.dismissActiveBanner() },
      modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 48.dp),
    )

    // Floating Liquid Glass Volume HUD
    LiquidGlassVolumeHud(
      visible = uiState.volumeHudVisible,
      volume = uiState.volumeHudLevel,
      maxVolume = uiState.volumeHudMax,
      modifier = Modifier.align(Alignment.CenterStart).padding(start = 14.dp),
    )

    // Toast-style Glass Status Pill
    AnimatedVisibility(
      visible = uiState.statusMessage != null,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 124.dp),
    ) {
      uiState.statusMessage?.let { msg ->
        LiquidGlassSurface(shape = CircleShape, elevated = true, tintOverride = palette.pillScrim) {
          Text(
            text = msg,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
          )
        }
      }
    }

    // Overlays
    ControlCenterOverlay(
      visible = uiState.isControlCenterOpen,
      settings = uiState.settings,
      status = uiState.systemStatus,
      isMediaPlaying = uiState.isMediaPlaying,
      onClose = { viewModel.closeControlCenter() },
      onOpenWifi = { viewModel.deviceController.openWifiPanel() },
      onOpenBluetooth = { viewModel.deviceController.openSystemSettings(Settings.ACTION_BLUETOOTH_SETTINGS) },
      onOpenAirplane = { viewModel.deviceController.openSystemSettings(Settings.ACTION_AIRPLANE_MODE_SETTINGS) },
      onOpenDefaultLauncher = { viewModel.deviceController.openDefaultLauncherSettings() },
      onToggleFlashlight = { viewModel.toggleFlashlight() },
      onToggleFocus = { viewModel.updateSettings { it.copy(focusModeEnabled = !it.focusModeEnabled) } },
      onToggleEcoGlass = {
        val currentlyEco = uiState.performanceConfig.effectiveMode == com.example.model.BatteryGlassMode.ECO_BATTERY_SAVER
        viewModel.setBatteryMode(
          if (currentlyEco) com.example.model.BatteryGlassMode.AUTO_ADAPTIVE
          else com.example.model.BatteryGlassMode.ECO_BATTERY_SAVER
        )
      },
      onToggleDarkGlass = { viewModel.updateSettings { it.copy(darkGlassMode = !it.darkGlassMode) } },
      onToggleOrientationLock = { viewModel.toggleOrientationLock() },
      onLockScreen = { viewModel.lockScreen() },
      onOpenThemeStudio = { viewModel.setThemeStudioOpen(true) },
      onOpenAndroidSettings = { viewModel.deviceController.openSystemSettings(Settings.ACTION_SETTINGS) },
      onBrightnessChange = { viewModel.setBrightness(activity, it) },
      onVolumeChange = { viewModel.setMediaVolumeFromSlider(it) },
      onPlayPause = { viewModel.toggleMediaPlayback() },
      onNext = { viewModel.skipMediaNext() },
      onPrev = { viewModel.skipMediaPrevious() },
    )

    NotificationCenterOverlay(
      visible = uiState.isNotificationCenterOpen,
      timeFormatted = uiState.currentTimeFormatted,
      dateFormatted = uiState.currentDateFormatted,
      notifications = uiState.notifications,
      onDismissNotification = { viewModel.dismissNotification(it) },
      onClearAll = { viewModel.clearAllNotifications() },
      onSimulateNotification = { viewModel.triggerLiveNotificationBanner() },
      onClose = { viewModel.closeNotificationCenter() },
    )

    val openFolder = uiState.workspace.folders.firstOrNull { it.id == uiState.openFolderId }
    FolderModalOverlay(
      folder = openFolder,
      appsMap = appsMap,
      overrides = overridesMap,
      iconStyle = uiState.settings.iconGlassStyle,
      cornerRadiusDp = uiState.settings.iconCornerRadiusDp,
      isEditMode = uiState.isEditMode,
      onClose = { viewModel.openFolder(null) },
      onRenameFolder = { id, title -> viewModel.renameFolder(id, title) },
      onLaunchApp = { viewModel.launchAppWithMorph(it) },
      onRemoveFromFolder = { pkg, folderId -> viewModel.removeAppFromFolder(pkg, folderId) },
    )

    WidgetGalleryOverlay(
      visible = uiState.isWidgetPickerOpen,
      onSelectWidget = { viewModel.addWidgetToCurrentPage(it) },
      onClose = { viewModel.setWidgetPickerOpen(false) },
    )

    ThemeStudioOverlay(
      visible = uiState.isThemeStudioOpen,
      settings = uiState.settings,
      status = uiState.systemStatus,
      onSelectThemePack = { viewModel.setThemePack(it) },
      onSelectIconStyle = { viewModel.setIconGlassStyle(it) },
      onSelectGridPreset = { viewModel.setGridPreset(it) },
      onSelectBatteryMode = { viewModel.setBatteryMode(it) },
      onSelectWallpaper = { viewModel.setWallpaperPreset(it) },
      onPickCustomPhoto = {
        photoPickerLauncher.launch(
          PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
      },
      onUpdateSettings = { viewModel.updateSettings(it) },
      onOpenDefaultLauncher = { viewModel.deviceController.openDefaultLauncherSettings() },
      onLockNow = { viewModel.lockScreen() },
      onClose = { viewModel.setThemeStudioOpen(false) },
    )

    val editingApp = uiState.editingAppPackage?.let { appsMap[it] }
    AppCustomizerOverlay(
      app = editingApp,
      override = editingApp?.let { overridesMap[it.packageName] },
      folders = uiState.workspace.folders,
      isInDock = editingApp?.let { uiState.workspace.dockPackages.contains(it.packageName) } == true,
      onSaveOverride = { pkg, lbl, tint, glyph -> viewModel.saveAppOverride(pkg, lbl, tint, glyph) },
      onCreateFolder = { pkg, title -> viewModel.createFolderWithApp(pkg, title) },
      onMoveToFolder = { pkg, folderId -> viewModel.moveAppToFolder(pkg, folderId) },
      onToggleDock = { viewModel.toggleAppInDock(it) },
      onRemoveFromHome = { viewModel.removeAppFromHomeScreen(it) },
      onOpenAppInfo = { viewModel.deviceController.openAppInfo(it) },
      onUninstallApp = { viewModel.deviceController.requestUninstallApp(it) },
      onClose = { viewModel.openAppEditor(null) },
    )

    LockScreenOverlay(
      visible = uiState.isLockScreenVisible,
      timeFormatted = uiState.currentTimeFormatted,
      dateFormatted = uiState.currentDateFormatted,
      status = uiState.systemStatus,
      notifications = uiState.notifications,
      onUnlock = { viewModel.unlockScreen() },
      onToggleFlashlight = { viewModel.toggleFlashlight() },
      onOpenCamera = { viewModel.openCameraShortcut() },
    )

    AppMorphLaunchOverlay(launchingApp = uiState.launchingApp)
  }
}

private sealed interface GridEntry {
  data class AppItem(val app: InstalledApp) : GridEntry
  data class FolderItem(val folder: HomeFolderModel) : GridEntry
}

@Composable
private fun HomeWorkspacePage(
  pageIndex: Int,
  widgets: List<HomeWidgetModel>,
  folders: List<HomeFolderModel>,
  apps: List<InstalledApp>,
  appsMap: Map<String, InstalledApp>,
  overridesMap: Map<String, CustomAppOverride>,
  uiState: LauncherUiState,
  viewModel: LauncherViewModel,
  onOpenAppLibrary: () -> Unit,
) {
  val columns = uiState.settings.gridPreset.columns
  val entries: List<GridEntry> =
    remember(folders, apps) {
      folders.map { GridEntry.FolderItem(it) } + apps.map { GridEntry.AppItem(it) }
    }

  val widgetRows =
    remember(widgets) {
      val rows = mutableListOf<List<HomeWidgetModel>>()
      var halfBuffer: HomeWidgetModel? = null
      for (w in widgets) {
        if (w.type.isWide) {
          if (halfBuffer != null) {
            rows.add(listOf(halfBuffer))
            halfBuffer = null
          }
          rows.add(listOf(w))
        } else {
          if (halfBuffer == null) {
            halfBuffer = w
          } else {
            rows.add(listOf(halfBuffer, w))
            halfBuffer = null
          }
        }
      }
      if (halfBuffer != null) rows.add(listOf(halfBuffer))
      rows
    }

  val appRows = remember(entries, columns) { entries.chunked(columns) }

  LazyColumn(
    modifier =
      Modifier.fillMaxSize()
        .pointerInput(uiState.isEditMode) {
          detectTapGestures(
            onLongPress = { viewModel.toggleEditMode(true) },
            onTap = {
              if (uiState.isEditMode) viewModel.toggleEditMode(false)
            },
          )
        }
        .testTag("home_page_$pageIndex"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // Widgets Section
    items(widgetRows.size, key = { idx -> widgetRows[idx].joinToString("_") { it.id } }) { idx ->
      val row = widgetRows[idx]
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        if (row.size == 1 && !row[0].type.isWide) {
          InteractiveWidgetCard(
            widget = row[0],
            currentTime = uiState.currentTimeFormatted,
            currentDate = uiState.currentDateFormatted,
            systemStatus = uiState.systemStatus,
            weatherIndex = uiState.weatherConditionIndex,
            focusNotes = uiState.workspace.focusNotes,
            isMediaPlaying = uiState.isMediaPlaying,
            isEditMode = uiState.isEditMode,
            onCycleWeather = { viewModel.cycleWeatherCondition() },
            onToggleNote = { viewModel.toggleFocusNote(it) },
            onPlayPause = { viewModel.toggleMediaPlayback() },
            onNextTrack = { viewModel.skipMediaNext() },
            onPrevTrack = { viewModel.skipMediaPrevious() },
            onVolumeChange = { viewModel.setMediaVolumeFromSlider(it) },
            onOpenBatteryStudio = { viewModel.setThemeStudioOpen(true) },
            onRemoveWidget = { viewModel.removeWidget(row[0].id) },
            onLongPress = { viewModel.toggleEditMode(true) },
            modifier = Modifier.weight(1f),
          )
          Spacer(modifier = Modifier.weight(1f))
        } else {
          row.forEach { widget ->
            InteractiveWidgetCard(
              widget = widget,
              currentTime = uiState.currentTimeFormatted,
              currentDate = uiState.currentDateFormatted,
              systemStatus = uiState.systemStatus,
              weatherIndex = uiState.weatherConditionIndex,
              focusNotes = uiState.workspace.focusNotes,
              isMediaPlaying = uiState.isMediaPlaying,
              isEditMode = uiState.isEditMode,
              onCycleWeather = { viewModel.cycleWeatherCondition() },
              onToggleNote = { viewModel.toggleFocusNote(it) },
              onPlayPause = { viewModel.toggleMediaPlayback() },
              onNextTrack = { viewModel.skipMediaNext() },
              onPrevTrack = { viewModel.skipMediaPrevious() },
              onVolumeChange = { viewModel.setMediaVolumeFromSlider(it) },
              onOpenBatteryStudio = { viewModel.setThemeStudioOpen(true) },
              onRemoveWidget = { viewModel.removeWidget(widget.id) },
              onLongPress = { viewModel.toggleEditMode(true) },
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }

    // Apps & Folders Grid Rows
    items(appRows.size, key = { idx -> "row_${pageIndex}_$idx" }) { rowIdx ->
      val rowEntries = appRows[rowIdx]
      Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        val iconSize = if (columns >= 5) 52.dp else 60.dp
        for (colIdx in 0 until columns) {
          val entry = rowEntries.getOrNull(colIdx)
          Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.TopCenter,
          ) {
            when (entry) {
              is GridEntry.FolderItem -> {
                LiquidGlassFolderIcon(
                  folder = entry.folder,
                  appsMap = appsMap,
                  iconSize = iconSize,
                  cornerRadiusDp = uiState.settings.iconCornerRadiusDp,
                  showLabel = uiState.settings.showLabels,
                  isEditMode = uiState.isEditMode,
                  onClick = { viewModel.openFolder(entry.folder.id) },
                  onLongPress = { viewModel.toggleEditMode(true) },
                )
              }
              is GridEntry.AppItem -> {
                val showDot =
                  !uiState.settings.focusModeEnabled &&
                    uiState.notifications.isNotEmpty() &&
                    colIdx == 0 &&
                    rowIdx == 0
                LiquidGlassAppIcon(
                  app = entry.app,
                  iconSize = iconSize,
                  cornerRadiusDp = uiState.settings.iconCornerRadiusDp,
                  glassStyle = uiState.settings.iconGlassStyle,
                  override = overridesMap[entry.app.packageName],
                  showLabel = uiState.settings.showLabels,
                  isEditMode = uiState.isEditMode,
                  showNotificationDot = showDot,
                  onClick = { viewModel.launchAppWithMorph(entry.app) },
                  onLongPress = { viewModel.toggleEditMode(true) },
                  onRemoveClick =
                    if (uiState.isEditMode) {
                      { viewModel.removeAppFromHomeScreen(entry.app.packageName) }
                    } else null,
                )
              }
              null -> Spacer(modifier = Modifier.size(iconSize))
            }
          }
        }
      }
    }

    if (entries.isEmpty() && widgets.isEmpty()) {
      item {
        LiquidGlassSurface(
          elevated = true,
          onClick = onOpenAppLibrary,
          modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text("Empty Glass Page", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "Long-press to add widgets or tap here to open App Library.",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White.copy(alpha = 0.78f),
            )
          }
        }
      }
    }
  }
}
