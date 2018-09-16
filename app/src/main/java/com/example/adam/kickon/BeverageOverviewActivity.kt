package com.example.adam.kickon

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
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

        val drinks = Tools.getDrinkList()
        val drink_names = ArrayList<String>(drinks.size)

        for(index in drinks.indices){
            drink_names.add(drinks[index].name)
        }

        m_ocrProcessor = OCRProcessor(applicationContext, drink_names)

        // Get intent extra
        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE))

        /// Correct rotation of the picture

        // Get Bitmap from imageuri
        var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val matrix = Matrix()

        matrix.postRotate(90.0F)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true)

        /// OCR

        // Detect cocktails and there prices
        m_beverageList =  m_ocrProcessor.detectFrom(bitmap) // detectPrices(items)

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
        // Send the beverage list to the database

        val bar_id = intent.getIntExtra(DATABASE_ID, 0)

        val alert = AlertDialog.Builder(this)
        alert.setTitle(R.string.password_promt)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL


        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setRawInputType(Configuration.KEYBOARD_12KEY)
        layout.addView(input)

        val del_others = CheckBox(this)
        del_others.text = getString(R.string.delete_other_drinks)
        layout.addView(del_others)

        alert.setView(layout)

        alert.setPositiveButton("Ok") { dialog, whichButton ->

            Log.d(TAG, Tools.modifyPrices( bar_id.toString(), input.text.toString(), m_beverageList, del_others.isChecked).toString())

            m_beverageList.forEach {
                Log.d(TAG,"Cocktail: " + it.name + "\tprice: " + it.price + " €\n")
            }

            /// Prepare result

            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
        alert.setNegativeButton(R.string.cancel) { dialog, whichButton ->

        }

        alert.show()
    }

    /**
     * @brief Adding a default beverage to the beverage list
     */
    fun addBeverage(view : View) {
        m_beverageList.add(m_beverageList.size, Beverage(getString(R.string.getr_nk, getString(R.string.example_price).replace(",", ".").toDouble())))
        m_viewAdapter.notifyDataSetChanged()
    }
}
