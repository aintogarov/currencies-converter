package com.aintogarov.currencyconverter.domain

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable


class MoneyAmountModel {
    private val moneyAmountBehaviorRelay = BehaviorRelay.create<MoneyAmountState>()

    fun observe(): Observable<MoneyAmountState> {
        return moneyAmountBehaviorRelay
    }

    @Synchronized
    fun push(currency: String, value: Double) {
        val nextState = MoneyAmountState(currency, value)
        moneyAmountBehaviorRelay.accept(nextState)
    }
}