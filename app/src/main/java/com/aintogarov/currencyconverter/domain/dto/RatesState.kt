package com.aintogarov.currencyconverter.domain.dto

import com.aintogarov.currencyconverter.data.network.dto.RatesDto


sealed class RatesState(
    open val ratesDto: RatesDto? = null,
    open val error: Throwable? = null
) {
    object Empty : RatesState(null, null)
    data class Error(override val error: Throwable) : RatesState(null, error)
    data class Loaded(override val ratesDto: RatesDto) : RatesState(ratesDto, null)
}