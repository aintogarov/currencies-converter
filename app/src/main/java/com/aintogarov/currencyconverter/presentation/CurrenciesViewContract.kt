package com.aintogarov.currencyconverter.presentation

import com.aintogarov.currencyconverter.domain.dto.CurrencyAmount
import com.aintogarov.currencyconverter.presentation.dto.ClickEvent
import io.reactivex.Observable

interface CurrenciesViewContract {
    val currencyItemClicks: Observable<CurrencyAmount>
    val amountInput: Observable<String>
    val retryClicks: Observable<ClickEvent>
}