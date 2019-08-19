package com.aintogarov.currencyconverter.data.storage

import com.aintogarov.currencyconverter.data.network.dto.RatesDto
import io.reactivex.Maybe
import java.math.BigDecimal


class StorageImpl : Storage {

    override fun writeRates(ratesDto: RatesDto) {
        //todo
    }

    override fun rates(base: String): Maybe<RatesDto> {
        return Maybe.fromCallable {
            RatesDto("EUR", mapOf("USD" to BigDecimal("1.6")))
        }
    }

    override fun order(): Maybe<List<String>> {
        return Maybe.fromCallable {
            null
        }
    }

    override fun writeOrder(order: List<String>) {
        //todo
    }
}