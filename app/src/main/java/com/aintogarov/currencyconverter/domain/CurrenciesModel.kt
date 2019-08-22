package com.aintogarov.currencyconverter.domain

import com.aintogarov.currencyconverter.data.network.dto.RatesDto
import com.aintogarov.currencyconverter.data.storage.Storage
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList


class CurrenciesModel(
    private val storage: Storage,
    private val config: RatesDispatchConfig,
    private val moneyAmountModel: MoneyAmountModel,
    private val ratesModel: RatesModel
) {
    private val currenciesStateBehaviorRelay = BehaviorRelay.create<CurrenciesState>()
    private val reorderingBus = PublishRelay.create<ReorderingEvent>()
    private val orderBehaviorRelay = BehaviorRelay.create<List<String>>()

    private val rates = ratesModel.observe()
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
        moneyAmountModel.observe(),
        rates,
        orderBehaviorRelay,
        Function3 { moneyAmount: MoneyAmountState, ratesDto: RatesDto, order: List<String> ->
            Container(moneyAmount, ratesDto.rates, order)
        })
        .filter { container: Container ->
            container.moneyAmountState.currency == container.currencyList.firstOrNull()
        }
        .map(this::calculate)
        .map(::CurrenciesState)

    private var disposable: CompositeDisposable = CompositeDisposable()

    fun observe(): Observable<CurrenciesState> {
        return currenciesStateBehaviorRelay
    }

    fun observeLoadingState(): Observable<LoadingState> {
        return ratesModel.observe()
            .map { state ->
                return@map when (state) {
                    is RatesState.Error -> LoadingState.Error
                    is RatesState.Loaded -> LoadingState.Loaded
                    RatesState.Empty -> LoadingState.Loading
                }
            }
    }

    fun observeReordering(): Observable<ReorderingEvent> {
        return reorderingBus
    }

    @Synchronized
    fun pushCurrencyToTop(currency: String) {
        orderBehaviorRelay.value?.let { list ->
            val currentIndex = list.indexOf(currency)
            if (currentIndex == 0) return

            val currencyAmount = currenciesStateBehaviorRelay.value?.list?.get(currentIndex)
            currencyAmount?.let {
                moneyAmountModel.push(it.currency, it.value)
            }

            val copyList = LinkedList(list)
            copyList.removeAt(currentIndex)
            copyList.addFirst(list[currentIndex])

            orderBehaviorRelay.accept(copyList)
            reorderingBus.accept(ReorderingEvent)

            storage.writeOrder(copyList)
        }
    }

    @Synchronized
    fun pushMoneyAmount(amount: BigDecimal) {
        disposable += Observable.just(amount)
            .withLatestFrom(moneyAmountModel.observe(),
                BiFunction { moneyAmount: BigDecimal, moneyAmountState: MoneyAmountState ->
                    moneyAmountState.currency to moneyAmount
                }
            )
            .subscribe { moneyAmountModel.push(it.first, it.second) }
    }

    @Synchronized
    fun retry() {
        ratesModel.retry()
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
        disposable.clear()
    }

    private fun calculate(container: Container): List<CurrencyAmount> {
        val (moneyAmount, ratesMap, currencyList) = container
        val currentRateToBase: BigDecimal =
            ratesMap[moneyAmount.currency] ?: BigDecimal("1.0")
        val normalizedAmount = moneyAmount.value.divide(currentRateToBase, 4, RoundingMode.HALF_UP)

        val result = ArrayList<CurrencyAmount>()
        for (currency in currencyList) {
            val rateToBase = ratesMap[currency] ?: continue
            val value = if (currency == moneyAmount.currency) {
                moneyAmount.value
            } else {
                (normalizedAmount * rateToBase).setScale(2, RoundingMode.HALF_UP)
            }
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