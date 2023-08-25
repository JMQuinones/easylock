package com.jmquinones.easylock

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class MainActivity : AppCompatActivity() {
    private lateinit var btnContinue: AppCompatButton
    private lateinit var btnConnect: AppCompatButton
    val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkBluetooth()

        initComponents()
        initListeners()
    }


    private fun initComponents() {
        btnContinue = findViewById(R.id.btnContinue)
        btnConnect = findViewById(R.id.btnConnect)
    }

    private fun initListeners() {
        btnContinue.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
        }
        btnConnect.setOnClickListener {
            val intent = Intent(this, BluetoothConnectActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkBluetooth() {

        if (!mBluetoothAdapter.isEnabled) {
            startActivityForResult(
                Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"),
                100
            )
        }

    }
}