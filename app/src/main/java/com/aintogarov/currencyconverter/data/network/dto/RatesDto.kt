package com.aintogarov.currencyconverter.data.network.dto

import java.math.BigDecimal


data class RatesDto(
    val rates: Map<String, BigDecimal>
)