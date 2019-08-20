package com.aintogarov.currencyconverter.di.modules

import android.content.Context
import androidx.room.Room
import com.aintogarov.currencyconverter.data.storage.AppDatabase
import com.aintogarov.currencyconverter.data.storage.Storage
import com.aintogarov.currencyconverter.data.storage.StorageImpl
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executors
import javax.inject.Singleton


@Module(includes = [AppModule::class])
class StorageModule {

    @Singleton
    @Provides
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "appDB").build()
    }

    @Singleton
    @Provides
    fun provideStorage(appDatabase: AppDatabase, gson: Gson): Storage {
        return StorageImpl(
            ratesDao = appDatabase.ratesDao(),
            keyValueDao = appDatabase.keyValueDao(),
            executorService = Executors.newCachedThreadPool(),
            gson = gson
        )
    }
}