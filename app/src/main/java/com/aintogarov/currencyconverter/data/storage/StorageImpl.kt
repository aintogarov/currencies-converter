package com.aintogarov.currencyconverter.data.storage

import com.aintogarov.currencyconverter.data.network.dto.RatesDto
import io.reactivex.Maybe


class StorageImpl : Storage {

    override fun writeRates(ratesDto: RatesDto) {
        //todo
    }

    override fun rates(base: String): Maybe<RatesDto> {
        return Maybe.fromCallable {
            null
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