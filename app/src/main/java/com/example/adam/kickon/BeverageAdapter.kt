package com.example.adam.kickon

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText

/**
 * @brief Connects the logical beveragelist with the RecyclerView
 */
class BeverageAdapter : RecyclerView.Adapter<BeverageAdapter.BeverageViewHolder> {

    // Logical representing of out bevereage list
    private var m_beverageList : MutableList<Beverage>

    constructor(beverageMap: MutableList<Beverage>) {
        m_beverageList = beverageMap
    }

    constructor() : this(mutableListOf<Beverage>())

    /**
     * @brief Creating a logical representation for the beverage visuialization
     *
     * @return the logical representation
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeverageViewHolder {
        val itemView : View = LayoutInflater.from(parent.context)
                .inflate(R.layout.beverage_list_item, parent, false)
        return BeverageViewHolder(itemView)
    }

    /**
     * @brief Getting the amount of items in the logical list
     *
     * @return the size of the logical beverage list
     */
    override fun getItemCount(): Int = m_beverageList.size

    /**
     * @brief Updating the visualisation based on the logical beverage list
     */
    override fun onBindViewHolder(holder: BeverageViewHolder, position: Int) {
        if(position >= m_beverageList.size) {
            throw IndexOutOfBoundsException()
        }

        holder.beverage.setText(m_beverageList[position].name)
        holder.price.setText(m_beverageList[position].price.toString())
        holder.updatePosition(position)
    }

    /**
     * @brief Logical representation of the beverage visualization
     *
     * @property beverage Name of the beverage
     * @property prive of the beverage
     *
     * @property m_beverageListener What happens, when the name is changed by the user?
     * @property m_priceListener What happens, when the price is changed by the user?
     */
    inner class BeverageViewHolder(val view : View) : RecyclerView.ViewHolder(view) {
        var beverage : EditText
        var price : EditText
        private var m_beverageListener : BeverageListener
        private var m_priceListener : PriceListener

        init {
            beverage = view.findViewById(R.id.editBeverage)
            m_beverageListener = BeverageListener()
            beverage.addTextChangedListener(m_beverageListener)
            price = view.findViewById(R.id.editPrice)
            m_priceListener = PriceListener()
            price.addTextChangedListener(m_priceListener)
        }

        /**
         * @brief Transfer the position in the view to the listener
         *
         * @param position of the item in the view
         */
        fun updatePosition(position : Int) {
            m_beverageListener.updatePosition(position)
            m_priceListener.updatePosition(position)
        }
    }

    /**
     * @brief Updating the name of the beverages based of the visualization
     *
     * @property m_position in the view
     */
    open inner private class BeverageListener : TextWatcher {

        protected var m_position : Int = 0

        /**
         * @brief Updating the view position property
         *
         * @param posiion on the view
         */
        fun updatePosition(position: Int) {
            m_position = position
        }

        /**
         * @brief What happens after the text is changed?
         */
        override fun afterTextChanged(s: Editable?) {
            /// Not used
        }

        /**
         * @brief What happens before the text is changed
         */
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            /// Not used
        }

        /**
         * @brief Updating the logical representation based on the visualization
         */
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            m_beverageList[m_position].name = s.toString()
        }

    }

    /**
     * @brief Updating the price of the beverages based of the visualization
     */
    private inner class PriceListener : BeverageListener() {
        /**
         * @brief Updating the logical representation based on the visualization
         */
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            m_beverageList[m_position].price = s.toString().toDouble()
        }
    }
}