package com.jmquinones.easylock

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.harrysoft.androidbluetoothserial.BluetoothManager
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class BluetoothModel(var MACAddress: String = "", val context: Context) {
    var bluetoothManager: BluetoothManager = BluetoothManager.getInstance()
    lateinit var pairedDevices: Collection<BluetoothDevice>
    lateinit var deviceInterface: SimpleBluetoothDeviceInterface
    var connectionAttemptedOrMade: Boolean = false

    init {
        //        if (bluetoothManager == null) {
//            // Bluetooth unavailable on this device :( tell the user
//            Toast.makeText(
//                this@BluetoothConnectActivity,
//                "Bluetooth no displonible.",
//                Toast.LENGTH_LONG
//            )
//                .show() // Replace context with your context instance.
//        }
        loadPairedDevices()
    }

    private fun loadPairedDevices() {
        pairedDevices = bluetoothManager.pairedDevicesList
    }

    @SuppressLint("CheckResult")
    public fun connectDevice(mac: String) {
        if (!connectionAttemptedOrMade) {
            bluetoothManager.openSerialDevice(mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnected, this::onError)
            this.connectionAttemptedOrMade = true
        }
    }

    @SuppressLint("CheckResult")
    public fun connectDeviceAndOpen(mac: String) {

        bluetoothManager.openSerialDevice(mac)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onConnectedAndOpen, this::onError)

    }

    private fun onConnected(connectedDevice: BluetoothSerialDevice) {

        // You are now connected to this device!
        // Here you may want to retain an instance to your device:
        deviceInterface = connectedDevice.toSimpleDeviceInterface()
        MACAddress = connectedDevice.mac
//        binding.mac.text = MACAddress
        saveMACAddress(MACAddress)

        // Listen to bluetooth events
        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSent, this::onError)
        Toast.makeText(context, "Conectado con exito", Toast.LENGTH_LONG)
            .show()

    }

    private fun onConnectedAndOpen(connectedDevice: BluetoothSerialDevice) {

        // You are now connected to this device!
        // Here you may want to retain an instance to your device:
        deviceInterface = connectedDevice.toSimpleDeviceInterface()
        MACAddress = connectedDevice.mac
//        binding.mac.text = MACAddress
        // Listen to bluetooth events
        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSent, this::onError)
        Toast.makeText(context, "Conectado con exito", Toast.LENGTH_LONG)
            .show()
        sendMessage("A")

//        disconnect(MACAddress)

    }

    fun disconnect(mac: String) {
        // Check we were connected
//        if (connectionAttemptedOrMade && this::deviceInterface.isInitialized) {
//            Log.i("Disconnect", "Disconnect")
//            connectionAttemptedOrMade = false
//            bluetoothManager.closeDevice(deviceInterface)
//        }
//        Log.i("Disconnect", "Disconnect")
        bluetoothManager.closeDevice(mac)
        bluetoothManager.close()
    }

    private fun saveMACAddress(address: String) {
        val filename = "device_address"
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(address.toByteArray())
        }
    }

    fun sendMessage(msg: String) {
        deviceInterface.sendMessage(msg)
    }

    private fun onMessageSent(message: String) {
        // We sent a message! Handle it here.
        Toast.makeText(context, "Enviando mensaje...", Toast.LENGTH_LONG)
            .show() // Replace context with your context instance.
    }

    private fun onMessageReceived(message: String) {
        // We received a message! Handle it here.
        Toast.makeText(
            context,
            "Mensaje enviado exitosamente",
            Toast.LENGTH_LONG
        )
            .show() // Replace context with your context instance.
    }

    private fun onError(error: Throwable) {
        error.message?.let { Log.e("Error", it) }
        Toast.makeText(context, error.message, Toast.LENGTH_LONG)
            .show()
    }
}