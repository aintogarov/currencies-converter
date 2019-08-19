package com.aintogarov.currencyconverter.presentation

import androidx.recyclerview.widget.DiffUtil
import com.aintogarov.currencyconverter.domain.CurrencyAmount


class CurrenciesAmountDiffCallback(
    private val old: List<CurrencyAmount>,
    private val new: List<CurrencyAmount>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition].currency == new[newItemPosition].currency
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }

}