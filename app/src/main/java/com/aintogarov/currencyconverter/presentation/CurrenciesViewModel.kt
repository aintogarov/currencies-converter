package com.aintogarov.currencyconverter.presentation

import androidx.recyclerview.widget.DiffUtil
import com.aintogarov.currencyconverter.domain.CurrenciesModel
import com.aintogarov.currencyconverter.domain.dto.LoadingState
import com.aintogarov.currencyconverter.domain.dto.ReorderingEvent
import com.aintogarov.currencyconverter.presentation.adapter.CurrenciesAmountDiffCallback
import com.aintogarov.currencyconverter.presentation.adapter.CurrenciesWithDiff
import com.aintogarov.currencyconverter.presentation.dto.CurrencyAmountItem
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.plusAssign
import java.math.BigDecimal


class CurrenciesViewModel(
    private val currenciesModel: CurrenciesModel,
    private val viewContract: CurrenciesViewContract,
    private val workerScheduler: Scheduler,
    private val uiScheduler: Scheduler
) {
    private val disposable: CompositeDisposable = CompositeDisposable()
    private val currenciesWithDiffRelay = BehaviorRelay.createDefault(
        CurrenciesWithDiff(
            currenciesList = emptyList(),
            diffResult = null
        )
    )

    private val currenciesWithDiffObservable = currenciesModel.observe()
        .observeOn(workerScheduler)
        .map { currenciesState ->
            currenciesState.list.mapIndexed { index, currencyAmount ->
                CurrencyAmountItem(
                    currencyAmount,
                    selected = (index == 0)
                )
            }
        }
        .withLatestFrom(currenciesWithDiffRelay,
            BiFunction { new: List<CurrencyAmountItem>, currenciesWithDiff: CurrenciesWithDiff ->
                val old = currenciesWithDiff.currenciesList
                val diffResult = DiffUtil.calculateDiff(
                    CurrenciesAmountDiffCallback(
                        old,
                        new
                    )
                )
                CurrenciesWithDiff(new, diffResult)
            })

    fun start() {
        disposable += currenciesWithDiffObservable
            .subscribe(currenciesWithDiffRelay::accept)

        disposable += viewContract.currencyItemClicks
            .observeOn(workerScheduler)
            .subscribe { currenciesModel.pushCurrencyToTop(it.currency) }

        disposable += viewContract.amountInput
            .observeOn(workerScheduler)
            .map { if (it.isEmpty()) "0" else it }
            .map(::BigDecimal)
            .subscribe { currenciesModel.pushMoneyAmount(it) }

        disposable += viewContract.retryClicks
            .observeOn(workerScheduler)
            .subscribe { currenciesModel.retry() }
    }

    fun stop() {
        disposable.clear()
    }

    fun currenciesWithDiff(): Observable<CurrenciesWithDiff> {
        return currenciesWithDiffRelay.observeOn(uiScheduler)
    }

    fun loadingState(): Observable<LoadingState> {
        return currenciesModel.observeLoadingState()
            .observeOn(uiScheduler)
    }

    fun observeReordering(): Observable<ReorderingEvent> {
        return Observable.zip(currenciesModel.observeReordering(), currenciesWithDiffObservable,
            BiFunction { reorderingEvent: ReorderingEvent, _: CurrenciesWithDiff ->
                reorderingEvent
            })
            .observeOn(uiScheduler)
    }
}