package com.jmquinones.easylock.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.jmquinones.easylock.R
import com.jmquinones.easylock.adapters.LogsAdapter
import com.jmquinones.easylock.databinding.ActivityLogsBinding
import com.jmquinones.easylock.models.LogAttempt
import com.jmquinones.easylock.utils.LogUtils
import java.io.File

class LogsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogsBinding
    private var logsLists: MutableList<LogAttempt> = mutableListOf<LogAttempt>()
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLogsBinding.inflate(layoutInflater)
        val view = binding.root
        super.onCreate(savedInstanceState)
        setContentView(view)
        initView()

    }

    private fun initView() {

        /*val adapter = ArrayAdapter(
            this,
            R.layout.custom_list,
            arrayListDevice
        )*/
        loadLogs()
        println(logsLists)
        val data = arrayOf("Item 1", "Item 2", "Item 3")
        val textColor = ContextCompat.getColor(this@LogsActivity, R.color.primary) // Replace with your color resource

        val adapter = LogsAdapter(this, android.R.layout.simple_list_item_1, data, textColor)

        binding.lvLogs.adapter = adapter


    }

    private fun loadLogs(){
        val logs: File = LogUtils.getLogFile(this@LogsActivity)
        try {
            logs.forEachLine {
                println(it)
                val logValue = it.split("#")
                logsLists += LogAttempt(logValue[0], logValue[1])
            }
        } catch (e: Exception){
            e.printStackTrace()
        }

    }


}



