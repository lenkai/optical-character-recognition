package com.example.adam.kickon

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import kotlin.properties.Delegates


const val EXTRA_IMAGE = "com.example.adam.kickon.IMAGE"

class BarActivity : Activity() {

    private val TAG = "BAR_ACTIVITY"
    private val IMAGE_CAPTURE = 1
    private val OVERVIEW_REQUEST = 2
    private val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 123

    // Button for starting the camera activity
    private var m_cameraButton by Delegates.notNull<Button>()
    // How we can get the created picture
    private lateinit var m_imageUri : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bar)

        // Display the Bar name from the button on the textView
        val buttonText = intent.getStringExtra(EXTRA_MESSAGE)
        findViewById<TextView>(R.id.barText).apply {
            text = buttonText
        }
        m_cameraButton = findViewById<Button>(R.id.uploadBeverageList)

        Log.d(TAG, "DRINKS!!!!!!!!")

        Tools.getDrinkList().forEach {
            Log.d(TAG, "Drink:  " + it.name)
        }

        Thread {
            //image view:
            val result = Tools.loadJSONfromUrl("https://lennartkaiser.de/ocr/bar_details.php?barid=" + intent.getIntExtra(DATABASE_ID, 0))

            val imgBox = findViewById(R.id.imageView2) as ImageView
            val img = Tools.loadImageFromUrl(result.getJSONObject(0).getJSONArray("images_urls").getString(0), this)
            runOnUiThread {
                try {
                    imgBox.setImageDrawable(img)
                } catch (e: Exception) {
                    e.printStackTrace()
                    imgBox.setImageResource(R.drawable.noimage)
                }
            }
        }.start()
    }
  
    /**
     * @brief Called when this activity was started
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(Array<String>(1){ Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
            m_cameraButton.setEnabled(false)
        }
        else{
            m_cameraButton.setEnabled(true)
        }
    }

    /**
     * @brief Receiving dynamic permission results
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if((requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) &&
                (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
            m_cameraButton.setEnabled(true)
        }

    }

    /**
     * @brief Opens the Camera when the upload button is pressed
     */
    fun onUpload(view: View) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, getString(R.string.app_name))
        values.put(MediaStore.Images.Media.DESCRIPTION, getString(R.string.picture_description))
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")

        m_imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, m_imageUri)
        startActivityForResult(intent, IMAGE_CAPTURE)
    }

    /**
     * @brief Shares the predefined text when the share button is pressed
     */
    fun onShare(view: View) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, "SHARE TEST.")
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    /**
     * @brief Receiving the result of the camera activity and working with the created image
     *
     * @param requestCode Is it really our camera activity?
     * @param resultCode Can we work with the created image?
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            IMAGE_CAPTURE ->
                if (resultCode == Activity.RESULT_OK) {
                    val overviewIntent = Intent(this, BeverageOverviewActivity::class.java).apply {
                        putExtra(EXTRA_IMAGE, m_imageUri.toString())
                        putExtra(DATABASE_ID, intent.getIntExtra(DATABASE_ID, 0))
                    }
                    startActivityForResult(overviewIntent, OVERVIEW_REQUEST)
                }
                // If camera activity was aborted
                else {
                    // Delete the image
                    val rowsDeleted = contentResolver.delete(m_imageUri, null, null)
                    Log.d(TAG, rowsDeleted.toString() + " rows deleted")
                }

            OVERVIEW_REQUEST ->
                if(resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "Overview ok!")
                }
        }
    }
}
