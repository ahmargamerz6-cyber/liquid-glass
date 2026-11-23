package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.LauncherRepository
import com.example.model.AppCategory
import com.example.model.BatteryGlassMode
import com.example.model.InstalledApp
import com.example.model.ThemePack
import com.example.ui.theme.GlassThemePalettes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `app name matches Glass27`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Glass27", appName)
  }

  @Test
  fun `repository creates balanced initial workspace with dock widgets and folder`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = LauncherRepository(context)
    val fakeApps =
      (1..16).map { idx ->
        InstalledApp(
          packageName = "com.demo.app$idx",
          activityName = "MainActivity",
          label = "App $idx",
          category = AppCategory.UTILITIES,
          icon = null,
        )
      }

    val workspace = repo.createInitialWorkspace(fakeApps)
    assertEquals(4, workspace.dockPackages.size)
    assertTrue(workspace.widgets.isNotEmpty())
    assertTrue(workspace.folders.isNotEmpty())
    assertTrue(workspace.appSlots.isNotEmpty())

    repo.saveWorkspace(workspace)
    val restored = repo.workspaceFlow.first()
    assertNotNull(restored)
    assertEquals(workspace.dockPackages, restored?.dockPackages)
    assertEquals(workspace.widgets.size, restored?.widgets?.size)
  }

  @Test
  fun `theme palettes resolve all packs including adaptive dark mode`() {
    ThemePack.entries.forEach { pack ->
      val palette = GlassThemePalettes.forPack(pack = pack, darkGlass = true)
      assertTrue(palette.displayName.isNotBlank())
    }
    assertEquals(4, BatteryGlassMode.entries.size)
  }
}
