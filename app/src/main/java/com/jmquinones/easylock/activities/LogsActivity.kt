package com.jmquinones.easylock.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
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

class LogsActivity : AppCompatActivity() {
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
    }

    private fun initView() {
        binding.rvLogs.layoutManager = LinearLayoutManager(this)
        logsLists = arrayListOf()
        loadLogs()
        println(logsLists)
    }

    private fun loadLogs(){
        val logs: File = LogUtils.getLogFile(this@LogsActivity)
        try {
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
        } catch (e: Exception) {
            e.localizedMessage?.let { showToastNotification(it) }
            e.printStackTrace()
        }
    }

    private fun showToastNotification(message: String) {
        Toast.makeText(
            this@LogsActivity,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}



