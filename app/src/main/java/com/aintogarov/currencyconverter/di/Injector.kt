package com.aintogarov.currencyconverter.di


object Injector {
    lateinit var appComponent: AppComponent

    fun init() {
        appComponent = DaggerAppComponent.builder()
            .build()
    }
}