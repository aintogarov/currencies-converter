package com.aintogarov.currencyconverter.domain

import com.aintogarov.currencyconverter.data.network.NetworkApi
import com.aintogarov.currencyconverter.data.storage.Storage
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.zipWith
import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


class RatesModel(
    networkApi: NetworkApi,
    storage: Storage,
    private val config: RatesDispatchConfig,
    workerScheduler: Scheduler
) {
    private val ratesStateBehaviorRelay = BehaviorRelay.create<RatesState>()
    private var disposable: Disposable? = null

    private val network = networkApi.rates(config.baseCurrency)
        .subscribeOn(workerScheduler)
        .map { ratesDto ->
            val rates = LinkedHashMap<String, BigDecimal>()
            rates[config.baseCurrency] = BigDecimal("1.0")
            rates.putAll(ratesDto.rates)
            return@map ratesDto.copy(rates = rates)
        }
        .doOnSuccess(storage::writeRates)
        .map<RatesState>(RatesState::Loaded)
        .retryWhen(this::handlerNetworkErrorWithRetry)
        .repeatWhen { completed -> completed.delay(config.interval, TimeUnit.MILLISECONDS) }
        .toObservable()
        .onErrorResumeNext(this::handleTerminalNetworkError)

    private val cache = storage.rates(config.baseCurrency)
        .subscribeOn(workerScheduler)
        .map<RatesState>(RatesState::Loaded)
        .toObservable()

    fun observe(): Observable<RatesState> = ratesStateBehaviorRelay

    fun onStart() {
        load()
    }

    fun onStop() {
        disposable?.dispose()
    }

    fun retry() {
        load()
    }

    private fun load() {
        disposable?.dispose()

        disposable = cache.concatWith(network)
            .startWith(RatesState.Empty)
            .doOnNext { Timber.d(it.toString()) }
            .doOnError { Timber.e(it) }
            .onErrorReturn(RatesState::Error)
            .subscribe(ratesStateBehaviorRelay::accept)
    }

    private object RetryEvent

    private fun handlerNetworkErrorWithRetry(errors: Flowable<Throwable>): Flowable<RetryEvent> {
        return errors.zipWith(Flowable.range(1, 4), { error: Throwable, retryIndex: Int -> error to retryIndex })
            .flatMap { pair ->
                return@flatMap if (pair.second == 4) {
                    Flowable.error<RetryEvent>(pair.first)
                } else {
                    Flowable.timer(1, TimeUnit.SECONDS)
                        .map { RetryEvent }
                }
            }
    }

    private fun handleTerminalNetworkError(throwable: Throwable): Observable<RatesState> {
        return Observable.just(throwable)
            .withLatestFrom(ratesStateBehaviorRelay,
                BiFunction { error: Throwable, state: RatesState ->
                    return@BiFunction state.takeIf { it is RatesState.Loaded } ?: throw error
                }
            )
    }
}