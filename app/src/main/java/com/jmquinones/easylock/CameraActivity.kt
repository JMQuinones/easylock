package com.jmquinones.easylock

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.jmquinones.easylock.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity() {
    lateinit var binding: ActivityCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCameraBinding.inflate(layoutInflater)
        val view = binding.root
        super.onCreate(savedInstanceState)
        setContentView(view)
        initListeners()
    }

    private fun initListeners() {
        binding.btnPicture.setOnClickListener{

            // Launch camera if we have permission
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1)
//                startActivityForResult(cameraIntent, 1)
                resultLauncher.launch(cameraIntent)
            } else {
                //Request camera permission if we don't have it.
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
            }
        }
    }
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
//            val imageBitmap = data?.extras?.get("data") as Bitmap

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val imageBitmap = data?.extras?.getParcelable("data", Bitmap::class.java) as Bitmap
                binding.ivPicture.setImageBitmap(imageBitmap)
            } else {
                val imageBitmap = data?.extras?.get("data") as Bitmap
                binding.ivPicture.setImageBitmap(imageBitmap)
            }
//            binding.ivPicture.setImageBitmap(imageBitmap)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.ivPicture.setImageBitmap(imageBitmap)

//            val dimension = image!!.width.coerceAtMost(image.height)
//            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
//            binding.ivPicture.setImageBitmap(image)
//            Log.d("BITMAP", image.toString())
//            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false)
//            classifyImage(image)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}