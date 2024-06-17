package com.jmquinones.easylock.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.TextPaint
import android.util.Log
import android.widget.Toast
import com.jmquinones.easylock.R
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.RuntimeException

class PdfUtils {

    private fun getTextPaint(textSize: Float, typeface: Typeface ): TextPaint{
        val textPaint = TextPaint()
        textPaint.typeface = typeface
        textPaint.textSize = textSize
        //textPaint.color = R.color.error
        return textPaint
    }
    companion object {
        fun saveToPdf(fileName:String, title: String, content:String, context: Context) {
            val pdfDocument = PdfDocument()
            val paint = Paint()
            val titlePaint = TextPaint()
            val contentPaint = TextPaint()

            // Letter size in pixels = 816 x 1054
            val pageInfo = PdfDocument.PageInfo.Builder(816, 1054, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            val canvas = page.canvas

            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_axis_lock)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80,80, false)
            canvas.drawBitmap(scaledBitmap, 368f, 20f, paint)

            titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            titlePaint.textSize = 20f
            canvas.drawText(title, 300f, 150f, titlePaint)

            contentPaint.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            contentPaint.textSize = 14f

            val contentArray = content.split("\n")

            var y = 200f
            for (item in contentArray) {
                canvas.drawText(item, 100f, y, contentPaint)
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
                Toast.makeText(context, "Pdf created succesfully", Toast.LENGTH_LONG).show()
            } catch (e: FileNotFoundException){
                Log.e("File not found", e.toString())
                throw RuntimeException(e)
            } catch (e: IOException){
                Log.e("IO", e.toString())
                throw RuntimeException(e)
            }
        }



    }
}