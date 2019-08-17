package com.aintogarov.currencyconverter.domain


class RatesDispatchConfig(
    val baseCurrency: String,
    val interval: Long
) {
    companion object {
        val DEFAULT = RatesDispatchConfig(baseCurrency = "EUR", interval = 1000L)
    }
}

