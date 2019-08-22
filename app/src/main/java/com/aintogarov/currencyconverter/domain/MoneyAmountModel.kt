package com.aintogarov.currencyconverter.domain

import com.aintogarov.currencyconverter.data.storage.Storage
import com.aintogarov.currencyconverter.domain.dto.MoneyAmountState
import com.aintogarov.currencyconverter.domain.dto.RatesDispatchConfig
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import java.math.BigDecimal


class MoneyAmountModel(
    private val storage: Storage,
    private val config: RatesDispatchConfig
) {
    private val moneyAmountBehaviorRelay = BehaviorRelay.create<MoneyAmountState>()
    private var disposable: Disposable? = null

    fun onStart() {
        disposable?.dispose()
        disposable = storage.moneyAmount()
            .subscribeBy(
                onComplete = {
                    val defaultValue = MoneyAmountState(
                        currency = config.baseCurrency,
                        value = config.defaultValue
                    )
                    moneyAmountBehaviorRelay.accept(defaultValue)
                },
                onSuccess = moneyAmountBehaviorRelay::accept
            )
    }

    fun onStop() {
        disposable?.dispose()
    }

    fun observe(): Observable<MoneyAmountState> {
        return moneyAmountBehaviorRelay
    }

    @Synchronized
    fun push(currency: String, value: BigDecimal) {
        val nextState = MoneyAmountState(currency, value)
        moneyAmountBehaviorRelay.accept(nextState)
        storage.writeMoneyAmount(nextState)
    }
}