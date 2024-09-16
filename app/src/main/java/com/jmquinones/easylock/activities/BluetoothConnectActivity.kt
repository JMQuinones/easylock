package com.jmquinones.easylock.activities

//import android.R
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.jmquinones.easylock.R
import com.jmquinones.easylock.databinding.ActivityBluetoothConnectBinding
import com.jmquinones.easylock.utils.BluetoothUtils


class BluetoothConnectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBluetoothConnectBinding
    private lateinit var pairedDevices: Collection<BluetoothDevice>
    private lateinit var MACAddress: String
    private lateinit var bluetoothUtils: BluetoothUtils

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityBluetoothConnectBinding.inflate(layoutInflater)
        val view = binding.root
        super.onCreate(savedInstanceState)
        setContentView(view)
        bluetoothUtils= BluetoothUtils(context = this@BluetoothConnectActivity)

        loadPairedDevices()
        initListeners()
    }

    override fun onDestroy() {
        if(this::MACAddress.isInitialized){
            bluetoothUtils.disconnect(MACAddress)
        }
        super.onDestroy()
    }



    private fun initListeners() {
        checkPermission()
        binding.btnPair.setOnClickListener{openBluetoothSettings()}
        binding.listDeviceBluetooth.setOnItemClickListener { _, _, i, _ ->
            if (!pairedDevices.isEmpty()) {
                binding.tvTitle.text = resources.getString(R.string.select_device)
                binding.tvSelected.text ="Disp. selecionado: ${pairedDevices.elementAt(i).name} - ${pairedDevices.elementAt(i).address}"
//                    pairedDevices.elementAt(i).name + "\n" + pairedDevices.elementAt(i).address
                connectDevice(pairedDevices.elementAt(i).address)
            }
        }
        try {
            if(this::MACAddress.isInitialized){
                onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
                    // disconnect the BT connection when pressing the back button
                    override fun handleOnBackPressed() {
                        bluetoothUtils.disconnect(MACAddress)
                        finish()
                    }
                })
            } else {
                onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        finish()
                    }
                })
            }
        } catch (e: Exception){
            Log.e("Error on backpress", e.toString())
        }

    }

    private fun loadPairedDevices() {
        checkPermission()
        pairedDevices = bluetoothUtils.pairedDevices

        val arrayListDevice: ArrayList<String> = ArrayList<String>()
        if (pairedDevices.isEmpty()) {
            binding.tvTitle.text = resources.getString(R.string.no_paired_device);
            showDialog()
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

    private fun showDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle("No se encontraron dispositivos vinculados.")
        builder.setMessage("¿Abrir ajustes para configurar?")
        builder.setPositiveButton("Abrir",
            DialogInterface.OnClickListener { dialog, which -> openBluetoothSettings()})
        builder.setNegativeButton("Cancelar",
            DialogInterface.OnClickListener { dialog, which -> })

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun openBluetoothSettings() {
        val intentOpenBluetoothSettings = Intent()
        intentOpenBluetoothSettings.setAction(Settings.ACTION_BLUETOOTH_SETTINGS)
        startActivity(intentOpenBluetoothSettings)
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
        bluetoothUtils.connectDevice(mac)
        //bluetoothUtils.disconnect(mac)

    }


    private fun showToastNotification(message: String){
        Toast.makeText(
            this@BluetoothConnectActivity,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}



