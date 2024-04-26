package com.jmquinones.easylock.utils

import android.R.id
import android.app.Application
import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class LogUtils: Application() {

    companion object {
        // TODO change to a proper db, maybe sqlite
        fun logError(tag:String, message: String, context: Context) {
            Log.d(tag, message)
            writeToFile(message, context)
        }

        private fun writeToFile(message: String, context: Context) {
            try {
                // Create a log file
                val logFile = getLogFile(context)

                // Get the current timestamp
                val timestamp =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                // Write the log entry to the file
                val logEntry = """
                   $timestamp#$message
                   
                   """.trimIndent()
                val outputStream = FileOutputStream(logFile, true)
                outputStream.write(logEntry.toByteArray())
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun getLogFile(context: Context): File {
            // Get the app's external storage directory
            val externalDir: File = File(context.getExternalFilesDir(null), "Logs")

            // Create the directory if it doesn't exist
            if (!externalDir.exists()) {
                externalDir.mkdirs()
            }

            // Create a log file within the directory
            val logFileName = "app_logs.txt"
            return File(externalDir, logFileName)
        }

    }
}