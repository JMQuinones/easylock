package com.jmquinones.easylock.utils

import android.app.Application
import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class LogUtils: Application() {

    companion object {

        fun logError(tag:String, message: String, context: Context) {
            Log.e(tag, message)
            writeToFile(message, context)
        }

        private fun writeToFile(message: String, context: Context) {
            try {
                // Create a log file
                val logFile = getLogFile(context)

                // Get the current timestamp
                val timestamp: String =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                // Create a JSON object for the log entry
                val logObject = JSONObject()
                try {
                    logObject.put("timestamp", timestamp)
                    logObject.put("message", message)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                // Write the JSON log entry to the file
                val logEntry = """
                $logObject
                
                """.trimIndent()
                val outputStream = FileOutputStream(logFile, true)
                outputStream.write(logEntry.toByteArray())
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun getLogFile(context: Context): File {
            // Get the app's external storage directory
            val externalDir: File = File(context.getExternalFilesDir(null), "Logs")

            // Create the directory if it doesn't exist
            if (!externalDir.exists()) {
                externalDir.mkdirs()
            }

            // Create a log file within the directory
            val logFileName = "app_logs.json"
            return File(externalDir, logFileName)
        }

    }
}