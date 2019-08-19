package com.aintogarov.currencyconverter.domain

import java.math.BigDecimal


data class CurrencyAmount(
    val currency: String,
    val value: BigDecimal
)