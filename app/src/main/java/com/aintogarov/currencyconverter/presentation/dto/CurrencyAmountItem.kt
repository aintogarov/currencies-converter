package com.aintogarov.currencyconverter.presentation.dto

import com.aintogarov.currencyconverter.domain.dto.CurrencyAmount


data class CurrencyAmountItem(
    val currencyAmount: CurrencyAmount,
    val selected: Boolean
)