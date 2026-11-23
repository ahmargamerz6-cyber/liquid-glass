package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.LauncherHomeScreen
import com.example.ui.theme.Glass27Theme
import com.example.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: LauncherViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      Glass27Theme(
        themePack = uiState.settings.themePack,
        darkGlass = uiState.settings.darkGlassMode,
        performanceConfig = uiState.performanceConfig,
      ) {
        LauncherHomeScreen(uiState = uiState, viewModel = viewModel)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.onResumeRefresh()
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      if (viewModel.handleHardwareVolumeKey(keyCode)) {
        return true
      }
    }
    return super.onKeyDown(keyCode, event)
  }
}
