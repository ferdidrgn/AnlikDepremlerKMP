package com.ferdidrgn.anlikdepremler.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Standalone, fully offline shake detector - the same single-phone accelerometer-threshold
 * approach the real shipped app "MK Earthquake Monitor" uses, not a true multi-phone
 * crowd-triangulated early-warning network (that needs a backend we don't have, and Android's
 * own OS-level Earthquake Alerts System already does it better anyway). Counts rapid high-G
 * samples within a short window before triggering, which cuts down single-bump false positives
 * (dropping the phone, a car pothole) but does not eliminate them - callers must be upfront
 * with the user that this is experimental and can false-alarm.
 */
class ShakeDetector(
    context: Context,
    private val sensitivity: Float
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var shakeCount = 0
    private var lastShakeTimestamp = 0L
    private var lastTriggerTimestamp = 0L
    private var onShakeDetected: (() -> Unit)? = null

    fun start(onShakeDetected: () -> Unit) {
        this.onShakeDetected = onShakeDetected
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        onShakeDetected = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        val gX = event.values[0] / SensorManager.GRAVITY_EARTH
        val gY = event.values[1] / SensorManager.GRAVITY_EARTH
        val gZ = event.values[2] / SensorManager.GRAVITY_EARTH
        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

        if (gForce <= sensitivity) return

        val now = System.currentTimeMillis()
        if (lastShakeTimestamp + SHAKE_SLOP_MILLIS > now) return
        if (lastShakeTimestamp + SHAKE_COUNT_RESET_MILLIS < now) {
            shakeCount = 0
        }
        lastShakeTimestamp = now
        shakeCount++

        if (shakeCount >= MIN_SHAKE_COUNT && now - lastTriggerTimestamp > TRIGGER_COOLDOWN_MILLIS) {
            lastTriggerTimestamp = now
            shakeCount = 0
            onShakeDetected?.invoke()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        /** Default sensitivity presets exposed to the Settings slider. */
        const val SENSITIVITY_LOW = 3.5f
        const val SENSITIVITY_MEDIUM = 2.7f
        const val SENSITIVITY_HIGH = 2.0f

        private const val SHAKE_SLOP_MILLIS = 50
        private const val SHAKE_COUNT_RESET_MILLIS = 3_000
        private const val MIN_SHAKE_COUNT = 4
        private const val TRIGGER_COOLDOWN_MILLIS = 20_000L
    }
}
