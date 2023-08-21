package com.jmquinones.easylock

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.jmquinones.easylock.databinding.ActivityCameraBinding
import com.jmquinones.easylock.ml.Model1
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private var imageSize:Int =224
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
//                binding.ivPicture.setImageBitmap(imageBitmap)
                processImage(imageBitmap)
            } else {
                val imageBitmap = data?.extras?.get("data") as Bitmap
//                binding.ivPicture.setImageBitmap(imageBitmap)
                processImage(imageBitmap)

            }
//            binding.ivPicture.setImageBitmap(imageBitmap)
        }
    }
    private fun processImage(imageBitmap: Bitmap) {
//        binding.ivPicture.setImageBitmap(imageBitmap)
        var image: Bitmap = imageBitmap
        val dimension = image.width.coerceAtMost(image.height)
        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
        binding.ivPicture.setImageBitmap(image)
        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false)
            classifyImage(image)
    }

    private fun classifyImage(image: Bitmap?) {
        val model = Model1.newInstance(applicationContext);

        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)

        val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(imageSize * imageSize)
        image!!.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
        var pixel = 0
//        Get RGB values from the bitmap
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val `val` = intValues[pixel++] // RGB
                byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 255f))
                byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 255f))
                byteBuffer.putFloat((`val` and 0xFF) * (1f / 255f))
            }
        }
        inputFeature0.loadBuffer(byteBuffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val confidences = outputFeature0.floatArray
        var maxConfidence = 0f
        var maxPos = 0
        for (i in confidences.indices) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i]
                maxPos = i
            }
        }
        val classes = arrayOf("Banana", "orange", "Pen", "Stikcy Note")
        binding.tvPrediction.text = classes[maxPos]
        var s = ""
        for (i in classes.indices) {
            s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100)
        }
        binding.tvConfidence.text = s

        // Releases model resources if no longer used.
        model.close()
    }


}