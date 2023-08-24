package com.jmquinones.easylock

import android.Manifest
//import android.R
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.harrysoft.androidbluetoothserial.BluetoothManager
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface
import com.jmquinones.easylock.databinding.ActivityBluetoothConnectBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class BluetoothConnectActivity : AppCompatActivity() {
    //    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var binding: ActivityBluetoothConnectBinding
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var pairedDevices: Collection<BluetoothDevice>
    private lateinit var deviceInterface: SimpleBluetoothDeviceInterface
    private var flag: Boolean = true
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityBluetoothConnectBinding.inflate(layoutInflater)
        val view = binding.root
        super.onCreate(savedInstanceState)
        setContentView(view)
//        checkPermission()

        bluetoothManager = BluetoothManager.getInstance()
        if (bluetoothManager == null) {
            // Bluetooth unavailable on this device :( tell the user
            Toast.makeText(
                this@BluetoothConnectActivity,
                "Bluetooth not available.",
                Toast.LENGTH_LONG
            )
                .show() // Replace context with your context instance.
            finish()
        }
        loadPairedDevices()
        initListeners()

    }
    private fun initListeners(){
        checkPermission()
//        binding.btnBluetooth.setOnClickListener {
//            // Setup our BluetoothManager
//
//            Log.d("TEST", "TEST")
//            pairedDevices = bluetoothManager.pairedDevicesList
////            var s = ""
//            val arrayListDevice: ArrayList<String> = ArrayList<String>()
//            for (device in pairedDevices) {
////                s += "Device name: " + device.name + "Device MAC Address: " + device.address + "\n"
////                Log.d("My Bluetooth App", "Device name: " + device.name)
////                Log.d("My Bluetooth App", "Device MAC Address: " + device.address)
//                arrayListDevice.add("Dispositivo: " + device.name + "\nDireccion MAC: " + device.address)
//
//            }
//
//            val adapter = ArrayAdapter(
//                this,
//                R.layout.custom_list,
//                arrayListDevice
//            )
//
//            binding.listDeviceBluetooth.adapter = adapter
//
//        }

        binding.listDeviceBluetooth.setOnItemClickListener { _, _, i, _ ->
            if(!pairedDevices.isEmpty()){
                binding.tvSelected.text = pairedDevices.elementAt(i).name+"\n"+pairedDevices.elementAt(i).address
                connectDevice(pairedDevices.elementAt(i).address)
            }
        }
        binding.btnOn.setOnClickListener{
            if(this::deviceInterface.isInitialized){
                sendMessage("A")
            } else {
                Toast.makeText(this@BluetoothConnectActivity, "Something went wrong", Toast.LENGTH_LONG)
                    .show()
            }
        }
        binding.btnOff.setOnClickListener{
            if(this::deviceInterface.isInitialized){
                sendMessage("B")
            } else {
                Toast.makeText(this@BluetoothConnectActivity, "Something went wrong", Toast.LENGTH_LONG)
                    .show()
            }
        }

    }

    private fun loadPairedDevices(){
        checkPermission()
        Log.d("TEST", "TEST")
        pairedDevices = bluetoothManager.pairedDevicesList
//            var s = ""
        val arrayListDevice: ArrayList<String> = ArrayList<String>()
        for (device in pairedDevices) {
//                s += "Device name: " + device.name + "Device MAC Address: " + device.address + "\n"
//                Log.d("My Bluetooth App", "Device name: " + device.name)
//                Log.d("My Bluetooth App", "Device MAC Address: " + device.address)
            arrayListDevice.add("Dispositivo: " + device.name + "\nDireccion MAC: " + device.address)

        }

        val adapter = ArrayAdapter(
            this,
            R.layout.custom_list,
            arrayListDevice
        )

        binding.listDeviceBluetooth.adapter = adapter
    }
    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH), 100)
        }
    }

    private fun connectDevice(mac: String) {
        bluetoothManager.openSerialDevice(mac)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onConnected, this::onError)
    }

    private fun onConnected(connectedDevice: BluetoothSerialDevice) {
        // You are now connected to this device!
        // Here you may want to retain an instance to your device:
        deviceInterface = connectedDevice.toSimpleDeviceInterface()

        // Listen to bluetooth events
        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSent, this::onError)

        // Let's send a message:
//        if (flag){
//
//            deviceInterface.sendMessage("A")
//            flag = !flag
//        } else {
//            deviceInterface.sendMessage("B")
//            flag = !flag
//        }
    }
    private fun sendMessage(msg: String) {
        deviceInterface.sendMessage(msg)
    }

    private fun onMessageSent(message: String) {
        // We sent a message! Handle it here.
        Toast.makeText(this@BluetoothConnectActivity, "Sent a message! Message was: $message", Toast.LENGTH_LONG)
            .show() // Replace context with your context instance.
    }

    private fun onMessageReceived(message: String) {
        // We received a message! Handle it here.
        Toast.makeText(this@BluetoothConnectActivity, "Received a message! Message was: $message", Toast.LENGTH_LONG)
            .show() // Replace context with your context instance.
    }

    private fun onError(error: Throwable) {
        Toast.makeText(this@BluetoothConnectActivity, "Something went wrong", Toast.LENGTH_LONG)
            .show()
    }
}



