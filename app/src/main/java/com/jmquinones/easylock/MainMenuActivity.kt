package com.jmquinones.easylock

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import com.jmquinones.easylock.databinding.ActivityMainMenuBinding
import java.util.concurrent.Executor


class MainMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: PromptInfo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        val view = binding.root
        checkDeviceHasBiometric()
        initListeners()
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
            biometricPrompt.authenticate(promptInfo)

        }
        binding.cvFace.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
    }


    private fun checkDeviceHasBiometric() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS ->{

                Log.d("MY_APP_TAG", "App can authenticate using biometrics.")
                binding.cvFinger.isClickable = true
                binding.cvFinger.isFocusable = true
                binding.imgFinger.setImageResource(R.drawable.ic_fingerprint_24)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->{
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
                Toast.makeText(this@MainMenuActivity,
                    "Autenticación biometrica no disponible.",Toast.LENGTH_LONG).show()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->{
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
                Toast.makeText(this@MainMenuActivity,
                    "Autenticación biometrica no disponible.",Toast.LENGTH_LONG).show()

            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {

                showDialog()
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q){
//                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
//                        putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
//                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
//                    }
//                    startActivityForResult(enrollIntent, 100)
//                } else{
//                    val enrollIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
//                    startActivityForResult(enrollIntent, 100)
//                }
                // Prompts the user to create credentials that your app accepts.

            }
            else ->{
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
                Toast.makeText(this@MainMenuActivity,
                    "Autenticación biometrica no disponible.",Toast.LENGTH_LONG).show()

            }
        }
    }

    private fun showDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle("No se encontraron registros biometricos")
        builder.setMessage("Abrir seguridad para configurar?")
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
        .setSubtitle("Coloque su dedo en el sensor")
        .setNegativeButtonText("Cancelar")
        .build()

    private fun createBiometricPrompt() =
        BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(
                    this@MainMenuActivity,
                    "Error al autenticar: $errString", Toast.LENGTH_LONG
                ).show()
            }

            // Auth success
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(
                    this@MainMenuActivity,
                    "Autenticacion exitosa.", Toast.LENGTH_LONG
                ).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(
                    this@MainMenuActivity,
                    "Error al autenticar.", Toast.LENGTH_LONG
                ).show()
            }
        })
}