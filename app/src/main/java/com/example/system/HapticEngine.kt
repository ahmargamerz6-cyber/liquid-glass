package com.example.system

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticEngine(context: Context) {
  private val vibrator: Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vm?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

  @Volatile
  var enabled: Boolean = true

  fun tap() {
    if (!enabled) return
    vibrate(durationMs = 12L, amplitude = 90, effectId = VibrationEffect.EFFECT_CLICK)
  }

  fun tick() {
    if (!enabled) return
    vibrate(durationMs = 6L, amplitude = 55, effectId = VibrationEffect.EFFECT_TICK)
  }

  fun heavy() {
    if (!enabled) return
    vibrate(durationMs = 24L, amplitude = 170, effectId = VibrationEffect.EFFECT_HEAVY_CLICK)
  }

  fun doubleTap() {
    if (!enabled) return
    vibrate(durationMs = 18L, amplitude = 130, effectId = VibrationEffect.EFFECT_DOUBLE_CLICK)
  }

  private fun vibrate(durationMs: Long, amplitude: Int, effectId: Int) {
    val vib = vibrator ?: return
    if (!vib.hasVibrator()) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        vib.vibrate(VibrationEffect.createPredefined(effectId))
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vib.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
      } else {
        @Suppress("DEPRECATION")
        vib.vibrate(durationMs)
      }
    } catch (_: SecurityException) {
      // Ignore if vibrator permission is unavailable on restricted test devices
    }
  }
}
