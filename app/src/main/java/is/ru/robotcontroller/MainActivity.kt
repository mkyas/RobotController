package `is`.ru.robotcontroller

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import android.util.Log
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.Data
import androidx.work.Constraints
import androidx.work.NetworkType
import java.util.concurrent.TimeUnit
import java.net.DatagramSocket
import java.net.DatagramPacket
import java.net.InetAddress
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

import `is`.ru.robotcontroller.ui.theme.RobotControllerTheme

const val REQUEST_ENABLE_BT = 0

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity(), JoystickActivity.JoyStickListener, GravityActivity.GravityListener {

    var joystickId = -1

    @Serializable
    data class State(var joyX: FloatArray, var joyY: FloatArray, var gravX: Float, var gravY: Float, var gravZ: Float)

    var state = State(FloatArray(2), FloatArray(2), Float.NaN, Float.NaN, Float.NaN)

    var host = "raspberry.local"
    var port = 4444
    val period = 100L // about ten times per second, drifts
    lateinit var socket: DatagramSocket


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val joystick: JoystickActivity = findViewById<JoystickActivity>(R.id.my_joystick).also {
            it.setJoystickListener(this)
        }

        val gravity: GravityActivity = GravityActivity().also {
            it.setGravityListener(this)
        }

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        connectSocket()

        val sendWorkRequest = PeriodicWorkRequestBuilder<SendWorker>(period, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build()
            )
            .setInputData(SendWorker.buildData(socket, state))
            .build()
    }

    override fun onResume() {
        super.onResume()
        connectSocket()
    }

    override fun onPause() {
        super.onPause()
        socket.close()
    }

    fun connectSocket() {
        socket = DatagramSocket()
        var destination = InetAddress.getByName(host)
        socket.connect(destination, port)
    }

    class SendWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
        override fun doWork(): Result {
            val state = inputData.keyValueMap["state"] as State
            val socket = inputData.keyValueMap["socket"] as DatagramSocket
            val json = Json.encodeToString(state)
            val buffer = json.toByteArray()
            Log.d("app", "sending [${buffer.size} bytes]: $json")
            socket.send(DatagramPacket(buffer, buffer.size))
            return Result.success()
        }

        companion object {
            fun buildData(socket: DatagramSocket, state: State): Data {
                return Data.Builder().putAll(mapOf("socket" to socket, "state" to state)).build()
            }
        }
    }

    override fun onJoystickMoved(xPercent: Float, yPercent: Float, id: Int) {
        if(id == R.id.my_joystick) {
            state.joyX[0] = xPercent
            state.joyY[0] = yPercent
            Log.d("app", "1: x - $xPercent, y - $yPercent")
        }
        else if(id == joystickId) {
            state.joyX[1] = xPercent
            state.joyY[1] = yPercent
            Log.d("app", "2: x - $xPercent, y - $yPercent")
        }
    }

    override fun onGravityChanged(x: Float, y: Float, z: Float) {
        state.gravX = x
        state.gravY = y
        state.gravZ = z
        Log.d("app", "x= $x, y= $y, z= $z")
    }
}