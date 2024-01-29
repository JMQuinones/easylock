package com.jmquinones.easylock.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class LogsAdapter(context: Context, resource: Int, objects: Array<String>, private val textColor: Int) :
    ArrayAdapter<String>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)

        // Get the TextView from the view
        val textView: TextView = view.findViewById(android.R.id.text1)

        // Set the text color
        if (textView.text.toString() == "Item 1"){

            textView.setTextColor(textColor)
        }

        return view
    }
}