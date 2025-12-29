package `is`.ru.robotcontroller

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.hardware.SensorEventListener
import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity

class GravityActivity: ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var gravitySensor: Sensor? = null
    private lateinit var gravityListener: GravityListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }


    override fun onSensorChanged(event: SensorEvent) {
        gravityListener.onGravityChanged(event.values[0], event.values[1], event.values[2])
    }


    override fun onResume() {
        super.onResume()
        gravitySensor?.also { gravity ->
            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    fun setGravityListener(listener: GravityListener) {
        this.gravityListener = listener
    }


    interface GravityListener {
        fun onGravityChanged(x: Float, y: Float, z: Float)
    }
}