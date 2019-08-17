package com.aintogarov.currencyconverter.data.storage

import com.aintogarov.currencyconverter.data.network.dto.RatesDto
import io.reactivex.Maybe


class StorageImpl : Storage {

    override fun rates(base: String): Maybe<RatesDto> {
        return Maybe.fromCallable {
            RatesDto("EUR", "Lol", mapOf("USD" to 1.16))
        }
    }
}