package com.jmquinones.easylock.activities

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.jmquinones.easylock.R
import com.jmquinones.easylock.utils.Constants.Companion.ATTEMPT_COUNTER_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {
    private lateinit var btnContinue: AppCompatButton
    private lateinit var btnConnect: AppCompatButton
    private lateinit var btnExit: AppCompatButton
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
        btnExit = findViewById(R.id.btnExit)
    }

    private fun initListeners() {
        btnContinue.setOnClickListener {
            if(mBluetoothAdapter.isEnabled){

                val intent = Intent(this, MainMenuActivity::class.java)
                startActivity(intent)
            } else {
                startActivityForResult(
                    Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"),
                    100
                )
            }

            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)


        }
        btnConnect.setOnClickListener {
            if(mBluetoothAdapter.isEnabled){

                val intent = Intent(this, BluetoothConnectActivity::class.java)
                startActivity(intent)
            } else {
                startActivityForResult(
                    Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"),
                    100
                )
            }
            val intent = Intent(this, BluetoothConnectActivity::class.java)
            startActivity(intent)
        }

        btnExit.setOnClickListener {
            this.finishAffinity();
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