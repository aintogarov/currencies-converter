package com.aintogarov.currencyconverter.di

import com.aintogarov.currencyconverter.di.modules.NetworkModule
import com.aintogarov.currencyconverter.di.modules.RatesModule
import com.aintogarov.currencyconverter.di.modules.StorageModule
import com.aintogarov.currencyconverter.domain.CurrenciesModel
import com.aintogarov.currencyconverter.domain.MoneyAmountModel
import com.aintogarov.currencyconverter.domain.RatesModel
import dagger.Component
import javax.inject.Singleton

@Component(
    modules = [
        NetworkModule::class,
        StorageModule::class,
        RatesModule::class
    ]
)
@Singleton
interface AppComponent {
    fun ratesModel(): RatesModel
    fun amountModel(): MoneyAmountModel
    fun currenciesModel(): CurrenciesModel
}