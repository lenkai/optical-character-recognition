package com.example.adam.kickon

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.support.v4.widget.SwipeRefreshLayout
import android.widget.ListView


const val EXTRA_MESSAGE = "com.example.adam.kickon.MESSAGE"
const val DATABASE_ID = "com.example.adam.kickon.ID"

class MainActivity : Activity() {

    private val lastData = mutableMapOf<Int, Bar>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        setContentView(R.layout.activity_main)

        /*
         * Listens to "swipe up's" on the SwipeRefreshLayout / ListView. After triggering, it calls the
         * function updateList() to refresh the data
         */
        findViewById<SwipeRefreshLayout>(R.id.pullToRefresh).setOnRefreshListener {
            //Toast.makeText(this, "refresh", Toast.LENGTH_SHORT).show()
            updateList()
            findViewById<SwipeRefreshLayout>(R.id.pullToRefresh).isRefreshing = false
        }

        /*
         * Listens to touches on one of the List elements. After triggering, it creates a new intent
         * and displays it.
         */
        findViewById<ListView>(R.id.bar_list_view).setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, BarActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE, lastData[position]?.name)
                putExtra(DATABASE_ID, lastData[position]?.id)
            }
            startActivity(intent)
        }

        //Initial first update
        updateList()
    }

    /**
     * loads a list of all bars from the getBarList() method in Tools and puts them in the last_data
     * list and creates a new adapter with the new bar list
     */
    fun updateList() {
        Thread {
            val listView = findViewById<ListView>(R.id.bar_list_view)

            val barList = Tools.getBarList(this)
            lastData.clear()
            barList.forEachIndexed { index, e ->
                lastData.put(index, e)
            }

            val adapter = BarAdapter(this, barList)
            runOnUiThread { listView.adapter = adapter }
        }.start()
    }
}
