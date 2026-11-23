package com.example.viewmodel

import android.app.Activity
import android.app.Application
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LauncherRepository
import com.example.model.BatteryGlassMode
import com.example.model.CustomAppOverride
import com.example.model.FocusNoteItem
import com.example.model.GlassNotification
import com.example.model.GridLayoutPreset
import com.example.model.HomeAppSlot
import com.example.model.HomeFolderModel
import com.example.model.HomeWidgetModel
import com.example.model.IconGlassStyle
import com.example.model.InstalledApp
import com.example.model.LauncherSettings
import com.example.model.PersistedWorkspace
import com.example.model.SystemStatusSnapshot
import com.example.model.ThemePack
import com.example.model.WallpaperPreset
import com.example.model.WidgetType
import com.example.system.DeviceController
import com.example.system.HapticEngine
import com.example.ui.theme.GlassPerformanceConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class LaunchingAppState(
  val app: InstalledApp,
  val isClosing: Boolean = false,
)

data class LauncherUiState(
  val isLoading: Boolean = true,
  val settings: LauncherSettings = LauncherSettings(),
  val workspace: PersistedWorkspace = PersistedWorkspace(),
  val installedApps: List<InstalledApp> = emptyList(),
  val systemStatus: SystemStatusSnapshot = SystemStatusSnapshot(),
  val performanceConfig: GlassPerformanceConfig = GlassPerformanceConfig(),
  val currentPage: Int = 0,
  val isEditMode: Boolean = false,
  val isControlCenterOpen: Boolean = false,
  val isNotificationCenterOpen: Boolean = false,
  val isLockScreenVisible: Boolean = false,
  val unlockAnimationPulse: Int = 0,
  val openFolderId: String? = null,
  val editingAppPackage: String? = null,
  val isWidgetPickerOpen: Boolean = false,
  val isThemeStudioOpen: Boolean = false,
  val appLibrarySearchQuery: String = "",
  val expandedLibraryCategory: String? = null,
  val volumeHudVisible: Boolean = false,
  val volumeHudLevel: Int = 7,
  val volumeHudMax: Int = 15,
  val isMediaPlaying: Boolean = false,
  val currentTimeFormatted: String = "8:24",
  val currentDateFormatted: String = "Monday, November 23",
  val notifications: List<GlassNotification> = emptyList(),
  val activeBannerNotification: GlassNotification? = null,
  val launchingApp: LaunchingAppState? = null,
  val weatherConditionIndex: Int = 0,
  val statusMessage: String? = null,
) {
  val totalPagesWithLibrary: Int
    get() = workspace.pageCount + 1

  val appLibraryPageIndex: Int
    get() = workspace.pageCount
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = LauncherRepository(application)
  val deviceController = DeviceController(application)
  val haptics = HapticEngine(application)

  private val installedAppsFlow = MutableStateFlow<List<InstalledApp>>(emptyList())
  private val systemStatusFlow = MutableStateFlow(SystemStatusSnapshot())
  private val transientFlow = MutableStateFlow(TransientUiState())

  private var volumeHudJob: Job? = null
  private var bannerJob: Job? = null
  private var launchAnimJob: Job? = null
  private var statusMsgJob: Job? = null

  private data class TransientUiState(
    val isLoading: Boolean = true,
    val currentPage: Int = 0,
    val isEditMode: Boolean = false,
    val isControlCenterOpen: Boolean = false,
    val isNotificationCenterOpen: Boolean = false,
    val isLockScreenVisible: Boolean = false,
    val unlockAnimationPulse: Int = 0,
    val openFolderId: String? = null,
    val editingAppPackage: String? = null,
    val isWidgetPickerOpen: Boolean = false,
    val isThemeStudioOpen: Boolean = false,
    val appLibrarySearchQuery: String = "",
    val expandedLibraryCategory: String? = null,
    val volumeHudVisible: Boolean = false,
    val volumeHudLevel: Int = 7,
    val volumeHudMax: Int = 15,
    val isMediaPlaying: Boolean = false,
    val currentTimeFormatted: String = "",
    val currentDateFormatted: String = "",
    val notifications: List<GlassNotification> = initialNotifications(),
    val activeBannerNotification: GlassNotification? = null,
    val launchingApp: LaunchingAppState? = null,
    val weatherConditionIndex: Int = 0,
    val statusMessage: String? = null,
  )

  val uiState: StateFlow<LauncherUiState> =
    combine(
        repository.settingsFlow,
        repository.workspaceFlow,
        installedAppsFlow,
        systemStatusFlow,
        transientFlow,
      ) { settings, maybeWorkspace, apps, sys, t ->
        haptics.enabled = settings.hapticEnabled
        val workspace = maybeWorkspace ?: PersistedWorkspace()
        val perf = computePerformanceConfig(settings, sys)
        LauncherUiState(
          isLoading = t.isLoading,
          settings = settings,
          workspace = workspace,
          installedApps = apps,
          systemStatus = sys,
          performanceConfig = perf,
          currentPage = t.currentPage,
          isEditMode = t.isEditMode,
          isControlCenterOpen = t.isControlCenterOpen,
          isNotificationCenterOpen = t.isNotificationCenterOpen,
          isLockScreenVisible = t.isLockScreenVisible,
          unlockAnimationPulse = t.unlockAnimationPulse,
          openFolderId = t.openFolderId,
          editingAppPackage = t.editingAppPackage,
          isWidgetPickerOpen = t.isWidgetPickerOpen,
          isThemeStudioOpen = t.isThemeStudioOpen,
          appLibrarySearchQuery = t.appLibrarySearchQuery,
          expandedLibraryCategory = t.expandedLibraryCategory,
          volumeHudVisible = t.volumeHudVisible,
          volumeHudLevel = t.volumeHudLevel,
          volumeHudMax = t.volumeHudMax,
          isMediaPlaying = t.isMediaPlaying,
          currentTimeFormatted = t.currentTimeFormatted,
          currentDateFormatted = t.currentDateFormatted,
          notifications = t.notifications,
          activeBannerNotification = t.activeBannerNotification,
          launchingApp = t.launchingApp,
          weatherConditionIndex = t.weatherConditionIndex,
          statusMessage = t.statusMessage,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherUiState(),
      )

  init {
    refreshTime()
    initializeLauncher()
    startLowOverheadClockLoop()
  }

  private fun initializeLauncher() {
    viewModelScope.launch {
      val apps = deviceController.loadInstalledApps()
      installedAppsFlow.value = apps
      val existingWorkspace = repository.workspaceFlow.first()
      if (existingWorkspace == null) {
        val initial = repository.createInitialWorkspace(apps)
        repository.saveWorkspace(initial)
      }
      val settings = repository.settingsFlow.first()
      val status = deviceController.readSystemStatus(0.76f, false)
      systemStatusFlow.value = status
      transientFlow.update {
        it.copy(
          isLoading = false,
          isLockScreenVisible = settings.lockScreenEnabled,
          volumeHudLevel = status.mediaVolume,
          volumeHudMax = status.maxMediaVolume,
        )
      }
    }
  }

  private fun startLowOverheadClockLoop() {
    viewModelScope.launch {
      while (true) {
        refreshTime()
        // Sleep until the next minute boundary to conserve battery
        val millisToNextMinute = 60_000L - (System.currentTimeMillis() % 60_000L)
        delay(millisToNextMinute.coerceIn(5_000L, 60_000L))
      }
    }
  }

  fun refreshTime() {
    val now = Date()
    val timeFmt = SimpleDateFormat("h:mm", Locale.getDefault())
    val dateFmt = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    transientFlow.update {
      it.copy(
        currentTimeFormatted = timeFmt.format(now),
        currentDateFormatted = dateFmt.format(now),
      )
    }
  }

  fun onResumeRefresh() {
    refreshTime()
    val currentStatus = systemStatusFlow.value
    val updated =
      deviceController.readSystemStatus(
        currentBrightness = currentStatus.brightnessLevel,
        orientationLocked = currentStatus.orientationLocked,
      )
    systemStatusFlow.value = updated
    // If returning from an external launched app, play smooth close spring transition
    val currentLaunching = transientFlow.value.launchingApp
    if (currentLaunching != null) {
      launchAnimJob?.cancel()
      transientFlow.update { it.copy(launchingApp = currentLaunching.copy(isClosing = true)) }
      launchAnimJob =
        viewModelScope.launch {
          delay(260L)
          transientFlow.update { it.copy(launchingApp = null) }
        }
    }
    viewModelScope.launch {
      installedAppsFlow.value = deviceController.loadInstalledApps()
    }
  }

  private fun computePerformanceConfig(
    settings: LauncherSettings,
    status: SystemStatusSnapshot,
  ): GlassPerformanceConfig {
    val effective =
      when (settings.batteryMode) {
        BatteryGlassMode.AUTO_ADAPTIVE -> {
          if (status.isAndroidPowerSave || (status.batteryPercent <= settings.autoEcoThresholdPercent && !status.isCharging)) {
            BatteryGlassMode.ECO_BATTERY_SAVER
          } else {
            BatteryGlassMode.BALANCED_GLASS
          }
        }
        else -> settings.batteryMode
      }
    return when (effective) {
      BatteryGlassMode.ULTRA_LIQUID ->
        GlassPerformanceConfig(
          effectiveMode = effective,
          enableBlur = true,
          enableSpecularAnimation = true,
          enableJiggleMotion = true,
          glassRefraction = settings.glassRefractionStrength,
        )
      BatteryGlassMode.BALANCED_GLASS ->
        GlassPerformanceConfig(
          effectiveMode = effective,
          enableBlur = true,
          enableSpecularAnimation = false,
          enableJiggleMotion = true,
          glassRefraction = settings.glassRefractionStrength,
        )
      BatteryGlassMode.ECO_BATTERY_SAVER,
      BatteryGlassMode.AUTO_ADAPTIVE ->
        GlassPerformanceConfig(
          effectiveMode = BatteryGlassMode.ECO_BATTERY_SAVER,
          enableBlur = false,
          enableSpecularAnimation = false,
          enableJiggleMotion = false,
          glassRefraction = (settings.glassRefractionStrength * 0.6f).coerceAtLeast(0.35f),
        )
    }
  }

  // --- Navigation & Gesture Controls ---

  fun setCurrentPage(page: Int) {
    val clamped = page.coerceIn(0, uiState.value.appLibraryPageIndex)
    if (clamped != transientFlow.value.currentPage) {
      haptics.tick()
      transientFlow.update { it.copy(currentPage = clamped) }
    }
  }

  fun openControlCenter() {
    haptics.tap()
    transientFlow.update {
      it.copy(
        isControlCenterOpen = true,
        isNotificationCenterOpen = false,
        isEditMode = false,
      )
    }
  }

  fun closeControlCenter() {
    haptics.tick()
    transientFlow.update { it.copy(isControlCenterOpen = false) }
  }

  fun openNotificationCenter() {
    haptics.tap()
    transientFlow.update {
      it.copy(
        isNotificationCenterOpen = true,
        isControlCenterOpen = false,
        activeBannerNotification = null,
      )
    }
  }

  fun closeNotificationCenter() {
    haptics.tick()
    transientFlow.update { it.copy(isNotificationCenterOpen = false) }
  }

  fun lockScreen() {
    haptics.heavy()
    transientFlow.update {
      it.copy(
        isLockScreenVisible = true,
        isControlCenterOpen = false,
        isNotificationCenterOpen = false,
        isEditMode = false,
        openFolderId = null,
      )
    }
  }

  fun unlockScreen() {
    haptics.doubleTap()
    transientFlow.update {
      it.copy(
        isLockScreenVisible = false,
        unlockAnimationPulse = it.unlockAnimationPulse + 1,
      )
    }
  }

  fun toggleEditMode(enabled: Boolean? = null) {
    val next = enabled ?: !transientFlow.value.isEditMode
    if (next) haptics.heavy() else haptics.tap()
    transientFlow.update {
      it.copy(
        isEditMode = next,
        openFolderId = if (next) null else it.openFolderId,
      )
    }
  }

  fun openFolder(folderId: String?) {
    if (folderId != null) haptics.tap() else haptics.tick()
    transientFlow.update { it.copy(openFolderId = folderId) }
  }

  fun openAppEditor(packageName: String?) {
    if (packageName != null) haptics.tap()
    transientFlow.update { it.copy(editingAppPackage = packageName) }
  }

  fun setWidgetPickerOpen(open: Boolean) {
    haptics.tap()
    transientFlow.update { it.copy(isWidgetPickerOpen = open) }
  }

  fun setThemeStudioOpen(open: Boolean) {
    haptics.tap()
    transientFlow.update { it.copy(isThemeStudioOpen = open) }
  }

  fun updateAppLibrarySearch(query: String) {
    transientFlow.update { it.copy(appLibrarySearchQuery = query) }
  }

  fun toggleLibraryCategory(categoryName: String?) {
    haptics.tap()
    transientFlow.update {
      it.copy(expandedLibraryCategory = if (it.expandedLibraryCategory == categoryName) null else categoryName)
    }
  }

  // --- App Launch with Morph Transition ---

  fun launchAppWithMorph(app: InstalledApp) {
    if (transientFlow.value.isEditMode) {
      openAppEditor(app.packageName)
      return
    }
    haptics.tap()
    transientFlow.update {
      it.copy(
        openFolderId = null,
        expandedLibraryCategory = null,
        launchingApp = LaunchingAppState(app = app, isClosing = false),
      )
    }
    launchAnimJob?.cancel()
    launchAnimJob =
      viewModelScope.launch {
        delay(220L)
        val launched = deviceController.launchApp(app.packageName)
        if (!launched) {
          transientFlow.update { it.copy(launchingApp = null) }
          showStatus("Unable to open ${app.label}")
        }
      }
  }

  // --- Volume & System Controls ---

  fun handleHardwareVolumeKey(keyCode: Int): Boolean {
    val delta =
      when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_UP -> 1
        KeyEvent.KEYCODE_VOLUME_DOWN -> -1
        else -> return false
      }
    haptics.tick()
    val newVol = deviceController.stepMediaVolume(delta)
    showVolumeHud(newVol, systemStatusFlow.value.maxMediaVolume)
    return true
  }

  fun setMediaVolumeFromSlider(level: Int) {
    val max = systemStatusFlow.value.maxMediaVolume
    val clamped = level.coerceIn(0, max)
    if (clamped != systemStatusFlow.value.mediaVolume) {
      haptics.tick()
    }
    deviceController.setMediaVolume(clamped)
    showVolumeHud(clamped, max)
  }

  private fun showVolumeHud(level: Int, max: Int) {
    systemStatusFlow.update { it.copy(mediaVolume = level) }
    transientFlow.update {
      it.copy(volumeHudVisible = true, volumeHudLevel = level, volumeHudMax = max)
    }
    volumeHudJob?.cancel()
    volumeHudJob =
      viewModelScope.launch {
        delay(2200L)
        transientFlow.update { it.copy(volumeHudVisible = false) }
      }
  }

  fun setBrightness(activity: Activity?, level: Float) {
    val clamped = level.coerceIn(0.08f, 1f)
    deviceController.applyWindowBrightness(activity, clamped)
    systemStatusFlow.update { it.copy(brightnessLevel = clamped) }
  }

  fun toggleFlashlight() {
    haptics.tap()
    val next = deviceController.toggleFlashlight()
    systemStatusFlow.update { it.copy(flashlightOn = next) }
  }

  fun toggleMediaPlayback() {
    haptics.tap()
    deviceController.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    transientFlow.update { it.copy(isMediaPlaying = !it.isMediaPlaying) }
  }

  fun skipMediaNext() {
    haptics.tick()
    deviceController.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
  }

  fun skipMediaPrevious() {
    haptics.tick()
    deviceController.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
  }

  fun toggleOrientationLock() {
    haptics.tap()
    systemStatusFlow.update { it.copy(orientationLocked = !it.orientationLocked) }
  }

  fun openCameraShortcut() {
    haptics.tap()
    deviceController.openCamera()
  }

  // --- Notifications ---

  fun dismissNotification(id: String) {
    haptics.tick()
    transientFlow.update { state ->
      state.copy(
        notifications = state.notifications.filterNot { it.id == id },
        activeBannerNotification =
          if (state.activeBannerNotification?.id == id) null else state.activeBannerNotification,
      )
    }
  }

  fun clearAllNotifications() {
    haptics.heavy()
    transientFlow.update {
      it.copy(notifications = emptyList(), activeBannerNotification = null)
    }
  }

  fun triggerLiveNotificationBanner() {
    haptics.doubleTap()
    val sample =
      listOf(
        GlassNotification(
          id = UUID.randomUUID().toString(),
          appName = "Messages",
          title = "Maya Lin",
          message = "Just sent the new Liquid Glass theme swatches!",
          timeLabel = "now",
          accentHex = 0xFF34D399,
          glyphKey = "chat",
        ),
        GlassNotification(
          id = UUID.randomUUID().toString(),
          appName = "Calendar",
          title = "Design Sync in 15m",
          message = "Glass Studio Review • Room 4B",
          timeLabel = "now",
          accentHex = 0xFF38BDF8,
          glyphKey = "calendar",
          isUrgent = true,
        ),
        GlassNotification(
          id = UUID.randomUUID().toString(),
          appName = "Eco Glass",
          title = "GPU Efficiency Optimal",
          message = "Dynamic blur caching saved 14% render overhead",
          timeLabel = "now",
          accentHex = 0xFFA78BFA,
          glyphKey = "bolt",
        ),
      ).random()

    transientFlow.update {
      it.copy(
        notifications = listOf(sample) + it.notifications,
        activeBannerNotification = sample,
      )
    }
    bannerJob?.cancel()
    bannerJob =
      viewModelScope.launch {
        delay(4200L)
        transientFlow.update { it.copy(activeBannerNotification = null) }
      }
  }

  fun dismissActiveBanner() {
    haptics.tick()
    bannerJob?.cancel()
    transientFlow.update { it.copy(activeBannerNotification = null) }
  }

  // --- Settings & Theme Packs ---

  fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
    haptics.tick()
    viewModelScope.launch {
      val next = transform(uiState.value.settings)
      repository.saveSettings(next)
    }
  }

  fun setThemePack(pack: ThemePack) = updateSettings { it.copy(themePack = pack) }
  fun setIconGlassStyle(style: IconGlassStyle) = updateSettings { it.copy(iconGlassStyle = style) }
  fun setGridPreset(preset: GridLayoutPreset) = updateSettings { it.copy(gridPreset = preset) }
  fun setBatteryMode(mode: BatteryGlassMode) = updateSettings { it.copy(batteryMode = mode) }
  fun setWallpaperPreset(preset: WallpaperPreset, customUri: String? = null) =
    updateSettings {
      it.copy(
        wallpaperPreset = preset,
        customWallpaperUri = customUri ?: it.customWallpaperUri,
      )
    }

  // --- Home Screen Editing: Widgets, Apps, Folders, Notes ---

  fun addWidgetToCurrentPage(type: WidgetType) {
    haptics.tap()
    val targetPage = uiState.value.currentPage.coerceIn(0, (uiState.value.workspace.pageCount - 1).coerceAtLeast(0))
    viewModelScope.launch {
      val ws = uiState.value.workspace
      val newWidget =
        HomeWidgetModel(
          id = "w_${type.name}_${System.currentTimeMillis()}",
          type = type,
          pageIndex = targetPage,
        )
      repository.saveWorkspace(ws.copy(widgets = ws.widgets + newWidget))
      transientFlow.update { it.copy(isWidgetPickerOpen = false) }
      showStatus("Added ${type.title} widget")
    }
  }

  fun removeWidget(widgetId: String) {
    haptics.heavy()
    viewModelScope.launch {
      val ws = uiState.value.workspace
      repository.saveWorkspace(ws.copy(widgets = ws.widgets.filterNot { it.id == widgetId }))
    }
  }

  fun removeAppFromHomeScreen(packageName: String) {
    haptics.heavy()
    viewModelScope.launch {
      val ws = uiState.value.workspace
      val updatedFolders =
        ws.folders
          .map { folder -> folder.copy(packageNames = folder.packageNames.filterNot { it == packageName }) }
          .filter { it.packageNames.isNotEmpty() }
      repository.saveWorkspace(
        ws.copy(
          appSlots = ws.appSlots.filterNot { it.packageName == packageName },
          dockPackages = ws.dockPackages.filterNot { it == packageName },
          folders = updatedFolders,
        )
      )
      transientFlow.update { it.copy(editingAppPackage = null) }
      showStatus("Removed from Home Screen (still in App Library)")
    }
  }

  fun addAppToHomeScreen(packageName: String) {
    haptics.tap()
    val targetPage = 0
    viewModelScope.launch {
      val ws = uiState.value.workspace
      if (ws.appSlots.any { it.packageName == packageName } || ws.dockPackages.contains(packageName)) {
        showStatus("Already on Home Screen")
        return@launch
      }
      val newSlot =
        HomeAppSlot(
          id = "slot_${System.currentTimeMillis()}_$packageName",
          packageName = packageName,
          pageIndex = targetPage,
        )
      repository.saveWorkspace(ws.copy(appSlots = ws.appSlots + newSlot))
      transientFlow.update { it.copy(currentPage = targetPage) }
      showStatus("Added to Home Screen")
    }
  }

  fun toggleAppInDock(packageName: String) {
    haptics.tap()
    viewModelScope.launch {
      val ws = uiState.value.workspace
      val updatedDock =
        if (ws.dockPackages.contains(packageName)) {
          ws.dockPackages.filterNot { it == packageName }
        } else {
          (ws.dockPackages + packageName).takeLast(4)
        }
      repository.saveWorkspace(ws.copy(dockPackages = updatedDock))
    }
  }

  fun createFolderWithApp(packageName: String, folderName: String) {
    haptics.tap()
    val targetPage = uiState.value.currentPage.coerceIn(0, (uiState.value.workspace.pageCount - 1).coerceAtLeast(0))
    viewModelScope.launch {
      val ws = uiState.value.workspace
      val newFolder =
        HomeFolderModel(
          id = "folder_${System.currentTimeMillis()}",
          title = folderName.ifBlank { "Folder" },
          packageNames = listOf(packageName),
          pageIndex = targetPage,
        )
      repository.saveWorkspace(
        ws.copy(
          appSlots = ws.appSlots.filterNot { it.packageName == packageName },
          folders = ws.folders + newFolder,
        )
      )
      transientFlow.update { it.copy(editingAppPackage = null) }
    }
  }

  fun moveAppToFolder(packageName: String, folderId: String) {
    haptics.tap()
    viewModelScope.launch {
      val ws = uiState.value.workspace
      val updatedFolders =
        ws.folders.map { folder ->
          val cleaned = folder.packageNames.filterNot { it == packageName }
          if (folder.id == folderId) folder.copy(packageNames = cleaned + packageName)
          else folder.copy(packageNames = cleaned)
        }
      repository.saveWorkspace(
        ws.copy(
          appSlots = ws.appSlots.filterNot { it.packageName == packageName },
          folders = updatedFolders.filter { it.packageNames.isNotEmpty() },
        )
      )
      transientFlow.update { it.copy(editingAppPackage = null) }
    }
  }

  fun removeAppFromFolder(packageName: String, folderId: String) {
    haptics.tap()
    viewModelScope.launch {
      val ws = uiState.value.workspace
      val folder = ws.folders.firstOrNull { it.id == folderId } ?: return@launch
      val updatedFolders =
        ws.folders
          .map { f ->
            if (f.id == folderId) f.copy(packageNames = f.packageNames.filterNot { it == packageName })
            else f
          }
          .filter { it.packageNames.isNotEmpty() }
      val restoredSlot =
        HomeAppSlot(
          id = "slot_${System.currentTimeMillis()}_$packageName",
          packageName = packageName,
          pageIndex = folder.pageIndex,
        )
      repository.saveWorkspace(
        ws.copy(
          folders = updatedFolders,
          appSlots = ws.appSlots + restoredSlot,
        )
      )
      if (updatedFolders.none { it.id == folderId }) {
        transientFlow.update { it.copy(openFolderId = null) }
      }
    }
  }

  fun renameFolder(folderId: String, newTitle: String) {
    viewModelScope.launch {
      val ws = uiState.value.workspace
      repository.saveWorkspace(
        ws.copy(
          folders =
            ws.folders.map { if (it.id == folderId) it.copy(title = newTitle.ifBlank { "Folder" }) else it }
        )
      )
    }
  }

  fun saveAppOverride(packageName: String, customLabel: String?, tintHex: Long?, glyphKey: String?) {
    haptics.tap()
    viewModelScope.launch {
      val ws = uiState.value.workspace
      val filtered = ws.appOverrides.filterNot { it.packageName == packageName }
      val nextOverrides =
        if (customLabel.isNullOrBlank() && tintHex == null && glyphKey == null) {
          filtered
        } else {
          filtered +
            CustomAppOverride(
              packageName = packageName,
              customLabel = customLabel?.takeIf { it.isNotBlank() },
              tintHex = tintHex,
              glyphKey = glyphKey,
            )
        }
      repository.saveWorkspace(ws.copy(appOverrides = nextOverrides))
      transientFlow.update { it.copy(editingAppPackage = null) }
    }
  }

  fun toggleFocusNote(noteId: String) {
    haptics.tick()
    viewModelScope.launch {
      val ws = uiState.value.workspace
      repository.saveWorkspace(
        ws.copy(
          focusNotes =
            ws.focusNotes.map { if (it.id == noteId) it.copy(completed = !it.completed) else it }
        )
      )
    }
  }

  fun addFocusNote(text: String) {
    if (text.isBlank()) return
    haptics.tap()
    viewModelScope.launch {
      val ws = uiState.value.workspace
      val newNote = FocusNoteItem(id = "note_${System.currentTimeMillis()}", text = text.trim())
      repository.saveWorkspace(ws.copy(focusNotes = listOf(newNote) + ws.focusNotes.take(4)))
    }
  }

  fun cycleWeatherCondition() {
    haptics.tick()
    transientFlow.update { it.copy(weatherConditionIndex = (it.weatherConditionIndex + 1) % 3) }
  }

  fun addHomePage() {
    haptics.tap()
    viewModelScope.launch {
      val ws = uiState.value.workspace
      if (ws.pageCount < 4) {
        val nextCount = ws.pageCount + 1
        repository.saveWorkspace(ws.copy(pageCount = nextCount))
        transientFlow.update { it.copy(currentPage = nextCount - 1) }
      }
    }
  }

  private fun showStatus(msg: String) {
    transientFlow.update { it.copy(statusMessage = msg) }
    statusMsgJob?.cancel()
    statusMsgJob =
      viewModelScope.launch {
        delay(2400L)
        transientFlow.update { it.copy(statusMessage = null) }
      }
  }

  companion object {
    private fun initialNotifications(): List<GlassNotification> =
      listOf(
        GlassNotification(
          id = "notif_welcome",
          appName = "Glass27",
          title = "Liquid Glass Engine Active",
          message = "Swipe down top-right for Control Center, or long-press Home to edit widgets & folders.",
          timeLabel = "2m ago",
          accentHex = 0xFF38BDF8,
          glyphKey = "sparkles",
        ),
        GlassNotification(
          id = "notif_eco",
          appName = "Battery Optimizer",
          title = "Auto-Adaptive Glass Ready",
          message = "Hardware layer caching enabled. Auto Eco Glass engages below 20%.",
          timeLabel = "12m ago",
          accentHex = 0xFF34D399,
          glyphKey = "bolt",
        ),
      )
  }
}
