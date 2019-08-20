package com.aintogarov.currencyconverter.domain

import com.aintogarov.currencyconverter.data.network.NetworkApi
import com.aintogarov.currencyconverter.data.storage.Storage
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


class RatesModel(
    private val networkApi: NetworkApi,
    private val storage: Storage,
    private val config: RatesDispatchConfig
) {
    private val ratesStateBehaviorRelay = BehaviorRelay.create<RatesState>()
    private var disposable: Disposable? = null

    fun observe(): Observable<RatesState> = ratesStateBehaviorRelay

    fun startUpdates() {
        disposable?.dispose()
        val cache = storage.rates(config.baseCurrency)
            .map<RatesState>(RatesState::Loaded)
            .toObservable()

        val network = Observable.interval(0L, config.interval, TimeUnit.MILLISECONDS)
            .map { config.baseCurrency }
            .flatMapSingle(networkApi::rates)
            .map { ratedDto ->
                val rates = LinkedHashMap<String, BigDecimal>()
                rates[config.baseCurrency] = BigDecimal("1.0")
                rates.putAll(ratedDto.rates)
                return@map ratedDto.copy(rates = rates)
            }
            .map<RatesState>(RatesState::Loaded)
            .onErrorResumeNext(this::handleNetworkError)

        disposable = Observable.merge(cache, network)
            .startWith(RatesState.Empty)
            .doOnNext { Timber.d(it.toString()) }
            .subscribe(ratesStateBehaviorRelay::accept)
    }

    fun stopUpdates() {
        disposable?.dispose()
    }

    private fun handleNetworkError(throwable: Throwable): Observable<RatesState> {
        return Observable.fromCallable { throwable }
            .withLatestFrom(ratesStateBehaviorRelay, BiFunction { error: Throwable, state: RatesState ->
                return@BiFunction if (state.ratesDto == null) {
                    RatesState.Error(error)
                } else {
                    state
                }
            })
    }
}