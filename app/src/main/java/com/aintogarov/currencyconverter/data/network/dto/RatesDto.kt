package com.aintogarov.currencyconverter.data.network.dto


data class RatesDto(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)