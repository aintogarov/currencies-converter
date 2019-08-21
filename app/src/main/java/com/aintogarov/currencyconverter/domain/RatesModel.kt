package com.aintogarov.currencyconverter.domain

import com.aintogarov.currencyconverter.data.network.NetworkApi
import com.aintogarov.currencyconverter.data.storage.Storage
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.zipWith
import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


class RatesModel(
    private val networkApi: NetworkApi,
    private val storage: Storage,
    private val config: RatesDispatchConfig,
    private val workerScheduler: Scheduler
) {
    private val ratesStateBehaviorRelay = BehaviorRelay.create<RatesState>()
    private var disposable: Disposable? = null

    fun observe(): Observable<RatesState> = ratesStateBehaviorRelay

    fun onStart() {
        disposable?.dispose()
        val cache = storage.rates(config.baseCurrency)
            .subscribeOn(workerScheduler)
            .map<RatesState>(RatesState::Loaded)
            .toObservable()

        val network = networkApi.rates(config.baseCurrency)
            .subscribeOn(workerScheduler)
            .map { ratesDto ->
                val rates = LinkedHashMap<String, BigDecimal>()
                rates[config.baseCurrency] = BigDecimal("1.0")
                rates.putAll(ratesDto.rates)
                return@map ratesDto.copy(rates = rates)
            }
            .map<RatesState>(RatesState::Loaded)
            .retryWhen(this::handlerErrorWithRetry)
            .repeatWhen { completed -> completed.delay(config.interval, TimeUnit.MILLISECONDS) }
            .toObservable()

        disposable = cache.concatWith(network)
            .startWith(RatesState.Empty)
            .doOnNext { Timber.d(it.toString()) }
            .onErrorReturn(RatesState::Error)
            .subscribe(ratesStateBehaviorRelay::accept)
    }

    fun onStop() {
        disposable?.dispose()
    }

    private object RetryEvent

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private fun handlerErrorWithRetry(errors: Flowable<Throwable>): Flowable<RetryEvent> {
        return errors.zipWith(Flowable.range(1, 3), { error: Throwable, retryIndex: Int -> error to retryIndex })
            .flatMap { pair ->
                return@flatMap if (pair.second == 3) {
                    Flowable.error<RetryEvent>(pair.first)
                } else {
                    Flowable.timer(1, TimeUnit.SECONDS)
                        .map { RetryEvent }
                }
            }
    }
}