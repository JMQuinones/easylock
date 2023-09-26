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
import androidx.activity.OnBackPressedCallback
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
    private lateinit var binding: ActivityBluetoothConnectBinding
    private lateinit var pairedDevices: Collection<BluetoothDevice>
    private lateinit var MACAddress: String
    private lateinit var bluetoothModel: BluetoothModel

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityBluetoothConnectBinding.inflate(layoutInflater)
        val view = binding.root
        super.onCreate(savedInstanceState)
        setContentView(view)
        bluetoothModel= BluetoothModel(context = this@BluetoothConnectActivity)

        loadPairedDevices()
        initListeners()
    }



    private fun initListeners() {
        checkPermission()

        binding.listDeviceBluetooth.setOnItemClickListener { _, _, i, _ ->
            if (!pairedDevices.isEmpty()) {
                binding.tvTitle.text = resources.getString(R.string.select_device)
                binding.tvSelected.text ="Disp. selecionado: ${pairedDevices.elementAt(i).name} - ${pairedDevices.elementAt(i).address}"
//                    pairedDevices.elementAt(i).name + "\n" + pairedDevices.elementAt(i).address
                connectDevice(pairedDevices.elementAt(i).address)
            }
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            // disconnedt the BT conecction when pressing the back button
            override fun handleOnBackPressed() {
//                Toast.makeText(this@BluetoothConnectActivity, "BACK PRESSED", Toast.LENGTH_LONG).show()
                bluetoothModel.disconnect(MACAddress)
                finish()
            }
        })

    }

    private fun loadPairedDevices() {
        checkPermission()
        pairedDevices = bluetoothModel.pairedDevices

        val arrayListDevice: ArrayList<String> = ArrayList<String>()
        if (pairedDevices.isEmpty()) {
            binding.tvTitle.text = resources.getString(R.string.no_paired_device);
            return
        }
        for (device in pairedDevices) {
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
    }


    private fun showToastNotification(message: String){
        Toast.makeText(
            this@BluetoothConnectActivity,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}



