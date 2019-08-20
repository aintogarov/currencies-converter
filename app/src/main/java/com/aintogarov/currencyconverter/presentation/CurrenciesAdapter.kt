package com.aintogarov.currencyconverter.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aintogarov.currencyconverter.R
import com.aintogarov.currencyconverter.domain.CurrencyAmount


class CurrenciesAdapter(
    private val clickListener: (CurrencyAmount) -> Unit
) : RecyclerView.Adapter<CurrenciesAdapter.CurrencyAmountViewHolder>() {

    private var items: MutableList<CurrencyAmount> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyAmountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_currency_amount, parent, false)
        return CurrencyAmountViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long {
        return items[position].currency.hashCode().toLong()
    }

    override fun onBindViewHolder(holder: CurrencyAmountViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    fun applyItems(items: List<CurrencyAmount>, diffResult: DiffUtil.DiffResult?) {
        diffResult?.dispatchUpdatesTo(this)
        this.items.clear()
        this.items.addAll(items)
    }

    inner class CurrencyAmountViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val currencyCodeTextView: TextView = view.findViewById(R.id.currency_code_text_view)
        private val amountEditText: EditText = view.findViewById(R.id.amount_edit_text)

        fun bind(currencyAmount: CurrencyAmount) {
            currencyCodeTextView.text = currencyAmount.currency
            amountEditText.setText(currencyAmount.value.toEngineeringString())

            view.setOnClickListener { clickListener.invoke(currencyAmount) }
            amountEditText.setOnFocusChangeListener { view, focused ->
                if (focused) {
                    val textLength = amountEditText.length()
                    amountEditText.setSelection(textLength)
                }
            }
        }
    }
}