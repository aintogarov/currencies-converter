package com.aintogarov.currencyconverter.di.modules

import com.aintogarov.currencyconverter.data.storage.Storage
import com.aintogarov.currencyconverter.data.storage.StorageImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class StorageModule {

    @Singleton
    @Provides
    fun provideStorage(): Storage {
        return StorageImpl()
    }
}