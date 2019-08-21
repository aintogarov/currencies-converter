package com.aintogarov.currencyconverter.presentation

import androidx.recyclerview.widget.DiffUtil


data class CurrenciesWithDiff(val currenciesList: List<CurrencyAmountItem>, val diffResult: DiffUtil.DiffResult?)