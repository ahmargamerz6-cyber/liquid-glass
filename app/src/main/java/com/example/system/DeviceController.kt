package com.example.system

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent
import com.example.model.AppCategory
import com.example.model.InstalledApp
import com.example.model.SystemStatusSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceController(private val context: Context) {
  private val packageManager: PackageManager = context.packageManager
  private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
  private val connectivityManager =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
  private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

  private var torchOnState = false

  suspend fun loadInstalledApps(): List<InstalledApp> =
    withContext(Dispatchers.IO) {
      val mainIntent =
        Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
      val resolved =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          packageManager.queryIntentActivities(
            mainIntent,
            PackageManager.ResolveInfoFlags.of(0),
          )
        } else {
          @Suppress("DEPRECATION")
          packageManager.queryIntentActivities(mainIntent, 0)
        }

      resolved
        .asSequence()
        .filter { it.activityInfo.packageName != context.packageName }
        .distinctBy { it.activityInfo.packageName }
        .map { resolveInfo ->
          val activityInfo = resolveInfo.activityInfo
          val appInfo = activityInfo.applicationInfo
          val label = resolveInfo.loadLabel(packageManager)?.toString()?.trim().orEmpty()
          val icon =
            try {
              resolveInfo.loadIcon(packageManager)
            } catch (_: Exception) {
              null
            }
          InstalledApp(
            packageName = activityInfo.packageName,
            activityName = activityInfo.name,
            label = label.ifEmpty { activityInfo.packageName.substringAfterLast('.') },
            category = classifyApp(activityInfo.packageName, label, appInfo),
            icon = icon,
          )
        }
        .sortedBy { it.label.lowercase() }
        .toList()
    }

  private fun classifyApp(
    packageName: String,
    label: String,
    appInfo: ApplicationInfo,
  ): AppCategory {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      when (appInfo.category) {
        ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAMES
        ApplicationInfo.CATEGORY_AUDIO,
        ApplicationInfo.CATEGORY_VIDEO -> return AppCategory.MEDIA
        ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.CREATIVITY
        ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.COMMUNICATION
        ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.PRODUCTIVITY
        ApplicationInfo.CATEGORY_MAPS,
        ApplicationInfo.CATEGORY_NEWS -> return AppCategory.UTILITIES
      }
    }
    val key = "${packageName.lowercase()} ${label.lowercase()}"
    return when {
      key.contains("dialer") ||
        key.contains("phone") ||
        key.contains("messaging") ||
        key.contains("sms") ||
        key.contains("chat") ||
        key.contains("mail") ||
        key.contains("contacts") ||
        key.contains("meet") -> AppCategory.COMMUNICATION
      key.contains("music") ||
        key.contains("youtube") ||
        key.contains("spotify") ||
        key.contains("media") ||
        key.contains("video") ||
        key.contains("play") -> AppCategory.MEDIA
      key.contains("camera") ||
        key.contains("photos") ||
        key.contains("gallery") ||
        key.contains("draw") -> AppCategory.CREATIVITY
      key.contains("calendar") ||
        key.contains("docs") ||
        key.contains("drive") ||
        key.contains("note") ||
        key.contains("chrome") ||
        key.contains("browser") ||
        key.contains("clock") ||
        key.contains("calculator") -> AppCategory.PRODUCTIVITY
      key.contains("game") -> AppCategory.GAMES
      else -> AppCategory.UTILITIES
    }
  }

  fun launchApp(packageName: String): Boolean {
    return try {
      val launchIntent =
        packageManager.getLaunchIntentForPackage(packageName)?.apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } ?: return false
      context.startActivity(launchIntent)
      true
    } catch (_: Exception) {
      false
    }
  }

  fun openAppInfo(packageName: String) {
    try {
      val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
          data = Uri.fromParts("package", packageName, null)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      context.startActivity(intent)
    } catch (_: Exception) {}
  }

  fun requestUninstallApp(packageName: String) {
    try {
      val intent =
        Intent(Intent.ACTION_DELETE).apply {
          data = Uri.fromParts("package", packageName, null)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      context.startActivity(intent)
    } catch (_: Exception) {}
  }

  fun openCamera() {
    try {
      val intent =
        Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      context.startActivity(intent)
    } catch (_: Exception) {
      try {
        val fallback =
          Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
        context.startActivity(fallback)
      } catch (_: Exception) {}
    }
  }

  fun openSystemSettings(action: String) {
    try {
      val intent = Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
      context.startActivity(intent)
    } catch (_: Exception) {
      try {
        val fallback =
          Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(fallback)
      } catch (_: Exception) {}
    }
  }

  fun openWifiPanel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      openSystemSettings(Settings.Panel.ACTION_WIFI)
    } else {
      openSystemSettings(Settings.ACTION_WIFI_SETTINGS)
    }
  }

  fun openDefaultLauncherSettings() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      openSystemSettings(Settings.ACTION_HOME_SETTINGS)
    } else {
      openSystemSettings(Settings.ACTION_SETTINGS)
    }
  }

  fun setMediaVolume(level: Int) {
    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val clamped = level.coerceIn(0, max)
    try {
      audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, clamped, 0)
    } catch (_: SecurityException) {}
  }

  fun stepMediaVolume(delta: Int): Int {
    val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val target = (current + delta).coerceIn(0, max)
    setMediaVolume(target)
    return target
  }

  fun dispatchMediaKey(keyCode: Int) {
    val now = SystemClock.uptimeMillis()
    try {
      audioManager.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0))
      audioManager.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0))
    } catch (_: Exception) {}
  }

  fun toggleFlashlight(): Boolean {
    val cm = cameraManager
    if (cm != null) {
      try {
        val torchCameraId =
          cm.cameraIdList.firstOrNull { id ->
            val chars = cm.getCameraCharacteristics(id)
            chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
          }
        if (torchCameraId != null) {
          val next = !torchOnState
          cm.setTorchMode(torchCameraId, next)
          torchOnState = next
          return next
        }
      } catch (_: Exception) {}
    }
    torchOnState = !torchOnState
    return torchOnState
  }

  fun applyWindowBrightness(activity: Activity?, level: Float) {
    val win = activity?.window ?: return
    val params = win.attributes
    params.screenBrightness = level.coerceIn(0.08f, 1.0f)
    win.attributes = params
  }

  fun readSystemStatus(currentBrightness: Float, orientationLocked: Boolean): SystemStatusSnapshot {
    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 85
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    val pct = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else 85
    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging =
      status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL
    val tempRaw = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 310) ?: 310
    val tempC = tempRaw / 10f

    val activeNet = connectivityManager.activeNetwork
    val caps = activeNet?.let { connectivityManager.getNetworkCapabilities(it) }
    val wifiConnected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    val cellularConnected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

    val airplaneMode =
      try {
        Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
      } catch (_: Exception) {
        false
      }

    val btEnabled = readBluetoothEnabled()

    val mediaVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    val maxMediaVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)

    val homeIntent =
      Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
    val defaultHomePkg =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.resolveActivity(homeIntent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
      } else {
        @Suppress("DEPRECATION")
        packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
      }?.activityInfo?.packageName

    return SystemStatusSnapshot(
      batteryPercent = pct,
      isCharging = isCharging,
      batteryTempCelsius = tempC,
      isAndroidPowerSave = powerManager.isPowerSaveMode,
      wifiConnected = wifiConnected,
      wifiLabel =
        when {
          wifiConnected -> "Wi-Fi Connected"
          cellularConnected -> "5G Cellular"
          else -> "Offline"
        },
      bluetoothEnabled = btEnabled,
      airplaneMode = airplaneMode,
      flashlightOn = torchOnState,
      mediaVolume = mediaVol,
      maxMediaVolume = maxMediaVol,
      brightnessLevel = currentBrightness,
      orientationLocked = orientationLocked,
      isDefaultLauncher = defaultHomePkg == context.packageName,
    )
  }

  private fun readBluetoothEnabled(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val granted =
        context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) ==
          PackageManager.PERMISSION_GRANTED
      if (!granted) return false
    }
    return try {
      val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
      bm?.adapter?.isEnabled == true
    } catch (_: SecurityException) {
      false
    }
  }
}
