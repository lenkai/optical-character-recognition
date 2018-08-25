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
import kotlin.properties.Delegates


class BeverageOverviewActivity : Activity() {

    private var TAG = "BEVERAGE_OVERVIEW"

    // Encapsultes the OCR functions
    private var m_ocrProcessor by Delegates.notNull<OCRProcessor>()

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

        /// Prepare result

        val resultIntent = Intent()
        //resultIntent.putExtra("some_key", "String data")
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
