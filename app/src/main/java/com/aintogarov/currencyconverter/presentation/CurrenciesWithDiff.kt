package com.aintogarov.currencyconverter.presentation

import androidx.recyclerview.widget.DiffUtil
import com.aintogarov.currencyconverter.domain.CurrencyAmount


data class CurrenciesWithDiff(val currenciesList: List<CurrencyAmount>, val diffResult: DiffUtil.DiffResult?)