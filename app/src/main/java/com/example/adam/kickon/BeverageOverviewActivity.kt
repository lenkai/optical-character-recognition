package com.example.adam.kickon

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import kotlin.properties.Delegates

/**
 * @brief Detects the items of a beverage card (given by image)
 *        and gives the chance to correct the items
 *
 * @property m_ocrProcessor Represents the unit,
 *                          which detects cocktails and there prices from an image
 */
class BeverageOverviewActivity : Activity() {

    private var TAG = "BEVERAGE_OVERVIEW"

    // Encapsulates the OCR functions
    private var m_ocrProcessor by Delegates.notNull<OCRProcessor>()

    /**
     * @brief Searching for cocktails and prices in the given image and displaying them
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beverage_overview)
        m_ocrProcessor = OCRProcessor(applicationContext)

        // Get intent extra
        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE))

        /// Correct rotation of the picture

        var matrix = Matrix()
        matrix.postRotate(90.0F)
        // Get Bitmap from imageuri
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val bitmapRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true)

        /// OCR

        // Detect cocktails and there prices
        val map =  m_ocrProcessor.detectFrom(bitmapRotated) // detectPrices(items)

        // Do something with the results
        map.forEach {
            Log.d(TAG,"Cocktail: " + it.key + "\tprice: " + it.value + " €\n")
        }

        /// TODO: Display the cocktails with there prices
    }

    /**
     * @brief Finishing this activity with "OK" status and saving the beverage list
     */
    fun onConfirm(view : View) {
        /// TODO: Saving beverage list in the database

        /// Prepare result

        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
