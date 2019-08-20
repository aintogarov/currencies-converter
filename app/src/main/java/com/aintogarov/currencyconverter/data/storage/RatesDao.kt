package com.aintogarov.currencyconverter.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface RatesDao {

    @Query("SELECT * FROM rates")
    fun getAll(): List<RateDB>

    @Query("DELETE FROM rates")
    fun deleteAll()

    @Insert
    fun insert(rates: List<RateDB>)

    @Transaction
    fun deleteAllAndInsert(rates: List<RateDB>) {
        deleteAll()
        insert(rates)
    }
}