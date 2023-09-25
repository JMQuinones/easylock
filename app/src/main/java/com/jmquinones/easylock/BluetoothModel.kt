package com.jmquinones.easylock

import android.bluetooth.BluetoothDevice
import com.harrysoft.androidbluetoothserial.BluetoothManager
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface

class BluetoothModel(val MACAddress: String) {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var pairedDevices: Collection<BluetoothDevice>
    private lateinit var deviceInterface: SimpleBluetoothDeviceInterface
    private var connectionAttemptedOrMade: Boolean = false
}