package com.jmquinones.easylock.activities

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.jmquinones.easylock.utils.BluetoothUtils
import com.jmquinones.easylock.R
import com.jmquinones.easylock.databinding.ActivityMainMenuBinding
import com.jmquinones.easylock.utils.Constants.Companion.ATTEMPT_COUNTER_KEY
import com.jmquinones.easylock.utils.Constants.Companion.DATE_KEY
import com.jmquinones.easylock.utils.LogUtils
import com.jmquinones.easylock.utils.NotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executor


class   MainMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: PromptInfo
    private lateinit var MACAddress: String
    private lateinit var notificationService: NotificationService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        val view = binding.root
        notificationService = NotificationService(applicationContext)
        checkDeviceHasBiometric()
        initListeners()
        readMACAddress()
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = createBiometricPrompt()
        promptInfo = buildPromtInfo()
        setContentView(view)
    }
    private fun initListeners() {
        binding.btnExit.setOnClickListener{
            this.finishAffinity();
        }
        binding.cvFinger.setOnClickListener {
//            checkDeviceHasBiometric()
            if(checkIsBlocked()){
                biometricPrompt.authenticate(promptInfo)
            } else {
                //showNotification()
                showToastNotification("Demasiados intentos. Vuelva a intentarlo despues.")
            }
            //biometricPrompt.authenticate(promptInfo)

        }
        binding.cvFace.setOnClickListener {
            Log.d("checkIsBlocked", checkIsBlocked().toString())
            if(checkIsBlocked()){
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)
            } else {
                //showNotification()
                showToastNotification("Demasiados intentos. Vuelva a intentarlo despues.")
            }
        }
        binding.cvLogs.setOnClickListener {
            val intent = Intent(this, LogsActivity::class.java)
            startActivity(intent)
        }

        binding.cvClose.setOnClickListener{
            closeLock();
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val exampleCounterFlow: Flow<Int> = applicationContext.dataStore.data
                .map { preferences ->
                    preferences[intPreferencesKey(ATTEMPT_COUNTER_KEY)] ?: 0
                }
            exampleCounterFlow.collect{ attempts ->
                Log.d("Attempts", "$attempts")
                if (attempts >= 10) {
                    Log.d("Attempts", "To many attempts")
                    showNotification()
                    setAttemptLockDate()
                    runOnUiThread {
                        showToastNotification("Demasiados intentos. Vuelva a intentarlo despues.")
                    }
                }
            }
        }
    }

    private fun closeLock() {
        if(MACAddress.isNotEmpty()){
            showToastNotification("Cerrando cerradura")
            val bluetoothUtils = BluetoothUtils(MACAddress=MACAddress,context = this@MainMenuActivity)
            bluetoothUtils.connectDeviceAndClose(MACAddress)
            LogUtils.logError("Close Attempt", "Cerrar", "Cerrar", this@MainMenuActivity)
        } else {
            showToastNotification("No hay un dispositivo conectado")

        }
    }


    private fun checkDeviceHasBiometric() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)) {
            // El dispositivo permite el reconocimiento biometrico
            BiometricManager.BIOMETRIC_SUCCESS ->{
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.")
                binding.cvFinger.isClickable = true
                binding.cvFinger.isFocusable = true
                binding.imgFinger.setImageResource(R.drawable.ic_fingerprint_24)
            }
            // El dispositivo no permite el reconocimiento biometrico
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->{
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
                showToastNotification("Autenticación biometrica no disponible")
//                Toast.makeText(this@MainMenuActivity,
//                    "Autenticación biometrica no disponible",Toast.LENGTH_LONG).show()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->{
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
                showToastNotification("Autenticación biometrica no disponible")
//                Toast.makeText(this@MainMenuActivity,
//                    "Autenticación biometrica no disponible",Toast.LENGTH_LONG).show()

            }
            // Permite pero no hay registros biometricos
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {

                showDialog()

            }
            else ->{
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
                showToastNotification("Autenticación biometrica no disponible")
//                Toast.makeText(this@MainMenuActivity,
//                    "Autenticación biometrica no disponible",Toast.LENGTH_LONG).show()

            }
        }
    }

    private fun showDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle("No se encontraron registros biometricos")
        builder.setMessage("¿Abrir ajustes para configurar?")
        builder.setPositiveButton("Abrir",
            DialogInterface.OnClickListener { dialog, which -> openSettings()})
        builder.setNegativeButton("Cancelar",
            DialogInterface.OnClickListener { dialog, which -> })

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun openSettings() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q){
            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            }
            startActivityForResult(enrollIntent, 100)
        } else{
            val enrollIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            startActivityForResult(enrollIntent, 100)
        }
    }

    private fun buildPromtInfo() = PromptInfo.Builder()
        .setTitle("Autenticación biómetrica")
        .setNegativeButtonText("Cancelar")
        .build()

    private fun createBiometricPrompt() =
        BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                showToastNotification("Error al autenticar: $errString")
            }

            // Auth success
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                if(MACAddress.isNotEmpty()){
                    showToastNotification("Éxito al autenticar. Abriendo cerradura")
                    val bluetoothUtils = BluetoothUtils(MACAddress=MACAddress,context = this@MainMenuActivity)
                    bluetoothUtils.connectDeviceAndOpen(MACAddress)
                    LogUtils.logError("Open Attempt", "Exito", "Rec. Dactilar", this@MainMenuActivity)
                } else {
                    showToastNotification("No hay un dispositivo conectado")

                }
                Toast.makeText(
                    this@MainMenuActivity,
                    "Autenticacion exitosa.", Toast.LENGTH_LONG
                ).show()
            }

            override fun onAuthenticationFailed() {
                lifecycleScope.launch(Dispatchers.IO){
                    incrementAttemptsCounter()
                }
                super.onAuthenticationFailed()
                Toast.makeText(
                    this@MainMenuActivity,
                    "Error al autenticar.", Toast.LENGTH_LONG
                ).show()
                LogUtils.logError("Open Attempt", "Error", "Rec. Dactilar", this@MainMenuActivity)

            }
        })

    private fun readMACAddress(){
        try {
            MACAddress=this.openFileInput("device_address"

            ).bufferedReader().useLines { lines ->
                lines.fold("") { some, text ->
                    "$some\n$text"
                }
            }.trim()
            Log.i("MAC-------------", MACAddress)
        }catch (e: Exception){
            Log.e("MAc address error", e.toString())
            showToastNotification(resources.getString(R.string.no_paired_device))
        }
    }

    private fun showToastNotification(message: String){
        Toast.makeText(
            this@MainMenuActivity,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun checkIsBlocked(): Boolean {
        val currentTimeSecs = System.currentTimeMillis()/1000

        val storeData = runBlocking { applicationContext.dataStore.data.first()}
        val blockDateSecs = storeData[longPreferencesKey(DATE_KEY)] ?: 0
        if ((currentTimeSecs - blockDateSecs) >= 60){
            lifecycleScope.launch(Dispatchers.IO){
                resetDataStore()
            }
            return true
        } else {
            return false
        }
    }

    private suspend fun resetDataStore() {
        val counterKey = intPreferencesKey(ATTEMPT_COUNTER_KEY)
        applicationContext.dataStore.edit { settings ->
            settings[counterKey] = 0
        }
        val dateKey = longPreferencesKey(DATE_KEY)
        applicationContext.dataStore.edit { settings ->
            settings[dateKey] = 0L
        }
    }

    private suspend fun incrementAttemptsCounter() {
        val counterKey = intPreferencesKey(ATTEMPT_COUNTER_KEY)
        applicationContext.dataStore.edit { settings ->
            val currentCounterValue = settings[counterKey] ?: 0
            settings[counterKey] = currentCounterValue + 1
        }
    }

    private suspend fun setAttemptLockDate(){
        val dateKey = longPreferencesKey(DATE_KEY)
        val currentTimeSecs = System.currentTimeMillis()/1000
        applicationContext.dataStore.edit { settings ->
            //val currentCounterValue = settings[counterKey] ?: 0
            settings[dateKey] = currentTimeSecs
        }
    }
    private fun showNotification() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request permission
            requestNotificationPermission()
        } else {
            // Permission already granted, proceed with notifications
            notificationService.showNotification()
        }
    }
    // Register for permission result
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, show notification
            showNotification()
        } else {
            // Permission denied, handle accordingly
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to request the permission
    private fun requestNotificationPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}