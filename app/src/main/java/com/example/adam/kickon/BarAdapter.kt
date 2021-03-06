package com.example.adam.kickon

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class BarAdapter(context: Context,
                 private val dataSource: ArrayList<Bar>) : BaseAdapter() {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        var rowView = convertView
        if(rowView == null) {
            rowView = inflater.inflate(R.layout.list_item, parent, false)
        }

        val titleTextView = rowView!!.findViewById(R.id.bar_list_title) as TextView
        val subtitleTextView = rowView.findViewById(R.id.bar_list_subtitle) as TextView
        val thumbnailImageView = rowView.findViewById(R.id.bar_list_thumbnail) as ImageView

        val bar = getItem(position) as Bar

        titleTextView.text = bar.name

        var text = if(bar.description.length >40){
            bar.description.substring(0, 40)
        }else{
            bar.description
        }
        text += "..."
        subtitleTextView.text = text

        thumbnailImageView.setImageDrawable(bar.logo)

        return rowView
    }
}