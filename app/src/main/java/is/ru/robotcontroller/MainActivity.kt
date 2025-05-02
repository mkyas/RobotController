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
import `is`.ru.robotcontroller.ui.theme.RobotControllerTheme

const val REQUEST_ENABLE_BT = 0

class MainActivity : ComponentActivity(), JoystickActivity.JoyStickListener {

    var joystickId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val joystick: JoystickActivity = findViewById<JoystickActivity>(R.id.my_joystick).also {
            it.setJoystickListener(this)
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
    }

    override fun onJoystickMoved(xPercent: Float, yPercent: Float, id: Int) {
        if(id == R.id.my_joystick) {
            Log.d("app", "1: x - $xPercent, y - $yPercent")
        }
        else if(id == joystickId) {
            Log.d("app", "2: x - $xPercent, y - $yPercent")
        }
    }
}