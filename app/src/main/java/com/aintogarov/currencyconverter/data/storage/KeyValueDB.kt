package com.aintogarov.currencyconverter.data.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "key_value_table")
data class KeyValueDB(
    @PrimaryKey val id: String,
    @ColumnInfo val value: String)