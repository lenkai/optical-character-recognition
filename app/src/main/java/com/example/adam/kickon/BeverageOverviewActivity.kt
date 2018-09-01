package com.example.adam.kickon

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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

    private lateinit var m_recyclerView: RecyclerView
    private lateinit var m_viewAdapter: RecyclerView.Adapter<*>
    private lateinit var m_viewManager: RecyclerView.LayoutManager

    // Encapsulates the OCR functions
    private var m_ocrProcessor by Delegates.notNull<OCRProcessor>()
    private lateinit var m_beverageList : MutableList<Beverage>

    /**
     * @brief Searching for cocktails and prices in the given image and displaying them
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beverage_overview)

        var drinkNames = ArrayList<String>()

        Tools.getDrinkList(applicationContext).forEach {
            drinkNames.add(it.name)
        }

        m_ocrProcessor = OCRProcessor(applicationContext, drinkNames)

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
        m_beverageList =  m_ocrProcessor.detectFrom(bitmapRotated) // detectPrices(items)

        /// Preparing the RecyclerView

        m_viewAdapter = BeverageAdapter(m_beverageList)
        m_viewManager = LinearLayoutManager(this)
        m_recyclerView = findViewById<RecyclerView>(R.id.beverageListView).apply {
            setHasFixedSize(true)
            // specify an viewAdapter (see also next example)
            adapter = m_viewAdapter
            // use a linear layout manager
            layoutManager = m_viewManager
        }
    }

    /**
     * @brief Finishing this activity with "OK" status and saving the beverage list
     */
    fun onConfirm(view : View) {
        /// TODO: Saving beverage list in the database

        // Do something with the results
        m_beverageList.forEach {
            Log.d(TAG,"Cocktail: " + it.name + "\tprice: " + it.price + " €\n")
        }

        /// Prepare result

        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    /**
     * @brief Adding a default beverage to the beverage list
     */
    fun addBeverage(view : View) {
        m_beverageList.add(m_beverageList.size, Beverage())
        m_viewAdapter.notifyDataSetChanged()
    }
}
