package com.aintogarov.currencyconverter.data.network.dto

import java.math.BigDecimal


data class RatesDto(
    val base: String,
    val rates: Map<String, BigDecimal>
)