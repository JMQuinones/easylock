package com.jmquinones.easylock.adapters



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jmquinones.easylock.R
import com.jmquinones.easylock.models.LogAttempt

class AdapterClass(private val dataList: ArrayList<LogAttempt>): RecyclerView.Adapter<AdapterClass.ViewHolderClass>() {
    class ViewHolderClass(itemView: View): RecyclerView.ViewHolder(itemView) {
        val rvType:TextView = itemView.findViewById(R.id.tvType)
        val rvTimeStamp:TextView = itemView.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.log_item, parent, false)
        return ViewHolderClass(itemView)
    }
    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        holder.rvTimeStamp.text = currentItem.timestamp
        if (currentItem.description == "Exito"){
            holder.rvType.text = holder.rvType.context.getString(R.string.open_success)
            val textColor = ContextCompat.getColor(holder.rvType.context, R.color.success)
            holder.rvType.setTextColor(textColor)
        } else {
            holder.rvType.text = holder.rvType.context.getString(R.string.open_failure)
            val textColor = ContextCompat.getColor(holder.rvType.context, R.color.error)
            holder.rvType.setTextColor(textColor)
        }

    }
    override fun getItemCount(): Int {
        return dataList.size
    }

}



















