package com.aintogarov.currencyconverter.di.modules

import com.aintogarov.currencyconverter.data.network.NetworkApi
import com.aintogarov.currencyconverter.data.storage.Storage
import com.aintogarov.currencyconverter.domain.RatesDispatchConfig
import com.aintogarov.currencyconverter.domain.RatesModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [
        NetworkModule::class,
        StorageModule::class
    ]
)
class RatesModule {

    @Singleton
    @Provides
    fun provideRatesModel(networkApi: NetworkApi, storage: Storage): RatesModel {
        return RatesModel(networkApi, storage, RatesDispatchConfig.DEFAULT)
    }
}