package com.aintogarov.currencyconverter.di.modules

import com.aintogarov.currencyconverter.data.network.NetworkApi
import com.aintogarov.currencyconverter.data.storage.Storage
import com.aintogarov.currencyconverter.domain.CurrenciesModel
import com.aintogarov.currencyconverter.domain.MoneyAmountModel
import com.aintogarov.currencyconverter.domain.RatesDispatchConfig
import com.aintogarov.currencyconverter.domain.RatesModel
import dagger.Module
import dagger.Provides
import java.math.BigDecimal
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

    @Singleton
    @Provides
    fun provideMoneyAmountModel(): MoneyAmountModel {
        return MoneyAmountModel().apply { push("EUR", BigDecimal("1.7")) }
    }

    @Singleton
    @Provides
    fun provideCurrenciesModel(
        storage: Storage,
        moneyAmountModel: MoneyAmountModel,
        ratesModel: RatesModel
    ): CurrenciesModel {
        return CurrenciesModel(
            storage = storage,
            config = RatesDispatchConfig.DEFAULT,
            moneyAmountModel = moneyAmountModel,
            ratesModel = ratesModel
        )
    }
}