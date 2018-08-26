package com.example.adam.kickon

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.SparseArray
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.gms.vision.Frame
import java.lang.Math.abs
import java.util.regex.Pattern
import kotlin.properties.Delegates

/**
 * @brief Encapsultes the required classes and functions of the google vision api,
 *        needed by the OCR and provides a method for textrcognition in a given image (bitmap)
 *
 * @property context of the textrecognizer
 */
class OCRProcessor(context: Context) {
    // Format of the prices
    private val PRICE_REGEX = "\\d+((,|\\.)\\d{2})?"
    // Topic for the log
    private val TAG = "OCRProcessor"
    // Hard coded "database"
    private val cocktails = listOf<String>("Havana Club", "Long Island Iced Tea", "Zombie")

    // Google Visions textrecognizer
    private var textRecognizer by Delegates.notNull<TextRecognizer>()

    /**
     * @brief Initiating the textrecognizer with our context
     */
    init{
        textRecognizer = TextRecognizer.Builder(context).build()
    }

    /**
     * @brief Detects cocktails and prices an a given bitmap
     *
     * @param bitmap which represents the image for the textrecognition
     *
     * @return @return map with cocktails (Strings) as keys and there prices (Doubles) as values
     */
    public fun detectFrom(bitmap : Bitmap) : MutableList<Beverage> {
        val frame = Frame.Builder().setBitmap(bitmap).build()
        // Do OCR
        return analyseText(textRecognizer.detect(frame))
    }

    /**
     * @brief Detects known cocktails and there prices, based on the results of a textrecognizer
     *
     * @param textBlocks calculated by a textrecognizer
     *
     * @return map with cocktails (Strings) as keys and there prices (Doubles) as values
     */
    private fun analyseText(textBlocks: SparseArray<TextBlock>) : MutableList<Beverage> {
        /// Emit left and right border

        var min = Int.MAX_VALUE
        var max = Int.MIN_VALUE

        for (index in 0..(textBlocks.size() - 1)) {

            val x_value = textBlocks.valueAt(index).boundingBox.centerX()

            if(min > x_value){
                min = x_value
            }

            if(max < x_value){
                max = x_value
            }
        }

        /// Sort textblocks into left (possible cocktails) and right (possible prices)

        /*val left = mutableListOf<String>()
        val right = mutableListOf<String>()

        for (index in 0..(textBlocks.size() - 1)) {
            val x_value = textBlocks.valueAt(index).boundingBox.centerX()

            if(abs(x_value - min) < abs(x_value - max)) {
                textBlocks.valueAt(index).components.forEach {
                    left.add(it.value.replace("\n", ""))
                }
            }
            else{
                textBlocks.valueAt(index).components.forEach {
                    right.add(it.value.replace("\n", "").replace("E", "").replace("€", "").replace(" ", ""))
                }
            }
        }*/

        var left_blocks = mutableListOf<TextBlock>()
        var right_blocks = mutableListOf<TextBlock>()

        for (index in 0..(textBlocks.size() - 1)) {
            val x_value = textBlocks.valueAt(index).boundingBox.centerX()

            if(abs(x_value - min) < abs(x_value - max)) {
                left_blocks.add(textBlocks.valueAt(index))
            }
            else{
                right_blocks.add(textBlocks.valueAt(index))
            }
        }

        left_blocks = sortSide(left_blocks)
        right_blocks = sortSide(right_blocks)

        var left = mutableListOf<String>()
        var right = mutableListOf<String>()

        left_blocks.forEach {
            it.components.forEach {
                left.add(it.value.replace("\n", ""))
            }
        }

        right_blocks.forEach {
            it.components.forEach {
                right.add(it.value.replace("\n", "").replace("E", "").replace("€", "").replace(" ", ""))
            }
        }

        /// Filter cocktails and prices (No headers or infos)

        var detected_cocktails = mutableListOf<String>()
        var detected_prices = mutableListOf<Double>()

        for (index in 0..(left.size - 1)) {
            val text = left.elementAt(index)

            if(cocktails.contains(text)) {
                detected_cocktails.add(text)
            }
        }

        for(index in 0..(right.size - 1)) {
            val text = right.elementAt(index).split(" ").last()
            if(Pattern.matches(PRICE_REGEX, text)) {
                detected_prices.add(text.replace(",", ".").toDouble())
            }
        }

        /// Store cocktail with belonging price in a map

        val size : Int
        val list = mutableListOf<Beverage>()

        if(detected_cocktails.size <= detected_prices.size) {
            size = detected_cocktails.size
        }
        else {
            size = detected_prices.size
        }

        for (index in 0..(size - 1)) {
            var beverage = Beverage()
            beverage.name = detected_cocktails.elementAt(index)
            beverage.price = detected_prices.elementAt(index)
            list.add(beverage)
        }

        return list
    }

    /**
     * @brief Sorting the textblocks of a side by y-coordinate
     *
     * @param side List of Textblocks, which should be sorted
     *
     * @return the sorted Textblock List
     */
    private fun sortSide(side : MutableList<TextBlock>) : MutableList<TextBlock> {
        var map = mutableMapOf<Int, TextBlock>()
        var sorted_side = mutableListOf<TextBlock>()

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