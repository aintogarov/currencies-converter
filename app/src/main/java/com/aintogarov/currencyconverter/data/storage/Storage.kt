package com.aintogarov.currencyconverter.data.storage

import com.aintogarov.currencyconverter.data.network.dto.RatesDto
import com.aintogarov.currencyconverter.domain.dto.MoneyAmountState
import io.reactivex.Maybe


interface Storage {
    fun rates(base: String): Maybe<RatesDto>
    fun writeRates(ratesDto: RatesDto)

    fun order(): Maybe<List<String>>
    fun writeOrder(order: List<String>)

    fun moneyAmount(): Maybe<MoneyAmountState>
    fun writeMoneyAmount(moneyAmountState: MoneyAmountState)
}