package com.aintogarov.currencyconverter.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface KeyValueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(keyValueDB: KeyValueDB)

    @Query("SELECT * FROM key_value_table WHERE id = :id")
    fun get(id: String): KeyValueDB?
}