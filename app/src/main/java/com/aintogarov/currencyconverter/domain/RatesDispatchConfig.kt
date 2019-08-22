package com.aintogarov.currencyconverter.domain

import java.math.BigDecimal


class RatesDispatchConfig(
    val baseCurrency: String,
    val interval: Long,
    val defaultValue: BigDecimal
) {
    companion object {
        val DEFAULT = RatesDispatchConfig(
            baseCurrency = "EUR",
            interval = 5000L,
            defaultValue = BigDecimal("100.0"))
    }
}

