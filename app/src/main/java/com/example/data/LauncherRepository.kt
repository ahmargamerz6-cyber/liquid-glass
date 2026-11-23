package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.FocusNoteItem
import com.example.model.HomeAppSlot
import com.example.model.HomeFolderModel
import com.example.model.HomeWidgetModel
import com.example.model.InstalledApp
import com.example.model.LauncherSettings
import com.example.model.PersistedWorkspace
import com.example.model.WidgetType
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "glass27_launcher_prefs")

class LauncherRepository(private val context: Context) {
  private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  private val settingsAdapter = moshi.adapter(LauncherSettings::class.java)
  private val workspaceAdapter = moshi.adapter(PersistedWorkspace::class.java)

  private object Keys {
    val SETTINGS_JSON = stringPreferencesKey("settings_json")
    val WORKSPACE_JSON = stringPreferencesKey("workspace_json")
  }

  val settingsFlow: Flow<LauncherSettings> =
    context.launcherDataStore.data.map { prefs ->
      val raw = prefs[Keys.SETTINGS_JSON]
      if (raw.isNullOrBlank()) {
        LauncherSettings()
      } else {
        try {
          settingsAdapter.fromJson(raw) ?: LauncherSettings()
        } catch (_: Exception) {
          LauncherSettings()
        }
      }
    }

  val workspaceFlow: Flow<PersistedWorkspace?> =
    context.launcherDataStore.data.map { prefs ->
      val raw = prefs[Keys.WORKSPACE_JSON]
      if (raw.isNullOrBlank()) {
        null
      } else {
        try {
          workspaceAdapter.fromJson(raw)
        } catch (_: Exception) {
          null
        }
      }
    }

  suspend fun saveSettings(settings: LauncherSettings) {
    context.launcherDataStore.edit { prefs ->
      prefs[Keys.SETTINGS_JSON] = settingsAdapter.toJson(settings)
    }
  }

  suspend fun saveWorkspace(workspace: PersistedWorkspace) {
    context.launcherDataStore.edit { prefs ->
      prefs[Keys.WORKSPACE_JSON] = workspaceAdapter.toJson(workspace)
    }
  }

  fun createInitialWorkspace(installedApps: List<InstalledApp>): PersistedWorkspace {
    val dockCandidates =
      listOf("dialer", "phone", "messaging", "messages", "chrome", "browser", "camera", "settings")
    val dockApps = mutableListOf<InstalledApp>()
    for (hint in dockCandidates) {
      if (dockApps.size >= 4) break
      val found =
        installedApps.firstOrNull { app ->
          app !in dockApps &&
            (app.packageName.lowercase().contains(hint) || app.label.lowercase().contains(hint))
        }
      if (found != null) dockApps.add(found)
    }
    for (app in installedApps) {
      if (dockApps.size >= 4) break
      if (app !in dockApps) dockApps.add(app)
    }

    val dockSet = dockApps.map { it.packageName }.toSet()
    val remaining = installedApps.filter { it.packageName !in dockSet }

    val page0Apps = remaining.take(8)
    val folderApps = remaining.drop(8).take(4)
    val page1Apps = remaining.drop(12).take(12)

    val widgets =
      listOf(
        HomeWidgetModel(id = "w_clock_0", type = WidgetType.CLOCK_WORLD, pageIndex = 0),
        HomeWidgetModel(id = "w_battery_0", type = WidgetType.BATTERY_ECO, pageIndex = 0),
        HomeWidgetModel(id = "w_weather_1", type = WidgetType.WEATHER_GLASS, pageIndex = 1),
        HomeWidgetModel(id = "w_focus_1", type = WidgetType.FOCUS_CHECKLIST, pageIndex = 1),
      )

    val folders =
      if (folderApps.size >= 2) {
        listOf(
          HomeFolderModel(
            id = "folder_essentials",
            title = "Essentials",
            packageNames = folderApps.map { it.packageName },
            pageIndex = 0,
          )
        )
      } else {
        emptyList()
      }

    val slots =
      page0Apps.mapIndexed { idx, app ->
        HomeAppSlot(id = "slot_0_${idx}_${app.packageName}", packageName = app.packageName, pageIndex = 0)
      } +
        page1Apps.mapIndexed { idx, app ->
          HomeAppSlot(id = "slot_1_${idx}_${app.packageName}", packageName = app.packageName, pageIndex = 1)
        }

    val defaultNotes =
      listOf(
        FocusNoteItem(id = "n1", text = "Review daily schedule", completed = false),
        FocusNoteItem(id = "n2", text = "Enable Eco Glass under 20%", completed = true),
        FocusNoteItem(id = "n3", text = "Customize theme & grid", completed = false),
      )

    return PersistedWorkspace(
      pageCount = 2,
      dockPackages = dockApps.map { it.packageName },
      appSlots = slots,
      folders = folders,
      widgets = widgets,
      appOverrides = emptyList(),
      focusNotes = defaultNotes,
    )
  }
}
