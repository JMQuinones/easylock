package com.jmquinones.easylock.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.TextPaint
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmquinones.easylock.R
import com.jmquinones.easylock.adapters.AdapterClass
//import com.jmquinones.easylock.adapters.LogsAdapter
import com.jmquinones.easylock.databinding.ActivityLogsBinding
import com.jmquinones.easylock.models.LogAttempt
import com.jmquinones.easylock.utils.LogUtils
import com.jmquinones.easylock.utils.ToastUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LogsActivity : AppCompatActivity() {
    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE: Int = 1
    private lateinit var binding: ActivityLogsBinding
    //private lateinit var recyclerView: RecyclerView
    private lateinit var logsLists: ArrayList<LogAttempt>
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLogsBinding.inflate(layoutInflater)
        val view = binding.root
        super.onCreate(savedInstanceState)
        setContentView(view)
        initListeners()
        initView()
        checkPermission()
    }

    private fun initListeners() {
        binding.fabDelete.setOnClickListener{
            //this.deleteLogs();
            val builder = AlertDialog.Builder(this@LogsActivity)
            builder.setMessage("Desea eliminar los registros?")
                .setCancelable(false)
                .setPositiveButton("Eliminar") { _, _ ->
                    this.deleteLogs()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        binding.fabPrint.setOnClickListener{
            Toast.makeText(this, "Imprimiendo registros...", Toast.LENGTH_LONG).show()
            saveToPdf("registros.pdf", "Intentos de apertura - ${getCurrentDate()}")
        }
    }

    private fun initView() {
        binding.rvLogs.layoutManager = LinearLayoutManager(this)
        logsLists = arrayListOf()
        loadLogs()
        if (logsLists.isEmpty()) {
            binding.fabDelete.hide()
            binding.fabPrint.hide()
        }
        println(logsLists)
    }

    private fun loadLogs(){
        val logs: File = LogUtils.getLogFile(this@LogsActivity)
        try {
            if(logs.length() == 0L) {
                binding.tvDeleteTitle.text = "No existen registros"
                return
            }
            logs.forEachLine {
                println(it)
                val logValues = it.split("#")
                logsLists.add(LogAttempt(logValues[0], logValues[1], logValues[2]))
            }
            binding.rvLogs.adapter = AdapterClass(logsLists)

        } catch (e: Exception){
            e.localizedMessage?.let { showToastNotification(it) }
            e.printStackTrace()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteLogs() {
        showToastNotification("Eliminando registros")
        try {

            LogUtils.deleteLogs(this@LogsActivity)
            logsLists.clear()
            binding.rvLogs.adapter?.notifyDataSetChanged()
            showToastNotification("Registros eliminados.")
            binding.tvDeleteTitle.text = "No existen registros"
            binding.fabDelete.hide()
            binding.fabPrint.hide()
        } catch (e: Exception) {
            e.localizedMessage?.let { showToastNotification(it) }
            e.printStackTrace()
        }
    }

    private fun saveToPdf(fileName:String, title: String, ) {

        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = TextPaint()
        val contentPaint = TextPaint()

        // Letter size in pixels = 816 x 1054
        val pageInfo = PdfDocument.PageInfo.Builder(816, 1054, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas

        val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.pdf_logo)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80,80, false)

        canvas.drawBitmap(scaledBitmap, 368f, 20f, paint)

        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 20f

        canvas.drawText(title, 250f, 150f, titlePaint)
//        canvas.drawText(getCurrentDate(), 300f, 175f, titlePaint)

        contentPaint.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        contentPaint.textSize = 14f

        //val contentArray = content.split("\n")

        var y = 250f
        for (log in logsLists) {
            canvas.drawText("${log.description} - ${log.openType} - ${log.timestamp}", 100f, y, contentPaint)
            y += 25
        }

        pdfDocument.finishPage(page)

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        //val fileName = "example.pdf"

        val file = File(downloadsDir, fileName)
        try {
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()
            Toast.makeText(this, "Informe guardado exitosamente", Toast.LENGTH_LONG).show()
        } catch (e: FileNotFoundException){
            Log.e("File not found", e.toString())
            throw RuntimeException(e)
        } catch (e: IOException){
            Log.e("IO", e.toString())
            throw RuntimeException(e)
        }
    }

    private fun getTextPaint(textSize: Float, typeface: Typeface ): TextPaint{
        val textPaint = TextPaint()
        textPaint.typeface = typeface
        textPaint.textSize = textSize
        //textPaint.color = R.color.error
        return textPaint
    }

    private fun checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }

    private fun getCurrentDate(): String {
        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return formatter.format(time)
    }

    private fun showToastNotification(message: String) {
        Toast.makeText(
            this@LogsActivity,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}



