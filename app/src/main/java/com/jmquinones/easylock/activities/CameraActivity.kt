package com.jmquinones.easylock.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier.ImageClassifierOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.jmquinones.easylock.R
import com.jmquinones.easylock.utils.BluetoothUtils
import com.jmquinones.easylock.databinding.ActivityCameraBinding
import com.jmquinones.easylock.ml.ModelCv
import com.jmquinones.easylock.utils.LogUtils
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private var imageSize: Int = 224
    private lateinit var detector: FaceDetector
    private lateinit var MACAddress: String
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCameraBinding.inflate(layoutInflater)
        val view = binding.root
        super.onCreate(savedInstanceState)
        setContentView(view)
        readMACAddress()
        initListeners()
        initFaceDetector()
    }

    private fun initListeners() {
        binding.btnPicture.setOnClickListener {

            // Launch camera if we have permission
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                resultLauncher.launch(cameraIntent)
            } else {
                //Request camera permission if we don't have it.
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
            }
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val imageBitmap =
                        data?.extras?.getParcelable("data", Bitmap::class.java) as Bitmap
                    processImage(imageBitmap)
                } else {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    processImage(imageBitmap)
//                classifyImage(imageBitmap)
                }

            }
        }

    private fun processImage(imageBitmap: Bitmap) {

        var image: Bitmap = imageBitmap
        val dimension = image.width.coerceAtMost(image.height)
        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
//        binding.ivPicture.setImageBitmap(image)
        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false)
        faceDetection(image)

    }

    private fun resizeImage(imageBitmap: Bitmap, xSize: Int, ySize: Int): Bitmap? {
        return Bitmap.createScaledBitmap(imageBitmap, xSize, ySize, false)
    }

    private fun initFaceDetector() {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
        detector = FaceDetection.getClient(options)
    }

    private fun faceDetection(imageBitmap: Bitmap) {
        val image = InputImage.fromBitmap(imageBitmap, 0)


        val result = detector.process(image)
            .addOnSuccessListener { faces ->
                try {
                    if (faces.isNotEmpty()) {
                        val face = faces.first()
                        val bounds = face.boundingBox
                        Log.d(
                            "bounds",
                            "left ${bounds.left} top ${bounds.top} right ${bounds.right} bottom ${bounds.bottom}"
                        )
                        // crop detected face
                        val faceDetected = Bitmap.createBitmap(
                            imageBitmap,
                            bounds.left, bounds.top,
                            bounds.right - bounds.left,
                            bounds.bottom - bounds.top
                        )
                        binding.ivPicture.setImageBitmap(faceDetected)
                        classifyImage(faceDetected)

                    } else {
                        showToastNotification("No se encontraron rostros, intente de nuevo")

                    }
                } catch (e: IllegalArgumentException) {
                    Log.e("error", e.toString())
                    showToastNotification("Algo salio mal, intente de nuevo")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ERROR", e.stackTraceToString())
                showToastNotification("Algo salio mal, intente de nuevo")

            }
    }

    private fun classifyImage(image: Bitmap?) {

        val imageClassifier = createTask();
        val mpImage = BitmapImageBuilder(image).build()
        val classifierResult = imageClassifier.classify(mpImage)
        val results = classifierResult.classificationResult().classifications()[0]
        var maxScore = 0f
        var maxIndex = 0

        for ((index, categories) in results.categories().withIndex()) {
            if (categories.score() >= maxScore) {
                maxIndex = index
                maxScore = categories.score()
            }
            /*Log.e(
                "Res", "Category: ${categories.categoryName()} " +
                        ", Display name: ${categories.displayName()}" +
                        ", Score: ${categories.score()}" +
                        ", Index: ${categories.index()}"
            )*/
        }
        Log.i("Max Index", "" + maxIndex)

        val predictedResult = results.categories()[maxIndex]
        Log.i("Predicted result", predictedResult.toString())

        val predictionScore = String.format("%.1f%%", results.categories()[maxIndex].score() * 100)

        binding.tvPrediction.text = predictedResult.categoryName()
        binding.tvConfidence.text = predictionScore

        // if authentication succeed send message to the lock
        if (predictedResult.categoryName() != "negative" && predictedResult.score() * 100 >= 75) {
            if (this::MACAddress.isInitialized && MACAddress.isNotEmpty()) {
                showToastNotification("Éxito al autenticar. Abriendo cerradura")
                //TODO: Send message to the arduino boards to open the lock
                val bluetoothUtils =
                    BluetoothUtils(MACAddress = MACAddress, context = this@CameraActivity)

                bluetoothUtils.connectDeviceAndOpen(MACAddress)
                //TODO: Save open attempt to log
                LogUtils.logError("Open Attempt", "Exito", "Rec. Facial", this@CameraActivity)

            } else {
                showToastNotification("No hay un dispositivo conectado")
            }
        } else {
            showToastNotification("Error al autenticar")
            //TODO: Save open attempt to log
            LogUtils.logError("Open Attempt", "Error","Rec. Facial", this@CameraActivity)

        }
        imageClassifier.close()
    }

    private fun classifyImageOld(image: Bitmap?) {
//        val model = Model1.newInstance(applicationContext)
//        val model = ModelUnquant.newInstance(applicationContext)
        val model = ModelCv.newInstance(applicationContext)

        // Creates inputs for reference.
        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, imageSize, imageSize, 3), DataType.FLOAT32)
        val byteBuffer = getByteArray(image)
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
        val classes = arrayOf("positive", "negative")
        var s = ""

        s += String.format("%.1f%%", confidences[maxPos] * 100)

        binding.tvPrediction.text = classes[maxPos]
        binding.tvConfidence.text = s

        // if authentication succed send message to the lock
        if (classes[maxPos] != "negative" && confidences[maxPos] * 100 >= 75) {

            if (MACAddress.isNotEmpty()) {
                showToastNotification("Éxito al autenticar. Abriendo cerradura")

                //TODO: Send message to the arduino boards to open the lock
                val bluetoothUtils =
                    BluetoothUtils(MACAddress = MACAddress, context = this@CameraActivity)

                bluetoothUtils.connectDeviceAndOpen(MACAddress)
                //TODO: Save open attempt to log
                LogUtils.logError("Open Attempt", "Exito", "Rec. Facial", this@CameraActivity)

            } else {
                showToastNotification("No hay un dispositivo conectado")
            }
        } else {
            showToastNotification("Error al autenticar")
            //TODO: Save open attempt to log
            LogUtils.logError("Open Attempt", "Error", "Rec. Facial", this@CameraActivity)

        }
        // Releases model resources if no longer used.
        model.close()
    }

    private fun createTask(): ImageClassifier {
        val options = ImageClassifierOptions.builder()
            .setBaseOptions(
                BaseOptions.builder().setModelAssetPath("model.tflite").build()
            )
            .setRunningMode(RunningMode.IMAGE)
            .setMaxResults(5)
            .build()
        return ImageClassifier.createFromOptions(applicationContext, options)
    }

    private fun getByteArray(image: Bitmap?): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(imageSize * imageSize)
        image!!.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
        var pixel = 0
//        Get RGB values from the bitmap
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val pixelValue = intValues[pixel++] // RGB
                byteBuffer.putFloat((pixelValue shr 16 and 0xFF) * (1f / 255f))
                byteBuffer.putFloat((pixelValue shr 8 and 0xFF) * (1f / 255f))
                byteBuffer.putFloat((pixelValue and 0xFF) * (1f / 255f))
            }
        }

        return byteBuffer
    }

    private fun readMACAddress() {
        try {
            MACAddress=this.openFileInput("device_address")
                .bufferedReader().useLines { lines ->
                lines.fold("") { some, text ->
                    "$some\n$text"
                }
            }.trim()
            Log.i("MAC-------------", MACAddress)
        }catch (e: Exception){
            Log.e("MAC address error", e.toString())
            showToastNotification(resources.getString(R.string.no_paired_device))
        }
    }

    private fun showToastNotification(message: String) {
        Toast.makeText(
            this@CameraActivity,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

}