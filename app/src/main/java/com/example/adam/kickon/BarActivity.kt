package com.example.adam.kickon

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.view.View
import android.widget.TextView


class BarActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bar)

        // Display the Bar name from the button on the textView
        val buttonText = intent.getStringExtra(EXTRA_MESSAGE)
        val textView = findViewById<TextView>(R.id.barText).apply {
            text = buttonText
        }
    }

    /**
     * Opens the Camera when the upload button is pressed
     */
    fun onUpload(view: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivity(intent)
    }

    /**
     * Shares the predefined text when the share button is pressed
     */
    fun onShare(view: View) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, "SHARE TEST.")
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

}
