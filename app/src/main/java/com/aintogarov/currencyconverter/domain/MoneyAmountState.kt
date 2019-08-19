package com.aintogarov.currencyconverter.domain

import java.math.BigDecimal


data class MoneyAmountState(
    val currency: String,
    val value: BigDecimal
)