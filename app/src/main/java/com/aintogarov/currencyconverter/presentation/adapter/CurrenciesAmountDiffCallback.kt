package com.aintogarov.currencyconverter.presentation.adapter

import androidx.recyclerview.widget.DiffUtil
import com.aintogarov.currencyconverter.presentation.dto.CurrencyAmountItem
import java.math.BigDecimal


class CurrenciesAmountDiffCallback(
    private val old: List<CurrencyAmountItem>,
    private val new: List<CurrencyAmountItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition].currencyAmount.currency == new[newItemPosition].currencyAmount.currency
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val newItem = new[newItemPosition]
        val oldItem = old[oldItemPosition]

        val moneyAmount = newItem.currencyAmount.value.takeIf { it != oldItem.currencyAmount.value }
        val selected = newItem.selected.takeIf { it != oldItem.selected }

        if (moneyAmount != null || selected != null) {
            return Payload(
                moneyAmount,
                selected
            )
        }

        return null
    }

    data class Payload(val moneyAmount: BigDecimal? = null, val selected: Boolean? = null)

}