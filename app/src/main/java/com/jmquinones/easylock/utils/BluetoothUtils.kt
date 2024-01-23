package com.jmquinones.easylock.utils

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

class BluetoothUtils(var MACAddress: String = "", val context: Context) {
    var bluetoothManager: BluetoothManager = BluetoothManager.getInstance()
    lateinit var pairedDevices: Collection<BluetoothDevice>
    lateinit var deviceInterface: SimpleBluetoothDeviceInterface
    private var connectionAttemptedOrMade: Boolean = false

    init {
        loadPairedDevices()
    }

    private fun loadPairedDevices() {
        pairedDevices = bluetoothManager.pairedDevicesList
    }

    @SuppressLint("CheckResult")
    fun connectDevice(mac: String) {
        if (!connectionAttemptedOrMade) {
            bluetoothManager.openSerialDevice(mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnected, this::onError)
            this.connectionAttemptedOrMade = true
        }
    }

    @SuppressLint("CheckResult")
    fun connectDeviceAndOpen(mac: String) {
        bluetoothManager.openSerialDevice(mac)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onConnectedAndOpen, this::onError)
    }


    private fun onConnected(connectedDevice: BluetoothSerialDevice) {
        deviceInterface = connectedDevice.toSimpleDeviceInterface()
        MACAddress = connectedDevice.mac
        saveMACAddress(MACAddress)
        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSent, this::onError)
        showToastNotification("Conectado con exito")
    }

    private fun onConnectedAndOpen(connectedDevice: BluetoothSerialDevice) {
        deviceInterface = connectedDevice.toSimpleDeviceInterface()
        MACAddress = connectedDevice.mac

        deviceInterface.setListeners(this::onOpenLockMessageReceived, this::onMessageSent, this::onError)
        showToastNotification("Conectado con exito")

        sendMessage("A")
    }

    fun disconnect(mac: String) {
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
        showToastNotification("Enviando mensaje...")
    }

    private fun onMessageReceived(message: String) {
        showToastNotification("Mensaje enviado exitosamente")
    }

    private fun onOpenLockMessageReceived(message: String) {
        showToastNotification("Cerradura abierta exitosamente")
        disconnect(MACAddress)
    }

    private fun onError(error: Throwable) {
        error.message?.let { Log.e("Error", it) }
        Toast.makeText(context, error.message, Toast.LENGTH_LONG)
            .show()
    }

    private fun showToastNotification(message: String){
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}