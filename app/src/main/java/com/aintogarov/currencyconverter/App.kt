package com.aintogarov.currencyconverter

import android.app.Application
import com.aintogarov.currencyconverter.di.Injector
import timber.log.Timber


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Injector.init()
    }
}