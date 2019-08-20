package com.aintogarov.currencyconverter.data.storage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RateDB::class, KeyValueDB::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ratesDao(): RatesDao
    abstract fun keyValueDao(): KeyValueDao
}