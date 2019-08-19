package com.aintogarov.currencyconverter.data.storage

import com.aintogarov.currencyconverter.data.network.dto.RatesDto
import io.reactivex.Maybe


interface Storage {
    fun rates(base: String): Maybe<RatesDto>
    fun writeRates(ratesDto: RatesDto)

    fun order(): Maybe<List<String>>
    fun writeOrder(order: List<String>)

}