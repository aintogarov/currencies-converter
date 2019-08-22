package com.aintogarov.currencyconverter.presentation.adapter

import androidx.recyclerview.widget.DiffUtil
import com.aintogarov.currencyconverter.presentation.dto.CurrencyAmountItem


data class CurrenciesWithDiff(val currenciesList: List<CurrencyAmountItem>, val diffResult: DiffUtil.DiffResult?)