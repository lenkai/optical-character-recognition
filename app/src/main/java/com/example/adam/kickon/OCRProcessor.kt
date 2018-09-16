package com.example.adam.kickon

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.util.SparseArray
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.lang.Math.abs
import java.util.regex.Pattern

/**
 * @brief Encapsultes the required classes and functions of the google vision api,
 *        needed by the OCR and provides a method for textrcognition in a given image (bitmap)
 *
 * @property context of the textrecognizer
 */
class OCRProcessor(val m_context: Context, val m_drinks : ArrayList<String>) {
    // Format of the prices
    private val PRICE_REGEX = "\\d+((,|\\.)(\\d)*)?" // "\\d+((,|\\.)\\d{2})?"
    private val ROTATION_ANGLE = 90.0F
    // Topic for the log
    private val TAG = "OCRProcessor"
    // Google Visions textrecognizer
    private lateinit var m_textRecognizer : TextRecognizer

    /**
     * @brief Initiating the textrecognizer with our context
     */
    init{
        m_textRecognizer = TextRecognizer.Builder(m_context).build()

        // Was the initialization of the textrecognizer successful
        if(!m_textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");
        }
    }



    /**
     * @brief Detects cocktails and prices an a given bitmap
     *
     * @param bitmap which represents the image for the textrecognition
     *
     * @return @return map with cocktails (Strings) as keys and there prices (Doubles) as values
     */
    fun detectFrom(bitmap : Bitmap) : MutableList<Beverage> {
        /// Do OCR

        var textblocks = m_textRecognizer.detect(Frame.Builder().setBitmap(bitmap).build())
        var beverage_list = analyseText(textblocks)

        var rotatedBitmap = bitmap
        val matrix = Matrix()

        matrix.postRotate(ROTATION_ANGLE)

        if(beverage_list.size == 0) {

            for (count in 1 .. (360.0F / ROTATION_ANGLE - 0.5).toInt()) {
                val oldRotatedBitmap = rotatedBitmap
                rotatedBitmap = Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), matrix, true)
                oldRotatedBitmap.recycle()

                val new_textblocks = m_textRecognizer.detect(Frame.Builder().setBitmap(rotatedBitmap).build())
                val new_beverage_list = analyseText(new_textblocks)


                if (new_beverage_list.size > 0) {
                    beverage_list = new_beverage_list
                    break
                }

                Log.d(TAG, count.toString())
            }
        }

        return beverage_list
    }

    /**
     * @brief Detects known cocktails and there prices, based on the results of a textrecognizer
     *
     * @param textblocks calculated by a textrecognizer
     *
     * @return map with cocktails (Strings) as keys and there prices (Doubles) as values
     */
    private fun analyseText(textblocks: SparseArray<TextBlock>) : MutableList<Beverage> {
        var textblock_list = mutableListOf<TextBlock>()

        // Transform to list
        for(index in 0 .. textblocks.size() - 1) {
            textblock_list.add(textblocks.valueAt(index))
        }

        /// Sort to left side (cocktail) and right side (amount and prices)

        val left_right = sortLeftRight(textblock_list)
        val left_blocks = sortSide(left_right.first)

        val price_liter = sortLeftRight(left_right.second)
        val price_blocks = sortSide(price_liter.second)
        val liter_blocks = sortSide(price_liter.first)


        val left = mutableListOf<String>()
        val prices = mutableListOf<String>()
        val liters = mutableListOf<String>()

        left_blocks.forEach {
            it.components.forEach {
                left.add(it.value.replace("\n", ""))
            }
        }

        price_blocks.forEach {
            it.components.forEach {
                prices.add(it.value.replace("\n", "").replace("E", "").replace("€", "").replace(" ", ""))
            }
        }

        liter_blocks.forEach {
            it.components.forEach {
                liters.add(it.value.replace("\n", "").replace(" ", "").replace("cl", "").replace("l", ""))
            }
        }

        /// Filter cocktails and prices (No headers or infos)

        val detected_cocktails = mutableListOf<String>()
        val detected_prices = mutableListOf<Double>()
        val detected_liters = mutableListOf<Double>()

        for (index in 0..(left.size - 1)) {
            val text = left.elementAt(index)

            if(m_drinks.contains(text)) {
                detected_cocktails.add(text)
            }
        }

        for(index in 0..(liters.size - 1)) {
            val split = liters.elementAt(index).split(" ")

            if(split.size > 1) {
                prices.add(split.last())
            }

            val text = split.first()

            if(Pattern.matches(PRICE_REGEX, text)) {
                detected_liters.add(text.replace(",", ".").toDouble())
            }
        }

        for(index in 0..(prices.size - 1)) {
            val text = prices.elementAt(index)
            if(Pattern.matches(PRICE_REGEX, text)) {
                detected_prices.add(text.replace(",", ".").toDouble())
            }
        }

        /// Store cocktail with belonging price in a map

        var size : Int
        val list = mutableListOf<Beverage>()

        if(detected_cocktails.size <= detected_prices.size) {
            size = detected_cocktails.size
        }
        else {
            size = detected_prices.size
        }

        if(detected_liters.size < size){
            size = detected_liters.size
        }

        for (index in 0..(size - 1)) {
            list.add(Beverage(detected_cocktails.elementAt(index), detected_prices.elementAt(index), detected_liters.elementAt(index)))
        }

        return list
    }

    /**
     * @brief Finding the minmum and maxiumum x-coordinate of a list of textblocks
     *
     * @param textBlocks List of textblocks in which the minimum and maximum x-coordinate should be found
     *
     * @return Pair, which first value is the minimum and second value the maximum x-coordinate
     */
    private fun minMaxVertical(textBlocks : List<TextBlock>) : Pair<Int, Int> {
        var min = Int.MAX_VALUE
        var max = Int.MIN_VALUE

        for (index in 0..(textBlocks.size - 1)) {

            val x_value = textBlocks[index].boundingBox.centerX()

            if(min > x_value){
                min = x_value
            }

            if(max < x_value){
                max = x_value
            }
        }

        return Pair(min, max)
    }

    /**
     * @brief Sort the given list of textblocks into the left and right side,
     *
     * @param textBlocks which should be sorted
     *
     * @return Pair, which first value is the left side and second value is the right side
     */
    private fun sortLeftRight(textBlocks : List<TextBlock>) : Pair<MutableList<TextBlock>, MutableList<TextBlock>> {
        var left_blocks = mutableListOf<TextBlock>()
        var right_blocks = mutableListOf<TextBlock>()

        val min_max = minMaxVertical(textBlocks)

        for (index in 0..(textBlocks.size - 1)) {
            val x_value = textBlocks[index].boundingBox.centerX()

            if(abs(x_value - min_max.first) < abs(x_value - min_max.second)) {
                left_blocks.add(textBlocks[index])
            }
            else{
                right_blocks.add(textBlocks[index])
            }
        }

        return Pair(left_blocks, right_blocks)
    }

    /**
     * @brief Sorting the textblocks of a side by y-coordinate
     *
     * @param side List of Textblocks, which should be sorted
     *
     * @return the sorted Textblock List
     */
    private fun sortSide(side : MutableList<TextBlock>) : MutableList<TextBlock> {
        val map = mutableMapOf<Int, TextBlock>()
        val sorted_side = mutableListOf<TextBlock>()

        for(index in 0..(side.size - 1)) {
            map[side[index].boundingBox.centerY()] = side[index]
        }

        val sorted_map = map.toSortedMap()

        sorted_map.forEach {
            sorted_side.add(it.value)
        }

        return sorted_side
    }
}