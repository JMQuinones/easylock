package com.jmquinones.easylock.utils

import android.app.Application
import android.content.Context
import android.widget.Toast

class ToastUtils: Application() {
    companion object {
        private fun showToastNotification(message: String, context: Context, duration: Int){
            Toast.makeText(
                context,
                message,
                duration
            ).show()
        }
    }
}