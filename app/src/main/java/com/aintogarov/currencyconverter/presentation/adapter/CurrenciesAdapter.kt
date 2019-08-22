package com.aintogarov.currencyconverter.presentation.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aintogarov.currencyconverter.R
import com.aintogarov.currencyconverter.domain.dto.CurrencyAmount
import com.aintogarov.currencyconverter.presentation.dto.CurrencyAmountItem
import com.aintogarov.currencyconverter.utils.MoneyTextFilter
import java.math.BigDecimal


class CurrenciesAdapter(
    private val clickListener: (CurrencyAmount) -> Unit,
    private val amountInputListener: (String) -> Unit
) : RecyclerView.Adapter<CurrenciesAdapter.CurrencyAmountViewHolder>() {

    private var items: MutableList<CurrencyAmountItem> = ArrayList()

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
            amountInputListener.invoke(text.toString())
        }
    }

    private val textFilters = arrayOf(MoneyTextFilter(10, 2))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyAmountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_currency_amount, parent, false)
        return CurrencyAmountViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CurrencyAmountViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun onBindViewHolder(holder: CurrencyAmountViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val (moneyAmount, selected) = payloads[0] as CurrenciesAmountDiffCallback.Payload
            if (moneyAmount != null) holder.bindMoneyAmount(moneyAmount, items[position].selected)
            if (selected != null) holder.bindSelected(selected)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    fun applyItems(items: List<CurrencyAmountItem>, diffResult: DiffUtil.DiffResult?) {
        diffResult?.dispatchUpdatesTo(this)
        this.items.clear()
        this.items.addAll(items)
    }

    inner class CurrencyAmountViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val currencyCodeTextView: TextView = view.findViewById(R.id.currency_code_text_view)
        private val amountEditText: EditText = view.findViewById(R.id.amount_edit_text)
        private val maskView: View = view.findViewById(R.id.mask_view)

        fun bind(item: CurrencyAmountItem) {
            val currencyAmount = item.currencyAmount
            maskView.setOnClickListener { clickListener.invoke(currencyAmount) }

            currencyCodeTextView.text = currencyAmount.currency
            amountEditText.filters = textFilters

            bindMoneyAmount(currencyAmount.value, item.selected)
            bindSelected(item.selected)

            amountEditText.setOnFocusChangeListener { _, focused ->
                if (focused) {
                    val textLength = amountEditText.length()
                    amountEditText.setSelection(textLength)
                } else {
                    if (item.selected) {
                        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(amountEditText.windowToken, 0)
                    }
                }
            }
        }

        fun bindMoneyAmount(moneyAmount: BigDecimal, selected: Boolean = false) {
            val oldText = amountEditText.text.toString()
            val newText = moneyAmount.toPlainString()
            if (oldText != newText) {
                amountEditText.removeTextChangedListener(textWatcher)
                amountEditText.setText(moneyAmount.toPlainString())
                amountEditText.addTextChangedListener(textWatcher)

                if (selected) {
                    val textLength = amountEditText.length()
                    amountEditText.setSelection(textLength)
                }
            }

        }

        fun bindSelected(selected: Boolean) {
            if (selected) view.requestFocus()
            maskView.visibility = if (selected) View.GONE else View.VISIBLE
        }
    }
}