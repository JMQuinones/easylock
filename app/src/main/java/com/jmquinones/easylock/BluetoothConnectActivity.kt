package com.jmquinones.easylock

import android.Manifest
import android.annotation.SuppressLint
//import android.R
import android.bluetooth.BluetoothDevice
import android.content.Context
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
    private lateinit var MACAddress: String
    private lateinit var bluetoothModel: BluetoothModel
    private var connectionAttemptedOrMade: Boolean = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityBluetoothConnectBinding.inflate(layoutInflater)
        val view = binding.root
        super.onCreate(savedInstanceState)
        setContentView(view)
//        checkPermission()

        bluetoothModel= BluetoothModel(context = this@BluetoothConnectActivity)
        bluetoothManager = BluetoothManager.getInstance()
//        if (bluetoothManager == null) {
//            // Bluetooth unavailable on this device :( tell the user
//            Toast.makeText(
//                this@BluetoothConnectActivity,
//                "Bluetooth no displonible.",
//                Toast.LENGTH_LONG
//            )
//                .show() // Replace context with your context instance.
//            finish()
//        }
        loadPairedDevices()
        initListeners()



    }

    private fun initListeners() {
        checkPermission()

        binding.listDeviceBluetooth.setOnItemClickListener { _, _, i, _ ->
            if (!pairedDevices.isEmpty()) {
                binding.tvTitle.text = resources.getString(R.string.select_device)
                binding.tvSelected.text =
                    pairedDevices.elementAt(i).name + "\n" + pairedDevices.elementAt(i).address
                connectDevice(pairedDevices.elementAt(i).address)
            }
        }
        binding.btnOn.setOnClickListener {
            if (this::bluetoothModel.isInitialized) {
                binding.stored.text = this.openFileInput(
                    "device_address"

                ).bufferedReader().useLines { lines ->
                    lines.fold("") { some, text ->
                        "$some\n$text"
                    }
                }

                bluetoothModel.sendMessage("A")
            } else {
                Toast.makeText(
                    this@BluetoothConnectActivity,
                    "Something went wrong",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
        binding.btnOff.setOnClickListener {
            if (this::bluetoothModel.isInitialized) {
                bluetoothModel.sendMessage("B")
            } else {
                Toast.makeText(
                    this@BluetoothConnectActivity,
                    "Something went wrong",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }

    }

    private fun loadPairedDevices() {
        checkPermission()
        pairedDevices = bluetoothModel.pairedDevices
//        pairedDevices = bluetoothManager.pairedDevicesList

//            var s = ""
        val arrayListDevice: ArrayList<String> = ArrayList<String>()
        if (pairedDevices.isEmpty()) {
            binding.tvTitle.text = resources.getString(R.string.no_paired_device);
            return
        }
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

    @SuppressLint("CheckResult")
    private fun connectDevice(mac: String) {
        Log.d("MAC", mac)
        MACAddress = mac
        bluetoothModel.connectDevice(mac)
//        if (!connectionAttemptedOrMade) {
//            bluetoothManager.openSerialDevice(mac)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(this::onConnected, this::onError)
//            this.connectionAttemptedOrMade = true
//        }
    }

//    private fun onConnected(connectedDevice: BluetoothSerialDevice) {
//
//        // You are now connected to this device!
//        // Here you may want to retain an instance to your device:
//        deviceInterface = connectedDevice.toSimpleDeviceInterface()
//        MACAddress = connectedDevice.mac
//        binding.mac.text = MACAddress
//        saveMACAddress(MACAddress)
//
//        // Listen to bluetooth events
//        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSent, this::onError)
//        Toast.makeText(this@BluetoothConnectActivity, "Conectado con exito", Toast.LENGTH_LONG)
//            .show()
//
//    }

//    fun disconnect() {
//        // Check we were connected
//        if (connectionAttemptedOrMade && this::deviceInterface.isInitialized) {
//            connectionAttemptedOrMade = false
//            bluetoothManager.closeDevice(deviceInterface)
//        }
//    }

    private fun saveMACAddress(address: String) {
        val filename = "device_address"
        this.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(address.toByteArray())
        }
    }

//    private fun sendMessage(msg: String) {
//        deviceInterface.sendMessage(msg)
//    }
//
//    private fun onMessageSent(message: String) {
//        // We sent a message! Handle it here.
//        Toast.makeText(this@BluetoothConnectActivity, "Enviando mensaje...", Toast.LENGTH_LONG)
//            .show() // Replace context with your context instance.
//    }
//
//    private fun onMessageReceived(message: String) {
//        // We received a message! Handle it here.
//        Toast.makeText(
//            this@BluetoothConnectActivity,
//            "Mensaje enviado exitosamente",
//            Toast.LENGTH_LONG
//        )
//            .show() // Replace context with your context instance.
//    }
//
//    private fun onError(error: Throwable) {
//        Toast.makeText(this@BluetoothConnectActivity, "Algo salio mal", Toast.LENGTH_LONG)
//            .show()
//    }
}



