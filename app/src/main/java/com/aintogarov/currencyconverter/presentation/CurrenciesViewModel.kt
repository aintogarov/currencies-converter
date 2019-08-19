package com.aintogarov.currencyconverter.presentation

import androidx.recyclerview.widget.DiffUtil
import com.aintogarov.currencyconverter.domain.CurrenciesModel
import com.aintogarov.currencyconverter.domain.CurrenciesState
import com.aintogarov.currencyconverter.domain.CurrencyAmount
import com.aintogarov.currencyconverter.domain.MoneyAmountModel
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.plusAssign


class CurrenciesViewModel(
    private val currenciesModel: CurrenciesModel,
    private val amountModel: MoneyAmountModel,
    private val currencyClicks: Observable<CurrencyAmount>,
    private val workerScheduler: Scheduler,
    private val uiScheduler: Scheduler
) {
    private val disposable: CompositeDisposable = CompositeDisposable()
    private val currenciesWithDiffRelay = BehaviorRelay.createDefault(
        CurrenciesWithDiff(currenciesList = emptyList(), diffResult = null)
    )

    fun start() {
        disposable += currenciesModel.observe()
            .observeOn(workerScheduler)
            .withLatestFrom(currenciesWithDiffRelay,
                BiFunction { currenciesState: CurrenciesState, currenciesWithDiff: CurrenciesWithDiff ->
                    val old = currenciesWithDiff.currenciesList
                    val new = currenciesState.list
                    val diffResult = DiffUtil.calculateDiff(CurrenciesAmountDiffCallback(old, new))
                    CurrenciesWithDiff(new, diffResult)
                })
            .subscribe(currenciesWithDiffRelay::accept)

        disposable += currencyClicks
            .subscribe { currenciesModel.pushCurrencyToTop(it.currency) }
    }

    fun stop() {
        disposable.clear()
    }

    fun currenciesWithDiff(): Observable<CurrenciesWithDiff> {
        return currenciesWithDiffRelay.observeOn(uiScheduler)
    }
}