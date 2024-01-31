package com.jmquinones.easylock.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmquinones.easylock.R
import com.jmquinones.easylock.adapters.AdapterClass
//import com.jmquinones.easylock.adapters.LogsAdapter
import com.jmquinones.easylock.databinding.ActivityLogsBinding
import com.jmquinones.easylock.models.LogAttempt
import com.jmquinones.easylock.utils.LogUtils
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
        initView()

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
                logsLists.add(LogAttempt(logValues[0], logValues[1]))
            }
            binding.rvLogs.adapter = AdapterClass(logsLists)

        } catch (e: Exception){
            e.printStackTrace()
        }
    }
}



