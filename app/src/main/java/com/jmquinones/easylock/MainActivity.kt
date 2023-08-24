package com.jmquinones.easylock

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton

class MainActivity : AppCompatActivity() {
    private lateinit var btnContinue: AppCompatButton
    private lateinit var btnConnect: AppCompatButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initComponents()
        initListeners()
    }

    private fun initComponents() {
        btnContinue = findViewById(R.id.btnContinue)
        btnConnect = findViewById(R.id.btnConnect)
    }

    private fun initListeners() {
        btnContinue.setOnClickListener{
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
        }
        btnConnect.setOnClickListener{
            val intent = Intent(this, BluetoothConnectActivity::class.java)
            startActivity(intent)
        }
    }
}