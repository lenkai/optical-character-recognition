package com.example.adam.kickon

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*

const val EXTRA_MESSAGE = "com.example.adam.kickon.MESSAGE"

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * Opens the Bar Activity when the specific Bar is clicked
     */
    fun onClickBar(view: View) {
        val buttonText = (view as Button).text.toString()
        val intent = Intent(this, BarActivity::class.java).apply{
            putExtra(EXTRA_MESSAGE, buttonText)
        }
        startActivity(intent)
    }

}
