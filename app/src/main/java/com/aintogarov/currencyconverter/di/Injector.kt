package com.aintogarov.currencyconverter.di

import com.aintogarov.currencyconverter.App
import com.aintogarov.currencyconverter.di.modules.AppModule


object Injector {
    lateinit var appComponent: AppComponent

    fun init(app: App) {
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(app))
            .build()
    }
}