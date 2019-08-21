package com.aintogarov.currencyconverter.di.modules

import android.content.Context
import com.aintogarov.currencyconverter.App
import com.aintogarov.currencyconverter.utils.KeyboardModel
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class AppModule(private val app: App) {

    @Singleton
    @Provides
    fun provideContext(): Context {
        return app
    }

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return Gson()
    }

    @Singleton
    @Provides
    fun provideKeyboardModel(): KeyboardModel {
        return KeyboardModel()
    }
}