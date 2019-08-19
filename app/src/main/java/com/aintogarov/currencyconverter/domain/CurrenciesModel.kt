package com.aintogarov.currencyconverter.domain

import com.aintogarov.currencyconverter.data.network.dto.RatesDto
import com.aintogarov.currencyconverter.data.storage.Storage
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList


class CurrenciesModel(
    private val storage: Storage,
    private val config: RatesDispatchConfig,
    moneyAmountObservable: Observable<MoneyAmountState>,
    ratesStateObservable: Observable<RatesState>
) {
    private val currenciesStateBehaviorRelay = BehaviorRelay.create<CurrenciesState>()
    private val orderBehaviorRelay = BehaviorRelay.create<List<String>>()

    private val rates = ratesStateObservable
        .filter { it is RatesState.Loaded }
        .map { it as RatesState.Loaded }
        .map { it.ratesDto }

    private val updateOrderObservable = rates.withLatestFrom(orderBehaviorRelay,
        BiFunction { ratesDto: RatesDto, order: List<String> ->
            val currencies = ratesDto.currencies(config.baseCurrency)
            val subtractionToAdd = currencies.subtract(order)
            val subtractionToRemove = order.subtract(currencies)
            return@BiFunction ArrayList(order).apply {
                addAll(subtractionToAdd)
                removeAll(subtractionToRemove)
            }
        })

    private val currencies = Observable.combineLatest<MoneyAmountState, RatesDto, List<String>, Container>(
        moneyAmountObservable,
        rates,
        orderBehaviorRelay,
        Function3 { moneyAmount: MoneyAmountState, ratesDto: RatesDto, order: List<String> ->
            Container(moneyAmount, ratesDto.rates, order)
        })
        .doOnNext { Timber.d("currencies: $it") }
        .filter { container: Container ->
            container.moneyAmountState.currency == container.currencyList.firstOrNull()
        }
        .map(this::calculate)
        .map(::CurrenciesState)

    private var disposable: CompositeDisposable = CompositeDisposable()

    fun observe(): Observable<CurrenciesState> {
        return currenciesStateBehaviorRelay
    }

    @Synchronized
    fun pushCurrencyToTop(currency: String) {
        orderBehaviorRelay.value?.let { list ->
            val currentIndex = list.indexOf(currency)
            if (currentIndex == 0) return

            val copyList = LinkedList(list)
            if (currentIndex != -1) {
                Collections.swap(copyList, 0, currentIndex)
            } else {
                copyList.addFirst(currency)
            }
            orderBehaviorRelay.accept(copyList)
        }
    }

    fun onStart() {
        disposable.clear()

        disposable += storage.order()
            .subscribeBy(
                onComplete = {
                    orderBehaviorRelay.accept(emptyList())
                },
                onSuccess = { cachedOrder ->
                    orderBehaviorRelay.accept(cachedOrder)
                })

        disposable += updateOrderObservable
            .distinctUntilChanged()
            .subscribe(orderBehaviorRelay::accept)

        disposable += currencies
            .subscribe(currenciesStateBehaviorRelay::accept)
    }

    fun onStop() {
        disposable.dispose()
        orderBehaviorRelay.value?.let { storage.writeOrder(it) }
    }

    private fun calculate(container: Container): List<CurrencyAmount> {
        val (moneyAmount, ratesMap, currencyList) = container
        val currentRateToBase: BigDecimal =
            ratesMap[moneyAmount.currency] ?: BigDecimal("1.0")
        val normalizedAmount = moneyAmount.value * currentRateToBase

        val result = ArrayList<CurrencyAmount>()
        for (currency in currencyList) {
            val rateToBase = ratesMap[currency] ?: continue
            val value = normalizedAmount / rateToBase
            result += CurrencyAmount(currency, value)
        }
        return result
    }

    private data class Container(
        val moneyAmountState: MoneyAmountState,
        val ratesMap: Map<String, BigDecimal>,
        val currencyList: List<String>
    )

    private companion object {
        @JvmStatic
        fun RatesDto.currencies(base: String): List<String> {
            val order = LinkedList(this.rates.keys)
            if (!order.contains(base)) {
                order.addFirst(base)
            }
            return order
        }
    }
}