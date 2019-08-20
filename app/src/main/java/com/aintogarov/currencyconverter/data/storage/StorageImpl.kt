@file:Suppress("RemoveExplicitTypeArguments")

package com.aintogarov.currencyconverter.data.storage

import com.aintogarov.currencyconverter.data.network.dto.RatesDto
import com.aintogarov.currencyconverter.domain.MoneyAmountState
import com.google.gson.Gson
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.util.concurrent.ExecutorService


class StorageImpl(
    private val ratesDao: RatesDao,
    private val keyValueDao: KeyValueDao,
    private val executorService: ExecutorService,
    private val gson: Gson
) : Storage {
    private val scheduler = Schedulers.from(executorService)

    override fun rates(base: String): Maybe<RatesDto> {
        return Maybe.fromCallable<RatesDto> {
            val rates = ratesDao.getAll()
            return@fromCallable if (rates.isNotEmpty()) {
                RatesDto(rates.map { it.currency to BigDecimal(it.value) }.toMap())
            } else {
                null
            }
        }.subscribeOn(scheduler)
    }

    override fun writeRates(ratesDto: RatesDto) {
        executorService.submit {
            val ratesDB = ratesDto.rates.map { RateDB(it.key, it.value.toPlainString()) }
            ratesDao.deleteAllAndInsert(ratesDB)
        }
    }

    override fun order(): Maybe<List<String>> {
        return Maybe.fromCallable<List<String>> {
            keyValueDao.get(KEY_ORDER)?.value?.split(',')
        }.subscribeOn(scheduler)
    }

    override fun writeOrder(order: List<String>) {
        executorService.submit {
            val serializedValue = order.joinToString(separator = ",")
            keyValueDao.insert(KeyValueDB(KEY_ORDER, serializedValue))
        }
    }

    override fun moneyAmount(): Maybe<MoneyAmountState> {
        return Maybe.fromCallable<MoneyAmountState> {
            val raw = keyValueDao.get(KEY_MONEY_AMOUNT)?.value
            raw?.let { gson.fromJson(it, MoneyAmountState::class.java) }
        }.subscribeOn(scheduler)
    }

    override fun writeMoneyAmount(moneyAmountState: MoneyAmountState) {
        executorService.submit {
            val jo = gson.toJson(moneyAmountState)
            keyValueDao.insert(KeyValueDB(KEY_MONEY_AMOUNT, jo))
        }
    }

    private companion object {
        private const val KEY_ORDER = "key_order"
        private const val KEY_MONEY_AMOUNT = "key_money_amount"
    }
}