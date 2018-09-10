package com.example.adam.kickon

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
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
      
        //change style of rating bar
        val ratingBar = findViewById<RatingBar>(R.id.rating_bar)
        val stars = ratingBar.getProgressDrawable()
        stars.setTint(Color.WHITE)
        /*
        Log.d(TAG, "DRINKS!!!!!!!!")

        Tools.getDrinkList().forEach {
            Log.d(TAG, "Drink:  " + it.name)
        }
        */

        Thread {
            //image view:
            val result = Tools.loadJSONfromUrl("https://lennartkaiser.de/ocr/bar_details.php?barid=" + intent.getIntExtra(DATABASE_ID, 0))

            //get features from array
            var features = ""
            for (i in  0 ..  result.getJSONObject(0).getJSONArray("features").length() - 1){

                features += result.getJSONObject(0).getJSONArray("features").getString(i) + "\n"
            }

            //get drinks (extra json file)
            val drinkResult = Tools.loadJSONfromUrl("https://lennartkaiser.de/ocr/list_prices.php?barid=" + intent.getIntExtra(DATABASE_ID, 0))

            //parse array
            var drinks = ""
            for (i in  0 ..  drinkResult.length() - 1){
                val drink = drinkResult.getJSONObject(i)
                val formatted = String.format("%.2f", drink.getDouble("price"))

                drinks += formatted + "€\t\t" + drink.getString("name") + "\n"
            }

            //set the corresponding values in the Textview Boxes
            runOnUiThread {
                findViewById<TextView>(R.id.opening_time).apply {
                    text = getString(R.string.description, result.getJSONObject(0).getString("closing_time"))
                }

                findViewById<TextView>(R.id.description).apply {
                    text = result.getJSONObject(0).getString("description")
                }

                findViewById<TextView>(R.id.adress).apply {
                    text = result.getJSONObject(0).getString("adress")
                }

                findViewById<TextView>(R.id.website_url).apply {
                    text = result.getJSONObject(0).getString("website")
                }

                findViewById<RatingBar>(R.id.rating_bar).apply {
                    rating = result.getJSONObject(0).getDouble("rating").toFloat()
                }

                findViewById<TextView>(R.id.features).apply {
                    text = features
                }

                findViewById<TextView>(R.id.drinks).apply {
                    text = drinks
                }

                //website url open browser
                findViewById<TextView>(R.id.website_url).setOnClickListener {
                    val openURL = Intent(Intent.ACTION_VIEW, Uri.parse(result.getJSONObject(0).getString("website")))
                    startActivity(openURL)
                }

                //tab on adress navigation

                findViewById<TextView>(R.id.adress).setOnClickListener {
                    val gmmIntentUri = Uri.parse("geo:0,0?q=" + result.getJSONObject(0).getString("adress"))
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }

            }



            if(result.getJSONObject(0).getJSONArray("images_urls").length() >= 1) {
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
     * @brief Encapsulates the creation of the sharing information
     *
     * @property builder Comfortable storing and appending of text for sharing
     */
    inner class ShareOrganizer{
        private val builder : StringBuilder

        init{
            builder = StringBuilder()
        }

        /**
         * @brief Adds the specific topic to the text, which should be shared
         *
         * @param topic, which should be shared
         * @param value of the topic
         */
        fun add(topic : String, value : String){
            if(builder.length != 0){
                builder.append("\n\n")
            }

            builder.append(topic)
            builder.append(": ")
            builder.append(value)
        }

        /**
         * @brief Returns text, which should be shared
         */
        override fun toString(): String {
            return builder.toString()
        }
    }

    /**
     * @brief Shares the characteristics of a bar
     */
    fun onShare(view: View) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND

        val organizer = ShareOrganizer()

        organizer.add(getString(R.string.bar_name), findViewById<TextView>(R.id.barText).text.toString())
        organizer.add(getString(R.string.ffnungszeiten), findViewById<TextView>(R.id.opening_time).text.toString())
        organizer.add(getString(R.string.beschreibung), findViewById<TextView>(R.id.description).text.toString())
        organizer.add(getString(R.string.adresse), findViewById<TextView>(R.id.adress).text.toString())
        organizer.add(getString(R.string.website), findViewById<TextView>(R.id.website_url).text.toString())
        organizer.add(getString(R.string.rating), findViewById<RatingBar>(R.id.rating_bar).rating.toString())
        organizer.add(getString(R.string.ausstattung), findViewById<TextView>(R.id.features).text.toString())
        organizer.add(getString(R.string.getr_nke), findViewById<TextView>(R.id.drinks).text.toString())

        sendIntent.putExtra(Intent.EXTRA_TEXT, organizer.toString())//"SHARE TEST.")
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
