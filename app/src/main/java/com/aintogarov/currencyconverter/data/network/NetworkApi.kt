package com.aintogarov.currencyconverter.data.network

import com.aintogarov.currencyconverter.data.network.dto.RatesDto
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface NetworkApi {
    @GET("latest")
    fun rates(@Query("base") currencyCode: String): Single<RatesDto>
}