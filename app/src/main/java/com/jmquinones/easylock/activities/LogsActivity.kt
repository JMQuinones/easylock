package com.jmquinones.easylock.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
    // Letter size in pixels = 816 x 1054
    private var PAGE_WIDTH = 816
    private var PAGE_HEIGHT = 1054

    private var successCount = 0
    private var failedCount = 0
    private var closedCount = 0

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
            saveToPdf("registros.pdf")
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
                val logValues = it.split("#")
                when (logValues[1]) {
                    "Exito" -> {
                        successCount++
                    }
                    "Error" -> {
                        failedCount++
                    }
                    else -> {
                        closedCount++
                    }
                }
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

    private fun saveToPdf(fileName: String) {
        val pdfDocument = PdfDocument()
        //val paint = Paint()

        val titlePaint = getTextPaint(20f, Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        val contentPaint = getTextPaint(14f, Typeface.defaultFromStyle(Typeface.NORMAL))
        val descPaint = getTextPaint(10f, Typeface.create(Typeface.DEFAULT, Typeface.ITALIC))


        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        var page = pdfDocument.startPage(pageInfo)

        var canvas = page.canvas
        // Set header
        setPdfHeader(canvas, titlePaint, "Intentos de apertura - ${getCurrentDate()}", descPaint)

        // Set content
        var y = 200f
        for (log in logsLists) {

            if(y >= PAGE_HEIGHT-75){
                y = 200f
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                setPdfHeader(canvas, titlePaint, "Intentos de apertura - ${getCurrentDate()}", descPaint)
            }
            when (log.description) {
                "Exito" -> {
                    contentPaint.color = ContextCompat.getColor(this@LogsActivity, R.color.success)
                    canvas.drawText("\u2022 Apertura exitosa - ${log.openType} - ${log.timestamp}", 100f, y, contentPaint)

                }
                "Error" -> {
                    contentPaint.color = ContextCompat.getColor(this@LogsActivity, R.color.error)
                    canvas.drawText("\u2022 Apertura fallida - ${log.openType} - ${log.timestamp}", 100f, y, contentPaint)
                }
                else -> {
                    contentPaint.color = ContextCompat.getColor(this@LogsActivity, R.color.info)
                    canvas.drawText("\u2022 Cerradura asegurada - ${log.openType} - ${log.timestamp}", 100f, y, contentPaint)
                }
            }
            y += 25
        }

        pdfDocument.finishPage(page)

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

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

    private fun setPdfHeader(canvas: Canvas, titlePaint: TextPaint, title: String, descPaint: TextPaint){
        // Set logo
        val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.pdf_logo)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 120,120, true)
        canvas.drawBitmap(scaledBitmap, 368f, 20f, titlePaint)

        // Set title
        canvas.drawText(title, 225f, 150f, titlePaint)

        // Set description
        descPaint.color = ContextCompat.getColor(this, R.color.light_gray)
        canvas.drawText("Exitosos: $successCount - Fallidos: $failedCount - Cerrar: $closedCount", 350f, 175f, descPaint)
    }

    private fun getTextPaint(textSize: Float, typeface: Typeface ): TextPaint{
        val textPaint = TextPaint()
        textPaint.typeface = typeface
        textPaint.textSize = textSize
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



